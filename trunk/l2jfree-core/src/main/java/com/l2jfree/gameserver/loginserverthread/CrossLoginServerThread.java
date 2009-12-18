/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.loginserverthread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.IOFloodManager;
import com.l2jfree.gameserver.network.L2GameClient.GameClientState;
import com.l2jfree.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jfree.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jfree.gameserver.network.gameserverpackets.CompatibleProtocol;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jfree.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfree.gameserver.network.gameserverpackets.ServerStatusL2jfree;
import com.l2jfree.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jfree.gameserver.network.loginserverpackets.InitLS;
import com.l2jfree.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jfree.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jfree.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jfree.gameserver.network.loginserverpackets.PlayerLoginAttempt;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.network.serverpackets.LoginFail;
import com.l2jfree.tools.security.NewCrypt;
import com.l2jfree.tools.util.HexUtil;

/**
 * @author savormix
 */
public final class CrossLoginServerThread extends LoginServerThread
{
	public static final int	PROTOCOL_L2J		= 258;
	public static final int	PROTOCOL_LEGACY		= 259;
	// protocol 1 does not support connection filtering
	public static final int	PROTOCOL_CURRENT	= 2;

	private int				_protocol;

	public CrossLoginServerThread()
	{
		super();
	}

	@Override
	public void run()
	{
		while (true)
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				_log.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());

				// init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (true)
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = lengthHi * 256 + lengthLo;

					if (lengthHi < 0)
					{
						_log.debug("LoginServerThread: Login terminated the connection.");
						break;
					}

					byte[] incoming = new byte[length];
					incoming[0] = (byte) lengthLo;
					incoming[1] = (byte) lengthHi;

					int receivedBytes = 0;
					int newBytes = 0;
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, 2, length - 2);
						receivedBytes = receivedBytes + newBytes;
					}

					if (receivedBytes != length - 2)
					{
						_log.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}

					byte[] decrypt = new byte[length - 2];
					System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
					// decrypt if we have a key
					decrypt = _blowfish.decrypt(decrypt);
					checksumOk = NewCrypt.verifyChecksum(decrypt);

					if (!checksumOk)
					{
						_log.warn("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}

					if (_log.isDebugEnabled())
						_log.debug("[C]\n" + HexUtil.printData(decrypt));

					int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
					case 00: // 0x00
						InitLS init = new InitLS(decrypt);
						if (_log.isDebugEnabled())
							_log.debug("Init received");
						if (init.getRevision() != PROTOCOL_L2J && init.getRevision() != PROTOCOL_LEGACY)
						{
							// WTF? Some retard thinks he is God?
							_log.warn("The specified login server does not support L2J!");
							break;
						}
						else if (init.getTrueRevision() == PROTOCOL_CURRENT)
						{
							// Fully compatible login
							_protocol = PROTOCOL_CURRENT;
							sendPacket(new CompatibleProtocol());
						}
						else
						{
							// Default compatibility login
							_protocol = init.getRevision();
							// not supported
							Config.CONNECTION_FILTERING = false;
						}

						try
						{
							KeyFactory kfac = KeyFactory.getInstance("RSA");
							BigInteger modulus = new BigInteger(init.getRSAKey());
							RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
							_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							if (_log.isDebugEnabled())
								_log.debug("RSA key set up");
						}
						catch (GeneralSecurityException e)
						{
							_log.warn("Troubles while init the public key send by login");
							break;
						}
						// send the blowfish key through the rsa encryption
						BlowFishKey bfk = new BlowFishKey(_protocol, _blowfishKey, _publicKey);
						sendPacket(bfk);
						if (_log.isDebugEnabled())
							_log.info("Sent new blowfish key");
						// now, only accept paket with the new encryption
						_blowfish = new NewCrypt(_blowfishKey);
						if (_log.isDebugEnabled())
							_log.info("Changed blowfish key");
						AuthRequest ar = new AuthRequest(_protocol, _requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort,
								_reserveHost, _maxPlayer);
						sendPacket(ar);
						if (_log.isDebugEnabled())
							_log.debug("Sent AuthRequest to login");
						break;
					case 01: // 0x01
						LoginServerFail lsf = new LoginServerFail(decrypt);
						_log.info("Damn! Registration Failed: " + lsf.getReasonString());
						// login will close the connection here
						break;
					case 02: // 0x02
						AuthResponse aresp = new AuthResponse(CrossLoginServerThread.PROTOCOL_LEGACY, decrypt);
						_serverID = aresp.getServerId();
						_serverName = aresp.getServerName();
						Config.saveHexid(_serverID, hexToString(_hexID));
						_log.info("Registered on login as Server " + _serverID + " : " + _serverName);
						ServerStatus st = new ServerStatus(_protocol);
						st.addAttribute(ServerStatus.SERVER_LIST_PVP, Config.SERVER_PVP);
						//max players already sent with auth
						if (Config.SERVER_GMONLY)
						{
							if (_protocol == PROTOCOL_LEGACY)
								_status = ServerStatusL2jfree.STATUS_GM_ONLY;
							else
								_status = ServerStatus.STATUS_GM_ONLY;
						}
						else
							_status = ServerStatus.STATUS_AUTO;
						st.addAttribute(ServerStatus.SERVER_LIST_STATUS, _status);
						st.addAttribute(ServerStatus.SERVER_LIST_UNK, Config.SERVER_BIT_1);
						st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, Config.SERVER_BIT_2);
						st.addAttribute(ServerStatus.SERVER_LIST_HIDE_NAME, Config.SERVER_BIT_3);
						st.addAttribute(ServerStatus.TEST_SERVER, Config.SERVER_BIT_4);
						st.addAttribute(ServerStatus.SERVER_LIST_BRACKETS, Config.SERVER_LIST_BRACKET);
						st.addAttribute(ServerStatus.SERVER_AGE_LIMITATION, Config.SERVER_AGE_LIM);
						sendPacket(st);
						if (L2World.getInstance().getAllPlayersCount() > 0)
						{
							FastList<String> playerList = new FastList<String>();
							for (L2PcInstance player : L2World.getInstance().getAllPlayers())
								playerList.add(player.getAccountName());
							sendPacket(new PlayerInGame(_protocol, playerList.toArray(new String[playerList.size()])));
						}
						break;
					case 03: // 0x03
						PlayerAuthResponse par = new PlayerAuthResponse(_protocol, decrypt);
						String account = par.getAccount();
						WaitingClient wcToRemove = null;
						synchronized (_waitingClients)
						{
							for (WaitingClient wc : _waitingClients)
								if (wc.account.equals(account))
									wcToRemove = wc;
						}
						if (wcToRemove != null)
						{
							if (par.isAuthed())
							{
								if (_log.isDebugEnabled())
									_log.debug("Login accepted player " + wcToRemove.account + " waited("
											+ (GameTimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
								sendPacket(new PlayerInGame(_protocol, par.getAccount()));
								wcToRemove.gameClient.setState(GameClientState.AUTHED);
								wcToRemove.gameClient.setSessionId(wcToRemove.session);
								CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
								wcToRemove.gameClient.sendPacket(cl);
								wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								wcToRemove.gameClient.setHostAddress(par.getHost());
							}
							else
							{
								_log.warn("session key is not correct. closing connection");
								wcToRemove.gameClient.sendPacket(new LoginFail(1));
								wcToRemove.gameClient.closeNow();
							}
							_waitingClients.remove(wcToRemove);
						}
						break;
					case 04: // 0x04
						KickPlayer kp = new KickPlayer(_protocol, decrypt);
						doKickPlayer(kp.getAccount());
						break;
					case 020: // 0x10
						PlayerLoginAttempt pla = new PlayerLoginAttempt(_protocol, decrypt);
						IOFloodManager.legalize(pla.getIP());
						break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				_log.warn(e.getMessage(), e);
			}
			catch (IOException e)
			{
				_log.info("Disconnected from Login, Trying to reconnect:", e);
			}
			finally
			{
				try
				{
					_loginSocket.close();
				}
				catch (Exception e)
				{
				}
			}

			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#getMaxPlayer()
	 */
	@Override
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#setMaxPlayers(int)
	 */
	@Override
	public void setMaxPlayers(int maxPlayer)
	{
		_maxPlayer = maxPlayer;
		ServerStatus ss = new ServerStatus(_protocol);
		ss.addAttribute(ServerStatus.SERVER_LIST_MAX_PLAYERS, maxPlayer);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (_log.isDebugEnabled())
				_log.debug("Cannot send new max player count", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#changeAttribute()
	 */
	@Override
	public void changeAttribute(int id, int value)
	{
		ServerStatus ss = new ServerStatus(_protocol);
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (_log.isDebugEnabled())
				_log.debug(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#getServerStatus()
	 */
	@Override
	public int getServerStatus()
	{
		return _status;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#setServerStatus(int)
	 */
	@Override
	public void setServerStatus(int status)
	{
		_status = status;
		changeAttribute(ServerStatus.SERVER_LIST_STATUS, status);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#setServerStatusDown()
	 */
	@Override
	public void setServerStatusDown()
	{
		setServerStatus(ServerStatus.STATUS_DOWN);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.LoginServerThread#getProtocol()
	 */
	@Override
	public int getProtocol()
	{
		return _protocol;
	}
}

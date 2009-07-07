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
import com.l2jfree.gameserver.network.L2GameClient.GameClientState;
import com.l2jfree.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jfree.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jfree.gameserver.network.gameserverpackets.ServerStatusL2j;
import com.l2jfree.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jfree.gameserver.network.loginserverpackets.InitLS;
import com.l2jfree.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jfree.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jfree.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.network.serverpackets.LoginFail;
import com.l2jfree.tools.security.NewCrypt;
import com.l2jfree.tools.util.HexUtil;

public class LoginServerThreadL2j extends LoginServerThread
{
	/** {@see com.l2jfree.loginserver.LoginServer#PROTOCOL_REV } */
	private static final int			REVISION	= CrossLoginServerThread.PROTOCOL_L2J;
	
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
						_log.debug("LoginServerThreadComp: Login terminated the connection.");
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
						case 00:
							InitLS init = new InitLS(decrypt);
							if (_log.isDebugEnabled())
								_log.debug("Init received");
							if (init.getRevision() != REVISION)
							{
								_log.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
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
							BlowFishKey bfk = new BlowFishKey(REVISION, _blowfishKey, _publicKey);
							sendPacket(bfk);
							if (_log.isDebugEnabled())
								_log.info("Sent new blowfish key");
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							if (_log.isDebugEnabled())
								_log.info("Changed blowfish key");
							AuthRequest ar = new AuthRequest(REVISION, _requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							if (_log.isDebugEnabled())
								_log.debug("Sent AuthRequest to login");
							break;
						case 01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							_log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 02:
							AuthResponse aresp = new AuthResponse(REVISION, decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							_log.info("Registered on login as Server " + _serverID + " : " + _serverName);
							ServerStatusL2j st = new ServerStatusL2j();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_SQUARE_BRACKET, ServerStatusL2j.ON);
							}
							else
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_SQUARE_BRACKET, ServerStatusL2j.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_CLOCK, ServerStatusL2j.ON);
							}
							else
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_CLOCK, ServerStatusL2j.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatusL2j.TEST_SERVER, ServerStatusL2j.ON);
							}
							else
							{
								st.addAttribute(ServerStatusL2j.TEST_SERVER, ServerStatusL2j.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_AUTO);
							}
							sendPacket(st);
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								FastList<String> playerList = new FastList<String>();
								for (L2PcInstance player : L2World.getInstance().getAllPlayers())
								{
									playerList.add(player.getAccountName());
								}
								PlayerInGame pig = new PlayerInGame(REVISION, playerList.toArray(new String[playerList.size()]));
								sendPacket(pig);
							}
							break;
						case 03:
							PlayerAuthResponse par = new PlayerAuthResponse(REVISION, decrypt);
							String account = par.getAccount();
							WaitingClient wcToRemove = null;

							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									if (_log.isDebugEnabled())
										_log.debug("Login accepted player " + wcToRemove.account + " waited(" + (GameTimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
									PlayerInGame pig = new PlayerInGame(REVISION, par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									_log.warn("session key is not correct. closing connection");
									wcToRemove.gameClient.sendPacket(new LoginFail(1));
									wcToRemove.gameClient.closeNow();
								}
								synchronized (_waitingClients)
								{
									_waitingClients.remove(wcToRemove);
								}
							}
							break;
						case 04:
							KickPlayer kp = new KickPlayer(CrossLoginServerThread.PROTOCOL_L2J, decrypt);
							doKickPlayer(kp.getAccount());
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
				_log.info("Deconnected from Login, Trying to reconnect:");
				_log.info(e.toString());
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
				//
			}
		}
	}

	@Override
	public void changeAttribute(int id, int value)
	{
		ServerStatusL2j ss = new ServerStatusL2j();
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

	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatusL2j.STATUS_STRING[_status];
	}

	@Override
	public int getServerStatus()
	{
		return _status;
	}

	@Override
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatusL2j.STATUS_AUTO:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatusL2j.STATUS_DOWN:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatusL2j.STATUS_FULL:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_FULL);
				_status = status;
				break;
			case ServerStatusL2j.STATUS_GM_ONLY:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatusL2j.STATUS_GOOD:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatusL2j.STATUS_NORMAL:
				changeAttribute(ServerStatusL2j.SERVER_LIST_STATUS, ServerStatusL2j.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}

	@Override
	public void setServerStatusDown()
	{
		setServerStatus(ServerStatusL2j.STATUS_DOWN);
	}
	
	@Override
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	@Override
	public void setMaxPlayers(int maxPlayer)
	{
		_maxPlayer = maxPlayer;
	}

	@Override
	protected int getProtocol()
	{
		return REVISION;
	}
}

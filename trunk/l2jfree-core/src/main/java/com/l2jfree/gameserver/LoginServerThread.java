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
package com.l2jfree.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.L2GameClient.GameClientState;
import com.l2jfree.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jfree.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jfree.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jfree.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jfree.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfree.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jfree.gameserver.network.loginserverpackets.InitLS;
import com.l2jfree.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jfree.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jfree.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.network.serverpackets.LoginFail;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.tools.security.NewCrypt;
import com.l2jfree.tools.util.HexUtil;

public class LoginServerThread extends Thread
{
	protected static Log				_log		= LogFactory.getLog(LoginServerThread.class.getName());

	/** The LoginServerThread singleton */
	private static LoginServerThread	_instance;

	/** {@see com.l2jfree.loginserver.LoginServer#PROTOCOL_REV } */
	private static final int			REVISION	= 0x0103;
	private RSAPublicKey				_publicKey;
	private String						_hostname;
	private int							_port;
	private int							_gamePort;
	private Socket						_loginSocket;
	private InputStream					_in;
	private OutputStream				_out;

	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt					_blowfish;
	private byte[]						_blowfishKey;
	private byte[]						_hexID;
	private boolean						_acceptAlternate;
	private int							_requestID;
	private int							_serverID;
	private boolean						_reserveHost;
	private int							_maxPlayer;
	private final List<WaitingClient>			_waitingClients;
	private Map<String, L2GameClient>	_accountsInGameServer;
	private int							_status;
	private String						_serverName;
	private String						_gameExternalHost;													// External host for old login server
	private String						_gameInternalHost;													// Internal host for old login server

	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new FastList<WaitingClient>();
		_accountsInGameServer = new FastMap<String, L2GameClient>().setShared(true);
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;

        if (Config.SUBNETWORKS != null && Config.SUBNETWORKS.length() > 0)  
        {
            _gameExternalHost = Config.SUBNETWORKS;
            _gameInternalHost = "";
        }


	}

	public static LoginServerThread getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginServerThread();
		}
		return _instance;
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
						BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
						sendPacket(bfk);
						if (_log.isDebugEnabled())
							_log.info("Sent new blowfish key");
						// now, only accept paket with the new encryption
						_blowfish = new NewCrypt(_blowfishKey);
						if (_log.isDebugEnabled())
							_log.info("Changed blowfish key");
						AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost,
								_maxPlayer);
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
						AuthResponse aresp = new AuthResponse(decrypt);
						_serverID = aresp.getServerId();
						_serverName = aresp.getServerName();
						Config.saveHexid(_serverID, hexToString(_hexID));
						_log.info("Registered on login as Server " + _serverID + " : " + _serverName);
						ServerStatus st = new ServerStatus();
						st.addAttribute(ServerStatus.SERVER_LIST_PVP, Config.SERVER_PVP);
						//max players already sent with auth
						st.addAttribute(ServerStatus.SERVER_LIST_STATUS, Config.SERVER_GMONLY);
						st.addAttribute(ServerStatus.SERVER_LIST_UNK, Config.SERVER_BIT_1);
						st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, Config.SERVER_BIT_2);
						st.addAttribute(ServerStatus.SERVER_LIST_HIDE_NAME, Config.SERVER_BIT_3);
						st.addAttribute(ServerStatus.TEST_SERVER, Config.SERVER_BIT_4);
						st.addAttribute(ServerStatus.SERVER_LIST_BRACKETS, Config.SERVER_LIST_BRACKET);
						st.addMinAgeAttribute(Config.SERVER_AGE_LIM);
						sendPacket(st); st = null;
						if (L2World.getInstance().getAllPlayersCount() > 0)
						{
							FastList<String> playerList = new FastList<String>();
							for (L2PcInstance player : L2World.getInstance().getAllPlayers())
							{
								playerList.add(player.getAccountName());
							}
							sendPacket(new PlayerInGame(playerList));
						}
						break;
					case 03:
						PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
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
									_log.debug("Login accepted player " + wcToRemove.account + " waited("
											+ (GameTimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
								sendPacket(new PlayerInGame(par.getAccount()));
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
					case 04:
						KickPlayer kp = new KickPlayer(decrypt);
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

	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		if (_log.isDebugEnabled())
			_log.debug(key);
		WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			_log.warn("Error while sending player auth request");
			if (_log.isDebugEnabled())
				_log.debug(e.getMessage(), e);
		}
	}

	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			if (toRemove != null)
				_waitingClients.remove(toRemove);
		}
	}

	public void sendLogout(String account)
	{
		if (account == null || account.isEmpty())
			return;
		
		_accountsInGameServer.remove(account);
		
		try
		{
			sendPacket(new PlayerLogout(account));
		}
		catch (IOException e)
		{
			_log.warn("Error while sending logout packet to login", e);
		}
	}

	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}

	public void sendAccessLevel(String account, int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
			if (_log.isDebugEnabled())
				_log.debug(e.getMessage(), e);
		}
	}

	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}

	public void doKickPlayer(String account)
	{
		if (_accountsInGameServer.get(account) != null)
			_accountsInGameServer.get(account).closeNow();
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		if (_log.isDebugEnabled())
			_log.debug("Generated random String:  \"" + array + "\"");
		return array;
	}

	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (_log.isDebugEnabled())
			_log.debug("[S]\n" + HexUtil.printData(data));
		data = _blowfish.crypt(data);

		int len = data.length + 2;
		synchronized (_out) // avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	/**
	 * @param maxPlayer
	 *            The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		_maxPlayer = maxPlayer;
		sendMaxPlayer(maxPlayer);
	}

	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	private void sendMaxPlayer(int newCount)
	{
		ServerStatus ss = new ServerStatus();
		ss.addMaxPlayerAttribute(newCount);
		try { sendPacket(ss); }
		catch (IOException e) { if (_log.isDebugEnabled()) _log.debug(e.getMessage(), e); }
		ss = null;
	}

	/**
	 * @param minAge
	 *            The minAge to set.
	 */
	public void setMinAge(int minAge)
	{
		Config.SERVER_AGE_LIM = minAge;
		sendMinAge(Config.SERVER_AGE_LIM);
	}

	private void sendMinAge(int newAge)
	{
		ServerStatus ss = new ServerStatus();
		ss.addMinAgeAttribute(newAge);
		try { sendPacket(ss); }
		catch (IOException e) { if (_log.isDebugEnabled()) _log.debug(e.getMessage(), e); }
		ss = null;
	}

	public void toggleServerAttribute(int attrib, boolean on)
	{
		switch(attrib)
		{
		case ServerStatus.SERVER_LIST_UNK:
			Config.SERVER_BIT_1 = on; break;
		case ServerStatus.SERVER_LIST_CLOCK:
			Config.SERVER_BIT_2 = on; break;
		case ServerStatus.SERVER_LIST_HIDE_NAME:
			Config.SERVER_BIT_3 = on; break;
		case ServerStatus.TEST_SERVER:
			Config.SERVER_BIT_4 = on; break;
		case ServerStatus.SERVER_LIST_BRACKETS:
			Config.SERVER_LIST_BRACKET = on; break;
		}
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(attrib, on);
		try { sendPacket(ss); }
		catch (IOException e) { if (_log.isDebugEnabled()) _log.debug(e.getMessage(), e); }
		ss = null;
	}

	public void sendServerStatus(int status)
	{
		ServerStatus ss = new ServerStatus();
		if (status == ServerStatus.STATUS_DOWN)
			ss.addServerDownAttribute();
		else
			ss.addAttribute(ServerStatus.SERVER_LIST_STATUS, status == 1);
		try { sendPacket(ss); }
		catch (IOException e) { if (_log.isDebugEnabled()) _log.debug(e.getMessage(), e); }
	}

	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}

	public int getGSStatus()
	{
		return _status;
	}

	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_BIT_2;
	}

	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}

	/**
	 * Sets server's status: ON, MAINTENANCE or OFF.<BR>
	 * While on-line server who's status is set to off forcedly kicks all non-GM players,
	 * maintenance doesn't enforce such strategy (e.g. random selection of testers on-line)<BR>
	 * You can always do OFF, then MAINTENANCE to ensure only GMs are online.
	 * @param status {@value ServerStatus#STATUS_AUTO}, {@value ServerStatus#STATUS_DOWN}, {@value ServerStatus#STATUS_GM_ONLY}
	 */
	public void setServerStatus(int status)
	{
		switch (status)
		{
		case ServerStatus.STATUS_AUTO:
			sendServerStatus(ServerStatus.STATUS_AUTO);
			_status = status;
			break;
		case ServerStatus.STATUS_DOWN:
			sendServerStatus(ServerStatus.STATUS_DOWN);
            if (!Shutdown.isInProgress())
            	kickPlayers();
			_status = status;
			break;
		case ServerStatus.STATUS_GM_ONLY:
			sendServerStatus(ServerStatus.STATUS_GM_ONLY);
			_status = status;
			break;
		}
	}

	public void kickPlayers()
	{
		int counter = 0;
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
            if (!player.isGM())
            {
                counter++;
                try
                {
                	new Disconnection(player).defaultSequence(true);
                }
                catch (Throwable t) {}
            }
		}
		_log.info(counter + " players were auto-kicked.");
	}

	public static class SessionKey
	{
		public int	playOkID1;
		public int	playOkID2;
		public int	loginOkID1;
		public int	loginOkID2;

		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}

		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}

	private class WaitingClient
	{
		public int			timestamp;
		public String		account;
		public L2GameClient	gameClient;
		public SessionKey	session;

		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}
}

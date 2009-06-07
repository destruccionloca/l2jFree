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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.loginserverthread.LoginServerThreadL2j;
import com.l2jfree.gameserver.loginserverthread.LoginServerThreadL2jfree;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jfree.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jfree.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.tools.security.NewCrypt;
import com.l2jfree.tools.util.HexUtil;

public abstract class LoginServerThread extends Thread
{
	protected static final Log _log = LogFactory.getLog(LoginServerThread.class);
	
	private static LoginServerThread _instance;
	
	public static LoginServerThread getInstance()
	{
		if (_instance == null)
		{
			if (Config.L2JFREE_LOGIN)
				_instance = new LoginServerThreadL2jfree();
			else
				_instance = new LoginServerThreadL2j();
		}
		
		return _instance;
	}
	
	public void stopInstance()
	{
		_instance.interrupt();
		_instance = null;
	}
	
	/** {@see com.l2jfree.loginserver.LoginServer#PROTOCOL_REV } */
	protected RSAPublicKey _publicKey;
	protected String _hostname;
	protected int _port;
	protected int _gamePort;
	protected Socket _loginSocket;
	protected InputStream _in;
	protected OutputStream _out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	protected NewCrypt _blowfish;
	protected byte[] _blowfishKey;
	protected byte[] _hexID;
	protected boolean _acceptAlternate;
	protected int _requestID;
	protected int _serverID;
	protected boolean _reserveHost;
	protected int _maxPlayer;
	protected final List<WaitingClient> _waitingClients;
	protected Map<String, L2GameClient> _accountsInGameServer;
	protected int _status;
	protected String _serverName;
	protected String _gameExternalHost; // External host for old login server
	protected String _gameInternalHost; // Internal host for old login server
	
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
	
	public boolean addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		synchronized (_waitingClients)
		{
			if (_waitingClients.size() == 10)
				return false;
			
			_waitingClients.add(new WaitingClient(acc, client, key));
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
		return true;
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
	
	protected String hexToString(byte[] hex)
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
	protected void sendPacket(GameServerBasePacket sl) throws IOException
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
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
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
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
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
	
	public class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}
	
	public abstract void setServerStatus(int status);
	
	public abstract void setServerStatusDown();
	
	public abstract int getMaxPlayer();
	
	public abstract void setMaxPlayers(int maxPlayer);
}

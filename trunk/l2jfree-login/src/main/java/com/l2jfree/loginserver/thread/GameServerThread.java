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
package com.l2jfree.loginserver.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2Config;
import com.l2jfree.loginserver.beans.GameServerInfo;
import com.l2jfree.loginserver.beans.SessionKey;
import com.l2jfree.loginserver.gameserverpackets.BlowFishKey;
import com.l2jfree.loginserver.gameserverpackets.ChangeAccessLevel;
import com.l2jfree.loginserver.gameserverpackets.GameServerAuth;
import com.l2jfree.loginserver.gameserverpackets.PlayerAuthRequest;
import com.l2jfree.loginserver.gameserverpackets.PlayerInGame;
import com.l2jfree.loginserver.gameserverpackets.PlayerLogout;
import com.l2jfree.loginserver.gameserverpackets.ServerStatus;
import com.l2jfree.loginserver.loginserverpackets.AuthResponse;
import com.l2jfree.loginserver.loginserverpackets.InitLS;
import com.l2jfree.loginserver.loginserverpackets.KickPlayer;
import com.l2jfree.loginserver.loginserverpackets.LoginServerFail;
import com.l2jfree.loginserver.loginserverpackets.LoginToGamePacket;
import com.l2jfree.loginserver.loginserverpackets.PlayerAuthResponse;
import com.l2jfree.loginserver.loginserverpackets.PlayerLoginAttempt;
import com.l2jfree.loginserver.manager.GameServerManager;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.network.LoginServerFailReason;
import com.l2jfree.status.Status;
import com.l2jfree.tools.network.SubNetHost;
import com.l2jfree.tools.security.NewCrypt;
import com.l2jfree.tools.util.HexUtil;

/**
 * @author -Wooden-
 *
 */
public class GameServerThread extends Thread
{
	private static final Log		_log					= LogFactory.getLog(GameServerThread.class);
	private final Socket			_connection;
	private InputStream				_in;
	private OutputStream			_out;
	private final RSAPublicKey		_publicKey;
	private final RSAPrivateKey		_privateKey;
	private NewCrypt				_blowfish;
	private byte[]					_blowfishKey;

	private final String			_connectionIp;

	private GameServerInfo			_gsi;
	private final List<SubNetHost>	_gameserverSubnets		= new FastList<SubNetHost>();

	private long					_lastIpUpdate;

	/** Authed Clients on a GameServer*/
	private final Set<String>		_accountsOnGameServer	= new FastSet<String>();

	private String					_connectionIpAddress;

	private int						_protocol;

	@Override
	public void run()
	{
		_connectionIpAddress = _connection.getInetAddress().getHostAddress();
		if (GameServerThread.isBannedGameserverIP(_connectionIpAddress))
		{
			_log.info("GameServerRegistration: IP Address " + _connectionIpAddress + " is on Banned IP list.");
			forceClose(LoginServerFailReason.REASON_IP_BANNED);
			// ensure no further processing for this connection
			return;
		}

		InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
		try
		{
			sendPacket(startPacket);

			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (;;)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = lengthHi * 256 + lengthLo;

				if (lengthHi < 0 || _connection.isClosed())
				{
					_log.info("LoginServerThread: Login terminated the connection.");
					break;
				}

				byte[] data = new byte[length - 2];

				int receivedBytes = 0;
				int newBytes = 0;
				while (newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes = _in.read(data, 0, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}

				if (receivedBytes != length - 2)
				{
					_log.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}

				// decrypt if we have a key
				data = _blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					_log.warn("Incorrect packet checksum, closing connection (LS)");
					return;
				}

				if (_log.isDebugEnabled())
					_log.debug("[C]\n" + HexUtil.printData(data));

				//_log.info("Incoming GameServer packet!");
				//_log.info("[C]\n" + HexUtil.printData(data));

				int packetType = data[0] & 0xff;
				switch (packetType)
				{
				case 00:
					onReceiveBlowfishKey(data);
					break;
				case 01:
					onGameServerAuth(data);
					break;
				case 02:
					onReceivePlayerInGame(data);
					break;
				case 03:
					onReceivePlayerLogOut(data);
					break;
				case 04:
					onReceiveChangeAccessLevel(data);
					break;
				case 05:
					onReceivePlayerAuthRequest(data);
					break;
				case 06:
					onReceiveServerStatus(data);
					break;
				case 0xAF:
					// trigger packet, shows that Game Server supports the actual protocol
					_protocol = L2Config.LOGIN_PROTOCOL_CURRENT;
					break;
				default:
					_log.warn("Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
					forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
				}
			}
		}
		catch (IOException e)
		{
			String serverName = (getServerId() != -1 ? "[" + getServerId() + "] " + GameServerManager.getInstance().getServerName(getServerId()) : "("
				+ _connectionIpAddress + ")");
			String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			_log.info(msg);
			broadcastToTelnet(msg);
		}
		finally
		{
			if (isAuthed())
			{
				_gsi.setDown();
				_log.info("Server [" + getServerId() + "] " + GameServerManager.getInstance().getServerName(getServerId()) + " is now set as disconnected");
			}
			GameServerListener.getInstance().removeGameServer(this);
			GameServerListener.getInstance().removeFloodProtection(_connectionIp);
		}
	}

	private void broadcastToTelnet(String msg)
	{
		Status.tryBroadcast(msg);
	}

	private void onReceiveBlowfishKey(byte[] data)
	{
		BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfishKey = bfk.getKey();
		_blowfish = new NewCrypt(_blowfishKey);
		if (_log.isDebugEnabled())
			_log.info("New BlowFish key received, Blowfish Engine initialized.");
	}

	private void onGameServerAuth(byte[] data) throws IOException
	{
		GameServerAuth gsa = new GameServerAuth(data);
		if (_log.isDebugEnabled())
			_log.info("Auth request received");
		handleRegProcess(gsa);
		if (isAuthed())
		{
			sendPacket(new AuthResponse(getGameServerInfo().getId()));
			if (_log.isDebugEnabled())
				_log.info("Authed: id: " + getGameServerInfo().getId());
			broadcastToTelnet("GameServer [" + getServerId() + "] " + GameServerManager.getInstance().getServerNameById(getServerId()) + " is connected");
		}
	}

	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			PlayerInGame pig = new PlayerInGame(data);
			String[] newAccounts = pig.getAccounts();
			for (String account : newAccounts)
			{
				_accountsOnGameServer.add(account);
				if (_log.isDebugEnabled())
					_log.info("Account " + account + " logged in GameServer: [" + getServerId() + "] "
							+ GameServerManager.getInstance().getServerNameById(getServerId()));

				broadcastToTelnet("Account " + account + " logged in GameServer " + getServerId());
			}

		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
			if (_log.isDebugEnabled())
				_log.info("Player " + plo.getAccount() + " logged out from gameserver [" + getServerId() + "] "
						+ GameServerManager.getInstance().getServerNameById(getServerId()));

			broadcastToTelnet("Player " + plo.getAccount() + " disconnected from GameServer " + getServerId());
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			ChangeAccessLevel cal = new ChangeAccessLevel(data);
			try
			{
				LoginManager.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
				_log.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
			}
			catch (Exception e)
			{
				_log.warn("Access level could not be changed. Reason: ", e);
			}
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceivePlayerAuthRequest(byte[] data) throws IOException
	{
		if (isAuthed())
		{
			PlayerAuthRequest par = new PlayerAuthRequest(data);
			PlayerAuthResponse authResponse;
			if (_log.isDebugEnabled())
				_log.info("auth request received for Player " + par.getAccount());
			SessionKey key = LoginManager.getInstance().getKeyForAccount(par.getAccount());
			String host = LoginManager.getInstance().getHostForAccount(par.getAccount());
			if (key != null && key.equals(par.getKey()))
			{
				if (_log.isDebugEnabled())
					_log.info("auth request: OK");
				LoginManager.getInstance().removeAuthedLoginClient(par.getAccount());
				authResponse = new PlayerAuthResponse(par.getAccount(), true, host);
			}
			else
			{
				if (_log.isDebugEnabled())
				{
					_log.info("auth request: NO");
					_log.info("session key from self: " + key);
					_log.info("session key sent: " + par.getKey());
				}
				authResponse = new PlayerAuthResponse(par.getAccount(), false, host);
			}
			sendPacket(authResponse);
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
		{
			if (_log.isDebugEnabled())
				_log.info("ServerStatus received");
			new ServerStatus(data, getServerId()); //will do the actions by itself
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void forceClose(LoginServerFailReason reason)
	{
		LoginServerFail lsf = new LoginServerFail(reason);
		try
		{
			sendPacket(lsf);
		}
		catch (IOException e)
		{
			_log.info("GameServerThread: Failed kicking banned server. Reason: ", e);
		}

		try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			//_log.info("GameServerThread: Failed disconnecting banned server, server already disconnected.");
		}
	}

	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		GameServerManager gameServerTable = GameServerManager.getInstance();

		int id = gameServerAuth.getDesiredID();
		byte[] hexId = gameServerAuth.getHexID();

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
						forceClose(LoginServerFailReason.REASON_ALREADY_LOGGED_IN);
					else
						attachGameServerInfo(gsi, gameServerAuth);
				}
			else // there is already a server registered with the desired id and different hex id
				// try to register this one with an alternative id
				if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
				{
					gsi = new GameServerInfo(id, hexId, this);
					if (gameServerTable.registerWithFirstAvailableId(gsi))
					{
						attachGameServerInfo(gsi, gameServerAuth);
						gameServerTable.registerServerOnDB(gsi);
					}
					else
						forceClose(LoginServerFailReason.REASON_NO_FREE_ID);
				}
				else
					// server id is already taken, and we cant get a new one for you
					forceClose(LoginServerFailReason.REASON_WRONG_HEXID);
		} else // can we register on this id?
			if (Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (gameServerTable.register(id, gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
					// some one took this ID meanwhile
					forceClose(LoginServerFailReason.REASON_ID_RESERVED);
			}
			else
				forceClose(LoginServerFailReason.REASON_WRONG_HEXID);
	}

	/**
	 * Attachs a GameServerInfo to this Thread
	 * <li>Updates the GameServerInfo values based on GameServerAuth packet</li>
	 * <li><b>Sets the GameServerInfo as Authed</b></li>
	 * @param gsi The GameServerInfo to be attached.
	 * @param gameServerAuth The server info.
	 */
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setNetConfig(gameServerAuth.getNetConfig());
		gsi.setIp(_connectionIp);

		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		_protocol = L2Config.LOGIN_PROTOCOL_L2J;
		
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		KeyPair pair = GameServerManager.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		start();
	}

	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(LoginToGamePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (_log.isDebugEnabled())
			_log.debug("[S] " + sl.getClass().getSimpleName() + ":\n" + HexUtil.printData(data));
		data = _blowfish.crypt(data);
		
		int len = data.length + 2;
		synchronized (_out)
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	public void kickPlayer(String account)
	{
		KickPlayer kp = new KickPlayer(account);
		try
		{
			sendPacket(kp);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}

	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}

	public void setNetConfig(String netConfig)
	{
		if (_gameserverSubnets.size() == 0)
		{
			StringTokenizer hostNets = new StringTokenizer(netConfig.trim(), ";");

			while (hostNets.hasMoreTokens())
			{
				String hostNet = hostNets.nextToken();

				StringTokenizer addresses = new StringTokenizer(hostNet.trim(), ",");

				String _host = addresses.nextToken();

				SubNetHost _subNetHost = new SubNetHost(_host);

				if (addresses.hasMoreTokens())
					while (addresses.hasMoreTokens())
						try
				{
							StringTokenizer netmask = new StringTokenizer(addresses.nextToken().trim(), "/");
							String _net = netmask.nextToken();
							String _mask = netmask.nextToken();

							_subNetHost.addSubNet(_net, _mask);
				}
				catch (NoSuchElementException c)
				{
					// Silence of the Lambs =)
				}
				else
					_subNetHost.addSubNet("0.0.0.0", "0");

				_gameserverSubnets.add(_subNetHost);
			}
		}

		updateIPs();
	}

	public void updateIPs()
	{
		_lastIpUpdate = System.currentTimeMillis();

		if (_gameserverSubnets.size() > 0)
		{
			_log.info("Updated Gameserver [" + getServerId() + "] " + GameServerManager.getInstance().getServerNameById(getServerId()) + " IP's:");

			for (SubNetHost _netConfig : _gameserverSubnets)
			{
				String _hostName = _netConfig.getHostname();
				try
				{
					String _hostAddress = InetAddress.getByName(_hostName).getHostAddress();
					_netConfig.setIp(_hostAddress);
					_log.info(!_hostName.equals(_hostAddress) ? _hostName + " (" + _hostAddress + ")" : _hostAddress);
				}
				catch (UnknownHostException e)
				{
					_log.warn("Couldn't resolve hostname \"" + _hostName + "\"");
				}
			}
		}
	}

	public String getIp(String ip)
	{
		String _host = null;

		if (Config.IP_UPDATE_TIME > 0 && (System.currentTimeMillis() > (_lastIpUpdate + Config.IP_UPDATE_TIME)))
			updateIPs();

		for (SubNetHost _netConfig : _gameserverSubnets)
			if (_netConfig.isInSubnet(ip))
			{
				_host = _netConfig.getIp();
				break;
			}
		if (_host == null)
			_host = ip;

		return _host;
	}

	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed()
	{
		if (getGameServerInfo() == null)
			return false;
		return getGameServerInfo().isAuthed();
	}

	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}

	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}

	/**
	 * @return Returns the connectionIpAddress.
	 */
	public String getConnectionIpAddress()
	{
		return _connectionIpAddress;
	}

	public int getServerId()
	{
		if (getGameServerInfo() != null)
			return getGameServerInfo().getId();
		return -1;
	}

	public void playerSelectedServer(String ip)
	{
		if (_protocol > 200 || _protocol < L2Config.LOGIN_PROTOCOL_CURRENT)
			return;
		try
		{
			sendPacket(new PlayerLoginAttempt(ip));
		}
		catch (IOException e)
		{
		}
	}
}

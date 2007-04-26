/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.loginserver.thread;

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
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.L2LoginServer;
import net.sf.l2j.loginserver.beans.GameServerInfo;
import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.gameserverpackets.BlowFishKey;
import net.sf.l2j.loginserver.gameserverpackets.ChangeAccessLevel;
import net.sf.l2j.loginserver.gameserverpackets.GameServerAuth;
import net.sf.l2j.loginserver.gameserverpackets.PlayerAuthRequest;
import net.sf.l2j.loginserver.gameserverpackets.PlayerInGame;
import net.sf.l2j.loginserver.gameserverpackets.PlayerLogout;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.loginserverpackets.AuthResponse;
import net.sf.l2j.loginserver.loginserverpackets.InitLS;
import net.sf.l2j.loginserver.loginserverpackets.KickPlayer;
import net.sf.l2j.loginserver.loginserverpackets.LoginServerFail;
import net.sf.l2j.loginserver.loginserverpackets.PlayerAuthResponse;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.manager.LoginManager;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;
import net.sf.l2j.tools.security.NewCrypt;
import net.sf.l2j.tools.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author -Wooden-
 *
 */

public class GameServerThread extends Thread
{
	private static final Log _log = LogFactory.getLog(GameServerThread.class.getName());
	private Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private RSAPublicKey _publicKey;
	private RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	
	private String _connectionIp;

	private GameServerInfo _gsi;
	
    /** Authed Clients on a GameServer*/
    private Set<String> _accountsOnGameServer = new FastSet<String>();
    
	private String _connectionIpAddress;

	
	
	private int _max_players;
	private List<String> _players;
	private int _server_id;
	private boolean _isTestServer;
	private boolean _PvpServer;
	private int _gamePort;
	private byte[] _hexID;
	private String _netConfig;
	

	/**
	 * @return Returns the hexID.
	 */
	public byte[] getHexID()
	{
		return _hexID;
	}
	
	public boolean isPlayerInGameServer(String account)
	{
		return _players.contains(account);
	}
	
	public void run()
	{
		_connectionIpAddress   = _connection.getInetAddress().getHostAddress();
        if (GameServerThread.isBannedGameserverIP(_connectionIpAddress))
        {
            _log.info("GameServerRegistration: IP Address " + _connectionIpAddress + " is on Banned IP list.");
            this.forceClose(LoginServerFail.REASON_IP_BANNED);
            // ensure no further processing for this connection
            return;
        }            
        
		InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
        try
        {
            this.sendPacket(startPacket);

            int lengthHi = 0;
            int lengthLo = 0;
            int length = 0;
            boolean checksumOk = false;
            for (;;)
            {
                lengthLo = _in.read();
                lengthHi = _in.read();
                length= lengthHi*256 + lengthLo;  

                if (lengthHi < 0 || _connection.isClosed())
                {
                    _log.info("LoginServerThread: Login terminated the connection.");
                    break;
                }

                byte[] data = new byte[length - 2];

                int receivedBytes = 0;
                int newBytes = 0;
                while (newBytes != -1 && receivedBytes<length-2)
                {
                    newBytes =  _in.read(data, 0, length-2);
                    receivedBytes = receivedBytes + newBytes;
                }

                if (receivedBytes != length-2)
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
                {
                    _log.debug("[C]\n"+Util.printData(data));
                }

                int packetType = data[0] & 0xff;
                switch (packetType)
                {
                    case 00:
                        this.onReceiveBlowfishKey(data);
                        break;
                    case 01:
                        this.onGameServerAuth(data);
                        break;
                    case 02:
                        this.onReceivePlayerInGame(data);
                        break;
                    case 03:
                        this.onReceivePlayerLogOut(data);
                        break;
                    case 04:
                        this.onReceiveChangeAccessLevel(data);
                        break;
                    case 05:
                        this.onReceivePlayerAuthRequest(data);
                        break;
                    case 06:
                        this.onReceiveServerStatus(data);
                        break;
                    default:
                        _log.warn("Unknown Opcode ("+Integer.toHexString(packetType).toUpperCase()+") from GameServer, closing connection.");
                        this.forceClose(LoginServerFail.NOT_AUTHED);
                }
                
            }
        }
        catch (IOException e)
        {
            String serverName = (this._server_id != -1 ? "["+_server_id+"] "+GameServerManager.getInstance().getServerName(_server_id) : "("+_connectionIpAddress+")");
            String msg = "GameServer "+serverName+": Connection lost: "+e.getMessage();
            _log.info(msg);
            this.broadcastToTelnet(msg);
        }
        finally
        {
            if (this.isAuthed())
            {
                _gsi.setDown();
                _log.info("Server ["+_server_id+"] "+GameServerManager.getInstance().getServerName(_server_id)+": Setted as disconnected");
            }
            L2LoginServer.getInstance().getGameServerListener().removeGameServer(this);
            L2LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
        }
	}
    
    private void broadcastToTelnet(String msg)
    {
        if (L2LoginServer.getInstance().getStatusServer() != null)
        {
            L2LoginServer.getInstance().getStatusServer().SendMessageToTelnets(msg);
        }
    }    
    
    private void onReceiveBlowfishKey(byte[] data)
    {
        /*if (_blowfish == null)
        {*/
        BlowFishKey bfk = new BlowFishKey(data,_privateKey);
        _blowfishKey = bfk.getKey();
        _blowfish = new NewCrypt(_blowfishKey);
        if (_log.isDebugEnabled())
        {
            _log.info("New BlowFish key received, Blowfih Engine initialized:");
        }
    }

    private void onGameServerAuth(byte[] data) throws IOException
    {
        GameServerAuth gsa = new GameServerAuth(data);
        if (_log.isDebugEnabled())
        {
            _log.info("Auth request received");
        }
        this.handleRegProcess(gsa);
        if (this.isAuthed())
        {
            AuthResponse ar = new AuthResponse(this.getGameServerInfo().getId());
            sendPacket(ar);
            if (_log.isDebugEnabled())
            {
                _log.info("Authed: id: "+this.getGameServerInfo().getId());
            }
            this.broadcastToTelnet("GameServer ["+getServerId()+"] "+GameServerManager.getInstance().getServerNameById(getServerId())+" is connected");
        }
    }

    private void onReceivePlayerInGame(byte[] data)
    {
        if (this.isAuthed())
        {
            PlayerInGame pig = new PlayerInGame(data);
            List<String> newAccounts = pig.getAccounts();
            for (String account : newAccounts)
            {
                _accountsOnGameServer.add(account);
                if (_log.isDebugEnabled())
                {
                    _log.info("Account "+account+" logged in GameServer: ["+getServerId()+"] "+GameServerManager.getInstance().getServerNameById(getServerId()));
                }
                
                this.broadcastToTelnet("Account "+account+" logged in GameServer "+getServerId());
            }

        }
        else
        {
            this.forceClose(LoginServerFail.NOT_AUTHED);
        }
    }

    private void onReceivePlayerLogOut(byte[] data)
    {
        if (this.isAuthed())
        {
            PlayerLogout plo = new PlayerLogout(data);
            _accountsOnGameServer.remove(plo.getAccount());
            if (_log.isDebugEnabled())
            {
                _log.info("Player "+plo.getAccount()+" logged out from gameserver ["+getServerId()+"] "+GameServerManager.getInstance().getServerNameById(getServerId()));
            }
            
            this.broadcastToTelnet("Player "+plo.getAccount()+" disconnected from GameServer "+getServerId());
        }
        else
        {
            this.forceClose(LoginServerFail.NOT_AUTHED);
        }
    }

    private void onReceiveChangeAccessLevel(byte[] data)
    {
        if (this.isAuthed())
        {
            ChangeAccessLevel cal = new ChangeAccessLevel(data);
            LoginManager.getInstance().setAccountAccessLevel(cal.getAccount(),cal.getLevel());
            _log.info("Changed "+cal.getAccount()+" access level to "+cal.getLevel());
        }
        else
        {
            this.forceClose(LoginServerFail.NOT_AUTHED);
        }
    }

    private void onReceivePlayerAuthRequest(byte[] data) throws IOException
    {
        if (this.isAuthed())
        {
            PlayerAuthRequest par = new PlayerAuthRequest(data);
            PlayerAuthResponse authResponse;
            if (_log.isDebugEnabled())
            {
                _log.info("auth request received for Player "+par.getAccount());
            }
            SessionKey key = LoginManager.getInstance().getKeyForAccount(par.getAccount());
            if (key != null && key.equals(par.getKey()))
            {
                if (_log.isDebugEnabled())
                {
                    _log.info("auth request: OK");
                }
                LoginManager.getInstance().removeAuthedLoginClient(par.getAccount());
                authResponse = new PlayerAuthResponse(par.getAccount(), true);
            }
            else
            {
                if (_log.isDebugEnabled())
                {
                    _log.info("auth request: NO");
                    _log.info("session key from self: "+key);
                    _log.info("session key sent: "+par.getKey());
                }
                authResponse = new PlayerAuthResponse(par.getAccount(), false);
            }
            this.sendPacket(authResponse);
        }
        else
        {
            this.forceClose(LoginServerFail.NOT_AUTHED);
        }
    }

    private void onReceiveServerStatus(byte[] data)
    {
        if (this.isAuthed())
        {
            if (_log.isDebugEnabled())
            {
                _log.info("ServerStatus received");
            }
            @SuppressWarnings("unused")
            ServerStatus ss = new ServerStatus(data,getServerId()); //will do the actions by itself
        }
        else
        {
            this.forceClose(LoginServerFail.NOT_AUTHED);
        }
    }    
	
    private void forceClose(int reason)
    {
        LoginServerFail lsf = new LoginServerFail(reason);
        try
        {
            this.sendPacket(lsf);
        }
        catch (IOException e)
        {
            _log.info("GameServerThread: Failed kicking banned server. Reason: "+e.getMessage());
        }

        try
        {
            _connection.close();
        }
        catch (IOException e)
        {
            _log.info("GameServerThread: Failed disconnecting banned server, server already disconnected.");
        }
    }    
    
//	/**
//	 * @param hexID
//	 */
//	private void handleRegisterationProcess(GameServerAuth gameServerauth)
//	{
//		try
//		{
//			GameServerManager gsm = GameServerManager.getInstance();
//			if(gsm.isARegisteredServer(gameServerauth.getHexID()))
//			{
//                if (_log.isDebugEnabled())_log.debug("Valid HexID");
//				_server_id = gsm.getServerIDforHex(gameServerauth.getHexID());
//				if(gsm.isServerAuthed(_server_id))
//				{
//					LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_ALREADY_LOGGED_IN);
//					sendPacket(lsf);
//					_connection.close();
//					return;
//				}
//				_gamePort = gameServerauth.getPort();
//				_max_players = gameServerauth.getMax_palyers();
//				_hexID = gameServerauth.getHexID();
//				_netConfig = gameServerauth.getNetConfig();
//				gsm.addServer(this);
//				gsm.updateIP(_server_id);
//			}
//			else if(Config.ACCEPT_NEW_GAMESERVER)
//			{
//                if (_log.isDebugEnabled())_log.debug("New HexID");
//				if(!gameServerauth.acceptAlternateID())
//				{
//					if(gsm.isIDfree(gameServerauth.getDesiredID()))
//					{
//                        if (_log.isDebugEnabled())_log.debug("Desired ID is Valid");
//						_server_id = gameServerauth.getDesiredID();
//						_gamePort = gameServerauth.getPort();
//						_max_players = gameServerauth.getMax_palyers();
//						_hexID = gameServerauth.getHexID();
//						_netConfig = gameServerauth.getNetConfig();
//						gsm.createServer(this);
//						gsm.addServer(this);
//						gsm.updateIP(_server_id);
//					}
//					else
//					{
//						LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_ID_RESERVED);
//						sendPacket(lsf);
//						_connection.close();
//						return;
//					}
//				}
//				else
//				{
//					int id;
//					if(!gsm.isIDfree(gameServerauth.getDesiredID()))
//					{
//						id = gsm.findFreeID();
//                        if (_log.isDebugEnabled())_log.debug("Affected New ID:"+id);
//						if(id < 0)
//						{
//							LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_NO_FREE_ID);
//							sendPacket(lsf);
//							_connection.close();
//							return;
//						}
//					}
//					else
//					{
//						id = gameServerauth.getDesiredID();
//                        if (_log.isDebugEnabled())_log.debug("Desired ID is Valid");
//					}
//					_server_id = id;
//					_gamePort = gameServerauth.getPort();
//					_max_players = gameServerauth.getMax_palyers();
//					_hexID = gameServerauth.getHexID();
//					_netConfig = gameServerauth.getNetConfig();
//					gsm.createServer(this);
//					gsm.addServer(this);
//					gsm.updateIP(_server_id);
//				}
//			}
//			else
//			{
//				_log.info("Wrong HexID");
//				LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_WRONG_HEXID);
//				sendPacket(lsf);
//				_connection.close();
//				return;
//			}
//			
//		}
//		catch (IOException e)
//		{
//			_log.info("Error while registering GameServer "+GameServerManager.getInstance().getServerName(_server_id)+" (ID:"+_server_id+")");
//		}
//	}
    
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
            {
                // check to see if this GS is already connected
                synchronized (gsi)
                {
                    if (gsi.isAuthed())
                    {
                        this.forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
                    }
                    else
                    {
                        this.attachGameServerInfo(gsi, gameServerAuth);
                    }
                }
            }
            else
            {
                // there is already a server registered with the desired id and different hex id
                // try to register this one with an alternative id
                if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
                {
                    gsi = new GameServerInfo(id, hexId, this);
                    if (gameServerTable.registerWithFirstAvailableId(gsi))
                    {
                        this.attachGameServerInfo(gsi, gameServerAuth);
                        gameServerTable.registerServerOnDB(gsi);
                    }
                    else
                    {
                        this.forceClose(LoginServerFail.REASON_NO_FREE_ID);
                    }
                }
                else
                {
                    // server id is already taken, and we cant get a new one for you
                    this.forceClose(LoginServerFail.REASON_WRONG_HEXID);
                }
            }
        }
        else
        {
            // can we register on this id?
            if (Config.ACCEPT_NEW_GAMESERVER)
            {
                gsi = new GameServerInfo(id, hexId, this);
                if (gameServerTable.register(id, gsi))
                {
                    this.attachGameServerInfo(gsi, gameServerAuth);
                    gameServerTable.registerServerOnDB(gsi);
                }
                else
                {
                    // some one took this ID meanwhile
                    this.forceClose(LoginServerFail.REASON_ID_RESERVED);
                }
            }
            else
            {
                this.forceClose(LoginServerFail.REASON_WRONG_HEXID);
            }
        }
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
		this.setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}	
	/**
	 * @param ipAddress
	 * @return
	 */
	public static boolean isBannedGameserverIP(@SuppressWarnings("unused") String ipAddress)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
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
		_players = new FastList<String>();
		start();
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(ServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (_log.isDebugEnabled())
		{
			_log.debug("[S] "+sl.getClass().getSimpleName()+":\n"+Util.printData(data));
		}
		data = _blowfish.crypt(data);

		int len = data.length+2;
		synchronized(_out)
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 &0xff);
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
	
	/**
	 * @return Returns the max_players.
	 */
	public int getMaxPlayers()
	{
		return _max_players;
	}
	
	/**
	 * @return Returns the current_players.
	 */
	public int getCurrentPlayers()
	{
		return _players.size();
	}
	
	/**
	 * @return Returns the server_id.
	 */
	public int getServerID()
	{
		return _server_id;
	}
	
	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}
	
	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}	
	
	/**
	 * @return Returns the networks config string.
	 */
	public String getNetConfig()
	{
		return _netConfig;
	}
	
	/**
	 * @return
	 */
	public int getPort()
	{
		return _gamePort;
	}
	
	/**
	 * @return
	 */
	public boolean getPvP()
	{
		return _PvpServer;
	}
	
	/**
	 * @return
	 */
	public boolean isTestServer()
	{
		return _isTestServer;
	}
	
	/**
	 * @param gameHost The gameHost to set.
	 */
	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		String oldInternal = _gsi.getInternalHost();
		String oldExternal = _gsi.getExternalHost();
		
		_gsi.setExternalHost(gameExternalHost);
		_gsi.setInternalIp(gameInternalHost);
		
		if (!gameExternalHost.equals("*"))
		{
			try
			{
				_gsi.setExternalIp(InetAddress.getByName(gameExternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warn("Couldn't resolve hostname \""+gameExternalHost+"\"");
			}
		}
		else
		{
			_gsi.setExternalIp(_connectionIp);
		}
		if(!gameInternalHost.equals("*"))
		{
			try
			{
				_gsi.setInternalIp(InetAddress.getByName(gameInternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warn("Couldn't resolve hostname \""+gameInternalHost+"\"");
			}
		}
		else
		{
			_gsi.setInternalIp(_connectionIp);
		}
		
		_log.info("Updated Gameserver ["+getServerId()+"] "+GameServerManager.getInstance().getServerNameById(getServerId())+" IP's:");
		if (oldInternal == null || !oldInternal.equalsIgnoreCase(gameInternalHost))
			_log.info("InternalIP: "+gameInternalHost);
		if (oldExternal == null || !oldExternal.equalsIgnoreCase(gameExternalHost))
			_log.info("ExternalIP: "+gameExternalHost);
	}	
	
	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed()
	{
		if (this.getGameServerInfo() == null)
			return false;
		return this.getGameServerInfo().isAuthed();
	}
	
	/**
	 * @param value
	 */
	public void setMaxPlayers(int value)
	{
		_max_players = value;
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

	private int getServerId()
	{
		if (this.getGameServerInfo() != null)
		{
			return this.getGameServerInfo().getId();
		}
		return -1;
	}
}
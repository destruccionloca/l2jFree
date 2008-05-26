/*
 * $Header: GameServerListener.java, 14-Jul-2005 03:26:20 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 14-Jul-2005 03:26:20 $
 * $Revision: 1 $
 * $Log: GameServerListener.java,v $
 * Revision 1  14-Jul-2005 03:26:20  luisantonioa
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.loginserver.manager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.beans.GameServer;
import net.sf.l2j.loginserver.beans.Gameservers;
import net.sf.l2j.loginserver.beans.GameServer.GameServerNetConfig;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.serverpackets.ServerList;
import net.sf.l2j.loginserver.services.GameserversServices;
import net.sf.l2j.loginserver.thread.GameServerThread;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.util.HexUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

/**
 * Manager servers
 * Servers come from server.xml file and database. 
 * For each server in database, an instance of Gameserver is launch. The manager controls each gameserver threads.
 */
public class GameServerManager
{
    private static final Log _log = LogFactory.getLog(GameServerManager.class);

    private GameserversServices _gsServices = null;
    private GameserversServices _gsServicesXml = null;

    private static GameServerManager __instance = null;

    private List<GameServer> _gameServerList = new FastList<GameServer>();;
    private long _last_IP_Update;
    private KeyPair[] _keyPairs;
    private KeyPairGenerator _keyGen;
    private Random _rnd;

    /**
     * Return singleton
     * @return  GameServerManager
     */
    public static GameServerManager getInstance()
    {
        if (__instance == null)
        {
            __instance = new GameServerManager();
        }
        return __instance;
    }

    /**
     * Initialize keypairs
     * Initialize servers list from xml and db
     */
    private GameServerManager()
    {
        // o Load DAO 
        // ---------
        _gsServices = (GameserversServices) L2Registry.getBean("GameserversServices");
        _gsServicesXml = (GameserversServices) L2Registry.getBean("GameserversServicesXml");

        // o Load Servers
        // --------------
        load();

        // o Initialize last ip update time
        // --------------------------------
        _last_IP_Update = System.currentTimeMillis();

        // o Generate keypairs
        // -------------------
        try
        {
            _keyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
            _keyGen.initialize(spec);
        }
        catch (GeneralSecurityException e)
        {
            _log.warn(e.getMessage(), e);
        }
        _keyPairs = new KeyPair[10];
        for (int i = 0; i < 10; i++)
        {
            _keyPairs[i] = _keyGen.generateKeyPair();
        }
        _log.info("Stored 10 Keypairs for gameserver communication");
        _rnd = new Random();
    }

    /**
     * Stop each gameserver Thread
     *
     */
    public void shutDown()
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.gst != null) gs.gst.interrupt();
        }
    }

    /**
     * Load Gameserver from DAO
     * For each gameserver, instantiate a GameServer, (a container that hold a thread)
     */
    private void load()
    {
        List<Gameservers> listGs = _gsServices.getAllGameservers();
        Iterator<Gameservers> it = listGs.iterator();
        int id = 0;
        int previousID = 0;
        while (it.hasNext())
        {
            Gameservers gsFromDAO = it.next();
            id = gsFromDAO.getServerId();
            for (int i = 1; id - i > previousID; i++) //fill with dummy servers to keep
            {
                GameServer gs = new GameServer(previousID + i);
                _gameServerList.add(gs);
            }
            GameServer gs = new GameServer(HexUtil.stringToHex(gsFromDAO.getHexid()), id);
            _gameServerList.add(gs);
            previousID = id;
        }
        _log.info("GameServerManager: Loaded " + listGs.size() + " servers (max id:" + id + ")");
    }

    public void setServerReallyDown(int id)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == id)
            {
                gs.ip = null;
                gs.netConfig = null;
                gs.port = 0;
                gs.gst = null;
            }
        }
    }

    public GameServerThread getGameServerThread(int ServerID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == ServerID)
            {
                return gs.gst;
            }
        }
        return null;
    }

    public int getGameServerStatus(int ServerID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == ServerID)
            {
                return gs.status;
            }
        }
        return -1;
    }

    /**
     * 
     * @param gst
     */
    public void addServer(GameServerThread gst)
    {
        GameServer gameServer = new GameServer(gst);
        GameServer toReplace = null;

        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == gst.getServerID())
            {
                toReplace = gs;
            }
        }
        if (toReplace != null)
        {
            _gameServerList.remove(toReplace);
        }
        _gameServerList.add(gameServer);
        orderList();
        if (_log.isDebugEnabled())
        {
            for (GameServer gs : _gameServerList)
            {
                _log.debug(gs.toString());
            }
        }
        gst.setAuthed(true);
    }

    /**
     * 
     * @param hex
     * @return
     */
    public int getServerIDforHex(byte[] hex)
    {
        for (GameServer gs : _gameServerList)
        {
            if (Arrays.equals(hex, gs.hexID)) return gs.server_id;
        }
        return -1;
    }

    /**
     * 
     * @param id
     * @return
     */
    public boolean isIDfree(int id)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == id && gs.hexID != null) return false;
        }
        return true;
    }

    /**
     * 
     * @param gs
     */
    public void createServer(GameServer gs)
    {
        try
        {
            Gameservers gameserver = new Gameservers();
            gameserver.setHexid(HexUtil.hexToString(gs.hexID));
            gameserver.setServerId(gs.server_id);
            if (gs.gst != null)
            {
                gameserver.setHost(gs.gst.getConnectionIpAddress());
            }
            else
            {
                gameserver.setHost("*");
            }
            _gsServices.createGameserver(gameserver);
        }
        catch (DataAccessException e)
        {
            _log.warn("Error while saving gameserver :" + e, e);
        }
    }

    /**
     * 
     * @param hex
     * @return
     */
    public boolean isARegisteredServer(byte[] hex)
    {
        for (GameServer gs : _gameServerList)
        {
            if (Arrays.equals(hex, gs.hexID)) return true;
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public int findFreeID()
    {
        for (int i = 0; i < 127; i++)
        {
            if (isIDfree(i)) return i;
        }
        return -1;
    }

    /**
     * 
     * @param id - the server id
     */
    public void deleteServer(int id)
    {
        _gsServices.deleteGameserver(id);
    }

    /**
     * 
     * @param isGM
     * @param ip
     * @return
     */
    public ServerList makeServerList(boolean isGM, String ip)
    {
        orderList();
        ServerList sl = new ServerList();
        boolean updated = false;
        for(GameServer gs : _gameServerList)
        {
            if(_log.isDebugEnabled())
                _log.debug("Updtime:"+Config.IP_UPDATE_TIME+" , current:"+_last_IP_Update+" so:"+(System.currentTimeMillis() - _last_IP_Update));
            if(System.currentTimeMillis() - _last_IP_Update > Config.IP_UPDATE_TIME * 1000 * 60 && Config.IP_UPDATE_TIME != 0)
            {
                if(gs.gst != null)
                {
                	updateIP(gs.server_id);
                    updated = true;
                }
            }
            String gs_ip = null; 
            if (gs.gst!=null) gs_ip = gs.getIp(ip);
            int status = gs.status;
            if(status == ServerStatus.STATUS_AUTO)
            {
            	if(gs_ip == null)
                    {
                        status = ServerStatus.STATUS_DOWN;
                    }
            }
            else if(status == ServerStatus.STATUS_GM_ONLY)
                {
                    if(!isGM)
                    {
                        status = ServerStatus.STATUS_DOWN;
                    }
                    else
                    {
                        if(gs_ip == null)
                        {
                            status = ServerStatus.STATUS_DOWN;
                        }
                    }
                }
                sl.addServer(gs_ip,gs.port,gs.pvp,gs.testServer,(gs.gst == null ? 0 : gs.gst.getCurrentPlayers()),gs.maxPlayers,gs.brackets,gs.clock,status,gs.server_id);
         }
        if(updated)
        {
            _last_IP_Update = System.currentTimeMillis();
        }
        
        return sl;
    }


    /**
     * 
     */
    private void orderList()
    {
        Collections.sort(_gameServerList, gsComparator);
    }

    private static final Comparator<GameServer> gsComparator = new Comparator<GameServer>() {
        public int compare(GameServer gs1, GameServer gs2)
        {
            return (gs1.server_id < gs2.server_id ? -1 : gs1.server_id == gs2.server_id ? 0 : 1);
        }
    };

    /**
     * @param thread
     */
    public void createServer(GameServerThread thread)
    {
        try
        {
            Gameservers gs = new Gameservers();
            gs.setHexid(HexUtil.hexToString(thread.getHexID()));
            gs.setHost(thread.getConnectionIpAddress());
            gs.setServerId(thread.getServerID());
            _gsServices.createGameserver(gs);
        }
        catch (DataAccessException e)
        {
            _log.warn("Error while saving gameserver :" + e, e);
        }
    }

    /**
     * @param value
     * @param serverID
     */
    public void setMaxPlayers(int value, int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                gs.maxPlayers = value;
                gs.gst.setMaxPlayers(value);
            }
        }
    }

    /**
     * @param b
     * @param serverID
     */
    public void setBracket(boolean b, int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                gs.brackets = b;
            }
        }
    }

    /**
     * @param b
     * @param serverID
     */
    public void setClock(boolean b, int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                gs.clock = b;
            }
        }
    }

    /**
     * @param b
     * @param serverID
     */
    public void setTestServer(boolean b, int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                gs.testServer = b;
            }
        }
    }

    /**
     * @param value
     * @param serverID
     */
    public void setStatus(int value, int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                gs.status = value;
                if (_log.isDebugEnabled())_log.debug("Status Changed for server " + serverID);
            }
        }
    }

    /**
     * 
     * @param serverID
     * @return
     */
    public boolean isServerAuthed(int serverID)
    {
        for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                if (gs.ip != null && gs.gst != null && gs.gst.isAuthed())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public List<String> status()
    {
        List<String> str = new ArrayList<String>();
        str.add("There are " + _gameServerList.size() + " GameServers");
        for (GameServer gs : _gameServerList)
        {
            str.add(gs.toString());
        }
        return str;
    }

    /**
     * 
     * @return
     */
    public KeyPair getKeyPair()
    {
        return _keyPairs[_rnd.nextInt(10)];
    }

    /**
     * 
     * @param id
     * @return
     */
    public String getServerName(int id)
    {
        return _gsServicesXml.getGameserverName(id);
    }

    /**
     * 
     * @param id
     * @return
     */
    public List<Gameservers> getServers()
    {
        return _gsServicesXml.getAllGameservers();
    }

    /**
     * 
     * @param id
     * @return
     */
    public void updateIP(int serverID)
    {
    	for (GameServer gs : _gameServerList)
        {
            if (gs.server_id == serverID)
            {
                if (gs.ip != null && gs.gst != null )
                {
                	_log.info("Updated Gameserver "+getServerName(serverID)+ "("+serverID+") IP's:");
                	for (GameServerNetConfig _netConfig : gs.gsNetConfig )
                	{
                		String _hostName = _netConfig.getHost();
                		try
                		{
                			String _hostAddress = InetAddress.getByName(_hostName).getHostAddress();
                			_netConfig.setIp(_hostAddress);
                			_log.info(!_hostName.equals(_hostAddress)?_hostName+" ("+_hostAddress+")":_hostAddress);
                		}
                		catch (UnknownHostException e)
                		{
                			_log.warn("Couldn't resolve hostname \""+_hostName+"\"");
                		}
                	}
                }
            }
        }
    }
}

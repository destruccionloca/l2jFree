/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
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
package net.sf.l2j.loginserver.beans;

import java.math.BigInteger;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javolution.util.FastList;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.thread.GameServerThread;
import net.sf.l2j.tools.network.Net;

/**
 *  
 */
public class GameServer
{
    public String name=null;
    public int server_id;
    public int port;
    public boolean pvp = true;
    public boolean testServer = false;
    public int maxPlayers;
    public byte[] hexID;
    public GameServerThread gst;
    public boolean brackets = false;
    public boolean clock = false;
    public int status = ServerStatus.STATUS_AUTO;
    public String ip=null;
    public String netConfig=null;
    public FastList<GameServerNetConfig> gsNetConfig = new  FastList<GameServerNetConfig>();

    
    public GameServer(GameServerThread gamest)
    {
        gst = gamest;
        port = gst.getPort();
        pvp = gst.getPvP();
        testServer = gst.isTestServer();
        maxPlayers = gst.getMaxPlayers();
        hexID = gst.getHexID();
        server_id = gst.getServerID();
        netConfig = gst.getNetConfig();
        ip = gst.getConnectionIpAddress();
        setNetConfig();
    }
    
    public String toString()
    {
        return "GameServer: "+GameServerManager.getInstance().getServerName(server_id)+" id:"+server_id+" hex:"+hexToString(hexID)+" ip:"+ip+":"+port+" status: "+ServerStatus.statusString[status];
    }
    
    private String hexToString(byte[] hex)
    {
        if(hex == null)
            return "null";
        return new BigInteger(hex).toString(16);
    }
    
    public GameServer(byte[] hex, int id)
    {
        hexID = hex;
        server_id = id;
    }
    
    public GameServer(int id)
    {
        server_id = id;
    }

	public void setNetConfig()
	{
		if (gsNetConfig.size() == 0)
		{
			StringTokenizer hostNets = new StringTokenizer(netConfig.trim(),";");	
			
			while (hostNets.hasMoreTokens())
			{
				String hostNet=hostNets.nextToken();
				
				StringTokenizer addresses = new StringTokenizer(hostNet.trim(),",");
		
				String _host = addresses.nextToken();

				GameServerNetConfig _NetConfig = new GameServerNetConfig(_host); 
				
				if (addresses.hasMoreTokens())
				{
				while (addresses.hasMoreTokens())
				{
					try
					{
						StringTokenizer netmask = new StringTokenizer(addresses.nextToken().trim(),"/");
						String _net = netmask.nextToken();
						String _mask = netmask.nextToken();

						_NetConfig.addNet(_net,_mask);
					}
					catch (NoSuchElementException c)
					{
					}
				}
				}else
					_NetConfig.addNet("0.0.0.0","0"); // Any address

				gsNetConfig.add(_NetConfig);
					
			}
		}
	}		

	public String getIp()
	{
		return ip;
	}
	
	public String getIp(String ip)
	{
		String _host = null;
		
		for (GameServerNetConfig _netConfig : gsNetConfig )
		{
			if (_netConfig.checkHost(ip))
			{
				_host = _netConfig.getIp();
				break;
			}
		}
		if (_host == null) _host = ip;
		return _host;
	}

	public class GameServerNetConfig
	{
	    private String _hostName;
	    private String _hostAddress;
	    private List<Net> _nets = new FastList<Net>();
	    
	    public GameServerNetConfig(String hostName)
	    {
	    	_hostName = hostName;
	    }
	    
	    public void addNet(String net, String mask)
	    {
	    	Net _net = new Net(net,mask);
	    	if (_net!=null)
	    	_nets.add(_net);
	    }
	    
	    public boolean checkHost(String _ip)
	    {
	    	boolean _rightHost=false;
	    	for(Net net: _nets) if (net.isInNet(_ip)) { _rightHost=true; break;}
	    	return _rightHost;
	    }
	    public String getHost()
	    {
	    	return _hostName;
	    }
	    
	    public void setHost(String hostName)
	    {
	    	_hostName=hostName;
	    }	    
	    public String getIp()
	    {
	    	return _hostAddress;
	    }
	    
	    public void setIp(String hostAddress)
	    {
	    	_hostAddress=hostAddress;
	    }
}
}
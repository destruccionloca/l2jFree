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

import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.thread.GameServerThread;

/**
 *  
 */
public class GameServer
{
    public String name=null;
    public String ip;
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
    public String internal_ip;
    
    public GameServer(GameServerThread gamest)
    {
        gst = gamest;
        ip = gst.getGameExternalIP();
        port = gst.getPort();
        pvp = gst.getPvP();
        testServer = gst.isTestServer();
        maxPlayers = gst.getMaxPlayers();
        hexID = gst.getHexID();
        server_id = gst.getServerID();
        internal_ip = gst.getGameInternalIP();
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
}
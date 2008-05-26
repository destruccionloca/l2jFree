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
package net.sf.l2j.loginserver.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.tools.network.Net;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manage ban list
 * 
 */
public class BanManager
{
    private static BanManager _instance = null;
    private static final Log _log = LogFactory.getLog(BanManager.class);
    private static List<Net> _bannedIPs = new FastList<Net>();
    public static String BAN_LIST = "config/banned_ip.cfg";
    private static final String ENCODING = "UTF-8";
    
    /**
     * return singleton for banmanager
     * @return BanManager instance
     */
    public static BanManager getInstance()
    {
        if (_instance == null)
        {
            _instance = new BanManager();
            return _instance;
        }
        else return _instance;
    }
    
    public void addBannedIP(String ip, int incorrectCount)
    {
        _bannedIPs.add(new Net(ip));
        int time = incorrectCount * incorrectCount * 1000;
        _log.info("Banning ip "+ip+" for "+time/1000.0+" seconds.");
        ThreadPoolManager.getInstance().scheduleGeneral(new UnbanTask(ip), time);
    }    
    
    /**
     * 
     * @param ip
     */
    private void addBannedIP(String ip)
    {
        _bannedIPs.add(new Net(ip));
    }
    
    /**
     * Remove banned ip or network
     * @param ip
     */
    public void unBanIP(String ip)
    {
    	Net _unban = new Net(ip);
    	for (Net _net : _bannedIPs)
        	if(_net.equal(_unban)) _bannedIPs.remove(_net);
    }
    
    /**
     * Remove all ip from banned list (in memory, not in file)
     *
     */
    public void purgeBanlist ()
    {
        _bannedIPs.clear();
    }
    
    private BanManager()
    {
    	load();
    }
    /**
     * Load banned list
     *
     */
    public  void load()
    {
        try
        {
        	_bannedIPs.clear();
            // try to read banned list
            File file = new File(BAN_LIST);
            List lines = FileUtils.readLines(file, ENCODING);            
            
            for (int i = 0 ; i< lines.size();i++)
            {
                String line = (String)lines.get(i);
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#"))
                {
                    addBannedIP(line);
                }
            }
            _log.info("BanManager: Loaded " + getNbOfBannedIp () + " banned ip/subnet(s).");
        }
        catch (IOException e)
        {
            _log.warn("error while reading banned file:" + e);
        }
    }
    
    /**
     * 
     * @return number of ip banned
     */
    public int getNbOfBannedIp ()
    {
       return _bannedIPs.size(); 
    }
    
    
    /**
     * Check if ip is in banned list
     * @param ip
     * @return true or false if ip is banned or not
     */
    public boolean isIpBanned (String ip)
    {
    	boolean _isBanned = false;
    	
        for (Net _net : _bannedIPs)
        	if(_net.isInNet(ip)) _isBanned = true;
        
        return _isBanned;
    }
    

    /**
     * 
     * This runnable manage unban task for an ip
     * 
     */
    private class UnbanTask implements Runnable
    {
    	String _ip;
        public UnbanTask(String ip)
        {
        	_ip = ip;
        	addBannedIP(ip);
        }
        public void run()
        {
            BanManager.getInstance().unBanIP(_ip);
        }
        
    }    
}

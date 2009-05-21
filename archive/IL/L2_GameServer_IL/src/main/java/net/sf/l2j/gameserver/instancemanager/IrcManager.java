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
package net.sf.l2j.gameserver.instancemanager;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2IrcClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/** 
 * @author evill33t
 * 
 */
public class IrcManager
{
    private static final Log _log = LogFactory.getLog(IrcManager.class.getName());

    // =========================================================
    private static IrcManager _instance;
    private static L2IrcClient _ircConnection;
    
    public static final IrcManager getInstance()
    {
        if (_instance == null)
        {
            _log.info("Initializing IrcManager");
            _instance = new IrcManager();
            _instance.load();
        }
        return _instance;
    }
    // =========================================================
    
    // =========================================================
    // Method - Public
    public void reload()
    {
    	_ircConnection.disconnect();
    	try
    	{
    		_ircConnection.connect();
		} 
    	catch (Exception e) 
    	{ 
			_log.fatal(e);
		}
    }

    public L2IrcClient getConnection()
    {
    	return _ircConnection;
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
		_ircConnection = new L2IrcClient(Config.IRC_SERVER, Config.IRC_PORT, Config.IRC_PASS, Config.IRC_NICK, Config.IRC_USER, Config.IRC_NAME, Config.IRC_SSL, Config.IRC_CHANNEL);    	
    	try
    	{
    		_ircConnection.connect();
		} 
    	catch (Exception e) 
    	{ 
			_log.fatal(e);
		}
    }
}
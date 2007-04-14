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
package net.sf.l2j.gameserver.handler;

import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private final static Log _log = LogFactory.getLog(AdminCommandHandler.class.getName());
	
	private static AdminCommandHandler _instance;
	
	private FastMap<String, IAdminCommandHandler> _datatable;
    
	public static AdminCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AdminCommandHandler();
		}
		return _instance;
	}
	
	private AdminCommandHandler()
	{
		_datatable = new FastMap<String, IAdminCommandHandler>();
	}
	
	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (int i = 0; i < ids.length; i++)
		{
			if (_log.isDebugEnabled()) _log.debug("Adding handler for command "+ids[i]);
			
			if (_datatable.keySet().contains(new String(ids[i])))
			{
				_log.warn("Duplicated command \""+ids[i]+"\" definition in "+ handler.getClass().getName()+".");
			} else
				_datatable.put(new String(ids[i]), handler);
			
			if (Config.ALT_PRIVILEGES_ADMIN && !Config.GM_COMMAND_PRIVILEGES.containsKey(ids[i]))
				_log.warn("Command \""+ids[i]+"\" have no access level definition. Can't be used.");
		}
	}
	
	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1) {
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		if (_log.isDebugEnabled())
			_log.debug("getting handler for command: "+command+
					" -> "+(_datatable.get(new String(command)) != null));
		return _datatable.get(command);
	}

    /**
     * @return
     */
    public int size()
    {
    	return _datatable.size();
    }
    
    public void checkDeprecated()
    {
    	if (Config.ALT_PRIVILEGES_ADMIN)
    		for (Object cmd : Config.GM_COMMAND_PRIVILEGES.keySet())
    		{
    			String _cmd = String.valueOf(cmd);
    			if (!_datatable.containsKey(_cmd))
    				_log.warn("Command \""+_cmd+"\" is no used anymore.");
    		}
	}
    
    public final boolean checkPrivileges(L2PcInstance player, String command)
    {
        // Can execute a admin command if everybody has admin rights
    	if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
    		return true;
    	
        //Only a GM can execute a admin command
        if (!player.isGM())
            return false;
        
        StringTokenizer st = new StringTokenizer(command, " ");
        
        String cmd = st.nextToken();  // get command
        
		//Check command existance
        if (!_datatable.containsKey(cmd))
            return false;
        	
        //Check command privileges
        if (Config.ALT_PRIVILEGES_ADMIN)
        {
        	if (Config.GM_COMMAND_PRIVILEGES.containsKey(cmd))
        	{
        		if (player.getAccessLevel() >= Config.GM_COMMAND_PRIVILEGES.get(cmd))
        			return true;
        		else
        			return false;
        	}
        	else
        	{
        		_log.warn("Command \""+cmd+"\" have no access level definition. Can't be used.");
        		return false;
        	}
        }
        /*
        else
        	if (!_datatable.get(cmd).checkLevel(player.getAccessLevel()))
        		return false;	
        */
        if (player.getAccessLevel()>0)
        	return true;
        else
        {
        	_log.warn("GM "+player.getName()+"("+player.getObjectId()+") have no access level.");
        	return false;
        }
      }
}

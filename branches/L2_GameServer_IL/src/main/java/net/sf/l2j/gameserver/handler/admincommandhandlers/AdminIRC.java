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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.instancemanager.IrcManager;

/**
 * This class handles following admin commands:
 * admin_ircc admin_ircm
 */
public class AdminIRC implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = {"admin_ircc","admin_ircm"};
    private static final int REQUIRED_LEVEL = Config.GM_IRC;

    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
    	
        String text = command.substring(10);
        
        if (command.startsWith("admin_ircc"))
        {
        	IrcManager.getInstance().getConnection().send(text);
        }
        else if (command.startsWith("admin_ircm")) 
        {
            StringTokenizer st = new StringTokenizer(text);
            String name = st.nextToken();
            String message = text.substring(name.length()+1);
    		IrcManager.getInstance().getConnection().send(name,message);
        }
        return true;
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
}

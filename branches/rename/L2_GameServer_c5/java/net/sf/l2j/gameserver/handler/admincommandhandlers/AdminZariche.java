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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ZaricheManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminZariche implements IAdminCommandHandler {
    //private static Logger _log = Logger.getLogger(AdminBan.class.getName());
    private static String[] _adminCommands = {"admin_z_infos", "admin_z_remove", "admin_z_goto"};
    private static final int REQUIRED_LEVEL = Config.GM_MIN;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel())))
            {
                return false;
            }
        }

        ZaricheManager zm = ZaricheManager.getInstance();
               
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (command.startsWith("admin_z_infos"))
        {
            activeChar.sendMessage("Infos on Zariche:");
            if (zm.isZaricheActive())
            {
                L2PcInstance pl = zm.getPlayer();
                activeChar.sendMessage("  Player holding: "+pl.getName());
                activeChar.sendMessage("    Player karma: "+zm.getPlayerKarma());
                activeChar.sendMessage("    Time Remaing: "+(zm.getTimeLeft()/60000)+" min.");
                activeChar.sendMessage("    Kills done: "+zm.getNbKills());
            } else if (zm.isZaricheDropped())
            {
                activeChar.sendMessage("  Only dropped on the ground.");
                activeChar.sendMessage("    Time Remaing: "+(zm.getTimeLeft()/60000)+" min.");
                activeChar.sendMessage("    Kills done: "+zm.getNbKills());
            } else
            {
                activeChar.sendMessage("  Didn't exist in the world.");
            }
            activeChar.sendMessage("----------------------");
        }
        else if (command.startsWith("admin_z_remove"))
        {
            zm.endOfZaricheLife();
        }
        else if (command.startsWith("admin_z_goto"))
        {
            zm.goTo(activeChar);
        }

        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}

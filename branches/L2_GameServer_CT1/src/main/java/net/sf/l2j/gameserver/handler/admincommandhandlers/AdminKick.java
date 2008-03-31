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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.LeaveWorld;

public class AdminKick implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS = {"admin_kick" ,"admin_kick_non_gm"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
                return false;
        
        if (command.startsWith("admin_kick"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String plyr = st.nextToken();
                L2PcInstance player = L2World.getInstance().getPlayer(plyr);
                if (player != null)
                {
                    kickPlayer(player);
                    activeChar.sendMessage("You kicked " + player.getName() + " from the game.");
                }
            }
        }
        if (command.startsWith("admin_kick_non_gm"))
        {
            int counter = 0;
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
                if(!player.isGM())
                {
                    counter++;
                    kickPlayer(player);
                }
            }
            activeChar.sendMessage("Kicked "+counter+" players");
        }
        return true;
    }
        
    private void kickPlayer (L2PcInstance player)
    {
        try
        {
            L2GameClient client = player.getClient();
            L2GameClient.saveCharToDisk(player, true); // Store character
            player.deleteMe();
            // prevent deleteMe from being called a second time on disconnection
            client.setActiveChar(null);
        }
        catch (Throwable t){}
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

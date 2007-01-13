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

/**
 *
 * @author: FBIagent
 *
 */

package net.sf.l2j.gameserver.handler.admincommandhandlers;

import javolution.lang.TextBuilder;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvT;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class AdminTvTEngine implements IAdminCommandHandler {

 private static String[] _adminCommands = {"admin_tvt",
                                           "admin_tvt_name", "admin_tvt_desc", "admin_tvt_join_loc",
                                           "admin_tvt_npc", "admin_tvt_npc_pos",
                                           "admin_tvt_reward", "admin_tvt_reward_amount",
                                           "admin_tvt_team_add", "admin_tvt_team_remove", "admin_tvt_team_pos", "admin_tvt_team_color",
                                           "admin_tvt_join", "admin_tvt_teleport", "admin_tvt_start", "admin_tvt_abort", "admin_tvt_finish",
                                           "admin_tvt_sit",
                                           "admin_tvt_dump"};
 
 private static final int REQUIRED_LEVEL = 100;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        if (command.equals("admin_tvt"))
            showMainPage(activeChar);
        else if (command.startsWith("admin_tvt_name "))
        {
            TvT._eventName = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_desc "))
        {
            TvT._eventDesc = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_join_loc "))
        {
            TvT._joiningLocationName = command.substring(19);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_npc "))
        {
            TvT._npcId = Integer.valueOf(command.substring(14));
            showMainPage(activeChar);
        }
        else if (command.equals("admin_tvt_npc_pos"))
        {
            TvT.setNpcPos(activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_reward "))
        {
            TvT._rewardId = Integer.valueOf(command.substring(17));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_reward_amount "))
        {
            TvT._rewardAmount = Integer.valueOf(command.substring(24));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_team_add "))
        {
            String teamName = command.substring(19);
            
            TvT.addTeam(teamName);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_team_remove "))
        {
            String teamName = command.substring(22);

            TvT.removeTeam(teamName);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_team_pos "))
        {
            String teamName = command.substring(19);

            TvT.setTeamPos(teamName, activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_tvt_team_color "))
        {
            String[] params;

            params = command.split(" ");
            
            if (params.length != 3)
            {
                activeChar.sendMessage("Wrong usge: //tvt_team_color <colorHex> <teamName>");
                return false;
            }

            TvT.setTeamColor(command.substring(params[0].length()+params[1].length()+2), Integer.decode("0x" + params[1]));
            showMainPage(activeChar);
        }
        else if(command.equals("admin_tvt_join"))
        {
            TvT.startJoin();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_tvt_teleport"))
        {
            TvT.teleportStart();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_tvt_start"))
        {
            TvT.startEvent();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_tvt_abort"))
        {
            TvT.abortEvent();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_tvt_finish"))
        {
            TvT.finishEvent(activeChar);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_tvt_sit"))
        {
            TvT.sit();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_tvt_dump"))
            TvT.dumpData();

        return true;
    }

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level) 
    {
        return (level >= REQUIRED_LEVEL);
    }

    public void showMainPage(L2PcInstance activeChar)
    {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        
        replyMSG.append("<center><font color=\"LEVEL\">[TvT Engine]</font></center><br><br><br>");
        replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
        replyMSG.append("<table border=\"0\"><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Name\" action=\"bypass -h admin_tvt_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Description\" action=\"bypass -h admin_tvt_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Join Location\" action=\"bypass -h admin_tvt_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC\" action=\"bypass -h admin_tvt_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC Pos\" action=\"bypass -h admin_tvt_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Reward\" action=\"bypass -h admin_tvt_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Reward Amount\" action=\"bypass -h admin_tvt_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Team Add\" action=\"bypass -h admin_tvt_team_add $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Team Color\" action=\"bypass -h admin_tvt_team_color $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Team Pos\" action=\"bypass -h admin_tvt_team_pos $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Team Remove\" action=\"bypass -h admin_tvt_team_remove $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Join\" action=\"bypass -h admin_tvt_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Teleport\" action=\"bypass -h admin_tvt_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Start\" action=\"bypass -h admin_tvt_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Abort\" action=\"bypass -h admin_tvt_abort\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Finish\" action=\"bypass -h admin_tvt_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Sit Force\" action=\"bypass -h admin_tvt_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Dump\" action=\"bypass -h admin_tvt_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><br>");
        replyMSG.append("Current event...<br1>");
        replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + TvT._eventName + "</font><br1>");
        replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + TvT._eventDesc + "</font><br1>");
        replyMSG.append("    ... joining location name:&nbsp;<font color=\"00FF00\">" + TvT._joiningLocationName + "</font><br1>");
        replyMSG.append("    ... joining NPC ID:&nbsp;<font color=\"00FF00\">" + TvT._npcId + " on pos " + TvT._npcX + "," + TvT._npcY + "," + TvT._npcZ + "</font><br1>");
        replyMSG.append("    ... reward ID:&nbsp;<font color=\"00FF00\">" + TvT._rewardId + "</font><br1>");
        replyMSG.append("    ... reward Amount:&nbsp;<font color=\"00FF00\">" + TvT._rewardAmount + "</font><br><br>");
        replyMSG.append("Current teams:<br1>");
        replyMSG.append("<center><table border=\"0\">");
        
        for (String team : TvT._teams)
        {
            replyMSG.append("<tr><td>Name: </td><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>");
            replyMSG.append("&nbsp;(" + TvT.teamPlayersCount(team) + " joined)");
            replyMSG.append("</td></tr>");
            replyMSG.append("<tr><td>Color: </td>");
            replyMSG.append("<td>" + TvT._teamColors.get(TvT._teams.indexOf(team)) + "</td></tr>");
            replyMSG.append("<tr><td>Coordinates: </td>");
            replyMSG.append("<td>" + TvT._teamsX.get(TvT._teams.indexOf(team)) + ", " + TvT._teamsY.get(TvT._teams.indexOf(team)) + ", " + TvT._teamsZ.get(TvT._teams.indexOf(team)) + "</td></tr>");
            replyMSG.append("<tr><td>To remove: </td>");
            replyMSG.append("<td width=\"60\"><button value=\"Remove\" action=\"bypass -h admin_tvt_team_remove " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        }
        
        replyMSG.append("</table></center>");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }
}
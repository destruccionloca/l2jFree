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

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DM;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class AdminDMEngine implements IAdminCommandHandler {

 private static String[] _adminCommands = {"admin_dm",
                                           "admin_dm_name", "admin_dm_desc", "admin_dm_join_loc",
                                           "admin_dm_minlvl", "admin_dm_maxlvl",
                                           "admin_dm_npc", "admin_dm_npc_pos",
                                           "admin_dm_reward", "admin_dm_reward_amount", "admin_dm_spawnpos", "admin_dm_color",
                                           "admin_dm_join", "admin_dm_teleport", "admin_dm_start", "admin_dm_abort", "admin_dm_finish",
                                           "admin_dm_sit", "admin_dm_dump", "admin_dm_save", "admin_dm_load"};
 
 private static final int REQUIRED_LEVEL = 100;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        if (command.equals("admin_dm"))
            showMainPage(activeChar);
        else if (command.startsWith("admin_dm_name "))
        {
            DM._eventName = command.substring(14);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_desc "))
        {
            DM._eventDesc = command.substring(14);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_minlvl "))
        {
            if (!DM.checkMinLevel(Integer.valueOf(command.substring(16))))
                return false;
            DM._minlvl = Integer.valueOf(command.substring(16));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_maxlvl "))
        {
            if (!DM.checkMaxLevel(Integer.valueOf(command.substring(16))))
                return false;
            DM._maxlvl = Integer.valueOf(command.substring(16));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_join_loc "))
        {
            DM._joiningLocationName = command.substring(18);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_npc "))
        {
            DM._npcId = Integer.valueOf(command.substring(13));
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_npc_pos"))
        {
            DM.setNpcPos(activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_reward "))
        {
            DM._rewardId = Integer.valueOf(command.substring(16));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_reward_amount "))
        {
            DM._rewardAmount = Integer.valueOf(command.substring(23));
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_spawnpos"))
        {
            DM.setPlayersPos(activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_dm_color "))
        {
            DM._playerColors = Integer.decode("0x" + command.substring(15));
            showMainPage(activeChar);
        }
        else if(command.equals("admin_dm_join"))
        {
            DM.startJoin(activeChar);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_teleport"))
        {
            DM.teleportStart();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_dm_start"))
        {
            DM.startEvent(activeChar);
            showMainPage(activeChar);
        }
        else if(command.equals("admin_dm_abort"))
        {
            activeChar.sendMessage("Aborting event");
            DM.abortEvent();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_dm_finish"))
        {
            DM.finishEvent(activeChar);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_sit"))
        {
            DM.sit();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_load"))
        {
            DM.loadData();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_save"))
        {
            DM.saveData();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_dm_dump"))
            DM.dumpData();

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
        
        replyMSG.append("<center><font color=\"LEVEL\">[dm Engine]</font></center><br><br><br>");
        replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
        replyMSG.append("<table border=\"0\"><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Name\" action=\"bypass -h admin_dm_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Description\" action=\"bypass -h admin_dm_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Join Location\" action=\"bypass -h admin_dm_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Max lvl\" action=\"bypass -h admin_dm_maxlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Min lvl\" action=\"bypass -h admin_dm_minlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC\" action=\"bypass -h admin_dm_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC Pos\" action=\"bypass -h admin_dm_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Reward\" action=\"bypass -h admin_dm_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Reward Amount\" action=\"bypass -h admin_dm_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"DM Color\" action=\"bypass -h admin_dm_color $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"DM SpawnPos\" action=\"bypass -h admin_dm_spawnpos $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><table><br><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Join\" action=\"bypass -h admin_dm_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Teleport\" action=\"bypass -h admin_dm_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Start\" action=\"bypass -h admin_dm_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Abort\" action=\"bypass -h admin_dm_abort\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Finish\" action=\"bypass -h admin_dm_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Sit Force\" action=\"bypass -h admin_dm_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Dump\" action=\"bypass -h admin_dm_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Save\" action=\"bypass -h admin_dm_save\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Load\" action=\"bypass -h admin_dm_load\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><br>");
        replyMSG.append("Current event...<br1>");
        replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + DM._eventName + "</font><br1>");
        replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + DM._eventDesc + "</font><br1>");
        replyMSG.append("    ... joining location name:&nbsp;<font color=\"00FF00\">" + DM._joiningLocationName + "</font><br1>");
        replyMSG.append("    ... joining NPC ID:&nbsp;<font color=\"00FF00\">" + DM._npcId + " on pos " + DM._npcX + "," + DM._npcY + "," + DM._npcZ + "</font><br1>");
        replyMSG.append("    ... reward ID:&nbsp;<font color=\"00FF00\">" + DM._rewardId + "</font><br1>");
        replyMSG.append("    ... reward Amount:&nbsp;<font color=\"00FF00\">" + DM._rewardAmount + "</font><br><br>");
        replyMSG.append("    ... Min lvl:&nbsp;<font color=\"00FF00\">" + DM._minlvl + "</font><br>");
        replyMSG.append("    ... Max lvl:&nbsp;<font color=\"00FF00\">" + DM._maxlvl + "</font><br><br>");
        replyMSG.append("Current players:<br1>");
        
        if (!DM._started)
        {
            replyMSG.append("<br1>");
            replyMSG.append(DM._players.size() + " players participating.");
            replyMSG.append("<br><br>");
        }
        
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }
}

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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.faction.Faction;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/** 
 * @author evill33t
 * 
 */
public class faction implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { "faction" };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if(activeChar.getNPCFaction()!=null)
        {
                NpcHtmlMessage factionInfo = new NpcHtmlMessage(5);

                int factionId = activeChar.getNPCFaction().getFactionId();
                Faction faction = FactionManager.getInstance().getFactions(factionId);
                TextBuilder replyMSG = new TextBuilder("<html><body>");
                replyMSG.append("<HTML><HEAD><BODY>");
                replyMSG.append("faction id" + String.valueOf(factionId)+"<br>");
                replyMSG.append("faction name" + faction.getName()+"<br>");
                replyMSG.append("faction points" + activeChar.getNPCFactionPoints()+"<br>");
                replyMSG.append("faction side" + String.valueOf(faction.getSide())+"<br>");
                replyMSG.append("</BODY></HTML>");
                factionInfo.setHtml(replyMSG.toString());
                activeChar.sendPacket(factionInfo);
                return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}    

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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.model.entity.faction.Faction;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FactionQuestManagerInstance extends L2NpcInstance
{
    /**
     * @author evill33t
     */
    public L2FactionQuestManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        showMessageWindow(player);
    }
    
    private void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/npcdefault.htm";
        String replace = "";
 
        int factionId = getTemplate().getNPCFactionId();
        String factionName = getTemplate().getNPCFactionName();
        if(factionId!=0)
        {
            filename = "data/html/faction/" + String.valueOf(factionId)  +  "/start.htm";
            replace = getName();
        }
        this.sendHtmlMessage(player, filename, replace, factionName);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // standard msg
        String filename = "data/html/npcdefault.htm";
        String factionName = getTemplate().getNPCFactionName();
        int factionId = getTemplate().getNPCFactionId();
        Faction faction = FactionManager.getInstance().getFactions(factionId);
        int factionPrice = faction.getPrice();
        String replace = "";
        if(factionId!=0)
        {
            String path = "data/html/faction" + String.valueOf(factionId) + "/";
            replace = String.valueOf(factionPrice);
            
            if(player.getNPCFaction()!=null)
            {
                //quest stuff here
            }
            else if (command.startsWith("Join"))
                filename = path + "wrong.htm";
        }
        this.sendHtmlMessage(player, filename, replace, factionName);
    } 

    private void sendHtmlMessage(L2PcInstance player, String filename, String replace, String factionName)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        html.replace("%factionName%", factionName);
        player.sendPacket(html);
    }
}

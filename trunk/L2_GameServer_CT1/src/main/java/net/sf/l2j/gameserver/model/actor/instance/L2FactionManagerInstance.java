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

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.model.entity.faction.Faction;
import net.sf.l2j.gameserver.model.entity.faction.FactionMember;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FactionManagerInstance extends L2NpcInstance
{
    /**
     * @author evill33t
     */
    public L2FactionManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}

    private void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/npcdefault.htm";
        String replace = "";
 
        int factionId = getTemplate().getNpcFaction();
        String factionName = getTemplate().getNpcFactionName();
        if(factionId!=0)
        {
            filename = "data/html/faction/" + String.valueOf(factionId)  +  "/start.htm";
            replace = getName();
        }
        sendHtmlMessage(player, filename, replace, factionName);
    }
    
    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // standard msg
        String filename = "data/html/npcdefault.htm";
        String factionName = getTemplate().getNpcFactionName();
        int factionId = getTemplate().getNpcFaction();
        Faction faction = FactionManager.getInstance().getFactions(factionId);
        int factionPrice = faction.getPrice();
        String replace = "";
        if(factionId!=0)
        {
            String path = "data/html/faction" + String.valueOf(factionId) + "/";
            replace = String.valueOf(factionPrice);
            
            if(player.getNPCFaction()!=null)
            {
                if(player.getNPCFaction().getSide()!=faction.getSide())
                    filename = path + "already.htm";
                else
                    filename = path + "switch.htm";
            }
            else if (command.startsWith("Join"))
                filename = path + "join.htm";
            else if (command.startsWith("Accept"))
            {
                if(player.getNPCFaction()==null)
                {
                    if(player.getAdena()<factionPrice)
                        filename = path + "noadena.htm";                    
                    else
                    {
                        player.getInventory().reduceAdena("Faction", factionPrice, player, null);
                        player.setNPCFaction(new FactionMember(player.getObjectId(),factionId));
                        filename = path + "accepted.htm";                    
                    }
                }
                else
                {
                    player.getNPCFaction().setFactionId(factionId);
                    filename = path + "switched.htm";
                }
            }
            else if (command.startsWith("Decline"))
                filename = path + "declined.htm";
            else if (command.startsWith("AskQuit"))
                filename = path + "askquit.htm";
            else if (command.startsWith("Story"))
                filename = path + "story.htm";
            else if (command.startsWith("Quit"))
            {
                player.getNPCFaction().quitFaction();
                filename = path + "quited.htm";
            }
            else if (command.startsWith("Quest"))
            {
                filename = path + "quest.htm";
            }
            else if (command.startsWith("FactionShop"))
                filename = path + "shop.htm";
        }
        sendHtmlMessage(player, filename, replace, factionName);
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

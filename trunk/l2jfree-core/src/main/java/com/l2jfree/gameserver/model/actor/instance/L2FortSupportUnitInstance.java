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
package com.l2jfree.gameserver.model.actor.instance;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Psychokiller1888
 */

public class L2FortSupportUnitInstance extends L2MerchantInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

    public L2FortSupportUnitInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("shirt"))
        {
        	String filename = "data/html/fortress/na.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(filename);
            player.sendPacket(html);
        }
        else if (command.startsWith("skills"))
        {
        	String filename = "data/html/fortress/na.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(filename);
            player.sendPacket(html);
        }
        else
        {
            super.onBypassFeedback(player, command);
        }
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
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
				showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/fortress/support_unit_captain-no.htm";
        
        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE)
        {
        	if (condition == COND_OWNER)
        		filename = "data/html/fortress/support_unit_captain.htm";
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
    }
    
	protected int validateCondition(L2PcInstance player)
	{
		if (getFort() != null && getFort().getFortId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if (getFort().getOwnerClan() == player.getClan()) // Clan owns fortress
					return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
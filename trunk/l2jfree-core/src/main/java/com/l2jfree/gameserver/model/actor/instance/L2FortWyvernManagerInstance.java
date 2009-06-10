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

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.PetDataTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Psychokiller1888
 */

public class L2FortWyvernManagerInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
    public L2FortWyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("RideWyvern"))
        {
            if (!player.isClanLeader())
            {
                player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
                return;
            }

            int petItemId = 0;
            L2ItemInstance petItem = null;
            
            if (player.getPet() == null)
            {
                if (player.isMounted())
                {
                    petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
                    if (petItem != null)
                        petItemId = petItem.getItemId();
                }
            }
            else
                petItemId = player.getPet().getControlItemId();

            if (petItemId == 0 || !player.isMounted() || !PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)))
            {
                player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
            	NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/fortress/wyvernmanager-explain.htm");
                html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
                player.sendPacket(html);
                return;
            }
            else if (player.isMounted() && PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)) &&
                         petItem != null && petItem.getEnchantLevel() < 55)
            {
            	NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/fortress/wyvernmanager-explain.htm");
                html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
                player.sendPacket(html);
                return;
            }
            
            // Wyvern requires Config.MANAGER_CRYSTAL_COUNT crystal for ride...
            if (player.getInventory().getItemByItemId(1460) != null &&
                    player.getInventory().getItemByItemId(1460).getCount() >= Config.ALT_MANAGER_CRYSTAL_COUNT)
            {
                if(!player.disarmWeapons(true))
                    return;
                
                if (player.isMounted())
                    player.dismount();
                
                if (player.getPet() != null)
                    player.getPet().unSummon(player);

                if (player.mount(12621, 0, true))
                {
                    player.getInventory().destroyItemByItemId("Wyvern", 1460, Config.ALT_MANAGER_CRYSTAL_COUNT, player, player.getTarget());
                    player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
                    player.sendMessage("The Wyvern has been summoned successfully!");
                }
            }
            else
            {
            	NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/fortress/wyvernmanager-explain.htm");
                html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
                player.sendPacket(html);

            	player.sendMessage("You need " + Config.ALT_MANAGER_CRYSTAL_COUNT + " Crystals: B Grade.");
            }
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
        String filename = "data/html/fortress/wyvernmanager-no.htm";
        
        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE)
        {
        	if (condition == COND_OWNER)
        		filename = "data/html/fortress/wyvernmanager.htm";
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
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
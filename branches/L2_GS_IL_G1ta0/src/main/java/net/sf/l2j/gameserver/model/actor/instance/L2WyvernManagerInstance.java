/* This program is free software; you can redistribute it and/or modify
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
import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{

    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
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
                player.sendMessage("Only clan leaders are allowed.");
                return;
            }

            int petItemId=0;
            L2ItemInstance petItem = null;
            
            if(player.getPet()==null) 
            {
                if(player.isMounted())
                {
                    petItem = (L2ItemInstance)L2World.getInstance().findObject(player.getMountObjectID());
                    
                    if (petItem!=null) petItemId=petItem.getItemId();
                }
            }
            else 
                petItemId = player.getPet().getControlItemId(); 

            if  ( petItemId==0 || !player.isMounted() || 
                 !PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)))
            {
                SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
                player.sendPacket(sm);
                sm = null;
                return;
            }
            else if ( player.isMounted() &&  PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)) &&
                         petItem != null && petItem.getEnchantLevel() < 55 )
            {
                player.sendMessage("Your Strider has not reached the required level.");
                return; 
            }
            
            // Wyvern requires 10B crystal for ride...
            if(player.getInventory().getItemByItemId(1460) != null &&
                    player.getInventory().getItemByItemId(1460).getCount() >= 10)
            {
                if(!player.disarmWeapons()) return;
                player.getInventory().destroyItemByItemId("WyvernManager", 1460, 10, player, this);
                
                if (player.isMounted())
                {
                   Ride dismount= new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT,0);
                   player.broadcastPacket(dismount);
                   player.setMountType(0);
                }
                
                if (player.getPet() != null) player.getPet().unSummon(player);    
                
                Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
                player.sendPacket(mount);
                player.broadcastPacket(mount);
                player.setMountType(mount.getMountType());
            }
            else
            {
                player.sendMessage("You need 10 Crystals: B Grade.");
                return;
            }
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
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		player.sendPacket(new ActionFailed());
	}

    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
        
        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE)
        {
            if (condition == COND_OWNER)                                     // Clan owns castle
                filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}

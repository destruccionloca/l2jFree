package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{

    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("RideWyvern"))
        {
        	
            int petItemId=0;
            L2ItemInstance petItem = null;
            
            if(player.getPet()==null) 
            {
                if(player.isMounted())
                {
            		petItem = (L2ItemInstance)L2World.getInstance().findObject(player.getMountObjectID());
            		
            		if (petItem!=null) petItemId=petItem.getItemId();
            	}
            } else 
            	petItemId = player.getPet().getControlItemId(); 

        	if  ( petItemId==0 || 
        		 !player.isMounted() || 
        		 !PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)))
            {
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
                player.sendPacket(sm);
                sm = null;
                return;
            } else
            	if  ( player.isMounted() && 
               		  PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)) &&
               		  petItem != null && 
               		  petItem.getEnchantLevel() < 55 )
            	{
                    SystemMessage sm = new SystemMessage(614);
                    sm.addString("Your Strider don't reach the required level.");
                    player.sendPacket(sm);
                    return; 
            	}
        	
            // Wyvern requires 10B crystal for ride...
            if(player.getInventory().getItemByItemId(1460) != null &&
                    player.getInventory().getItemByItemId(1460).getCount() >= 10)
            {
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
                SystemMessage sm = new SystemMessage(614);
                sm.addString("You need 10 Crystals: B Grade.");
                player.sendPacket(sm);
                return;
            }
        }
    }
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));
        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
    }
    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
        
        int condition = validateCondition(player);
        if (condition > Cond_All_False)
        {
            if (condition == Cond_Owner)                                     // Clan owns castle
                filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    } 
}

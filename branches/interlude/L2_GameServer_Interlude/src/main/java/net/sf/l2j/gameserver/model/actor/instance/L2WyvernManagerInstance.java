package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2FolkInstance
{

    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("RideWyvern"))
        {
            if (getCastle().getOwnerId() != player.getClanId() && !player.isClanLeader())
            {
                SystemMessage sm = new SystemMessage(614);
                sm.addString("To ride a wyvern, you must be the clan leader.");
                player.sendPacket(sm);
                sm = null;
                return;
            }
            
            int petItemId=0;
            
            if(player.getPet()==null) {
                   if(player.isMounted()){
                   L2ItemInstance petItem = (L2ItemInstance)L2World.getInstance().findObject(player.getMountObjectID());
                   if (petItem!=null) petItemId=petItem.getItemId();}
            } else petItemId=player.getPet().getControlItemId(); 

            if  ( petItemId==0 || !player.isMounted() || !L2PetDataTable.isStrider(L2PetDataTable.getPetIdByItemId(petItemId)))
            {
                SystemMessage sm = new SystemMessage(614);
                sm.addString("To ride a wyvern, you must be riding a strider.");
                player.sendPacket(sm);
                sm = null;
                return;
            }
            
            // Wyvern requires 100B crystal for ride...
            if(player.getInventory().getItemByItemId(1460) != null &&
                    player.getInventory().getItemByItemId(1460).getCount() >= 100)
            {
                player.getInventory().destroyItemByItemId("Wyvern", 1460, 100, player, player.getTarget());
                if (player.isMounted())
                {
                   Ride dismount= new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT,0);
                   player.broadcastPacket(dismount);
                   player.setMountType(0);
                }
                if (player.getPet() != null)player.getPet().unSummon(player);                
                Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
                player.sendPacket(mount);
                player.broadcastPacket(mount);
                player.setMountType(mount.getMountType());
            }
            else
            {
                player.sendMessage("You need 100 B Crystals to ride Wyvern");
            }
        }
    }
}

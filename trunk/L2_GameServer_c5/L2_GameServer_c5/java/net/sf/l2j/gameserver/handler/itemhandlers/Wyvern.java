package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class Wyvern implements IItemHandler
{
	private static int[] _itemIds = { 4425 };
        private int PetRideId;	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
	    int itemId = item.getItemId();

        if (activeChar.isInOlympiadMode())
        {
            activeChar.sendMessage("Cant use Wyvern in Olympiad mode");
            return;
        }
        
        if (activeChar.isCursedWeaponEquiped())
        {
           // You can't mount while weilding a cursed weapon
           activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
           return;
        }
        
        if (itemId == 4425) // wyvern
        {
            if(activeChar.isMounted() || activeChar.getPet() != null){
                SystemMessage sm = new SystemMessage(614);
                sm.addString("Already Have a Pet or Mounted.");
                activeChar.sendPacket(sm);
                return;                
            } else 
            {
            PetRideId = 12621; 
            Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, PetRideId);
            activeChar.sendPacket(mount);
            activeChar.broadcastPacket(mount);
            activeChar.setMountType(mount.getMountType());
            return;            
            }
        }        
		//activeChar.removeItemFromInventory(item, 1);
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}

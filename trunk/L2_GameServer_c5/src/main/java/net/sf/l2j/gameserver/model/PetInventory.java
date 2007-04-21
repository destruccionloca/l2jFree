package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;

public class PetInventory extends Inventory 
{
	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner) 
    {
		_owner = owner;
	}
    
	public L2PetInstance getOwner() 
    { 
        return _owner; 
    }
    
	protected ItemLocation getBaseLocation() 
    {
        return ItemLocation.PET; 
    }
    
	protected ItemLocation getEquipLocation() 
    { 
        return ItemLocation.PET_EQUIP; 
    }
	
    protected void refreshWeight()
    {
        super.refreshWeight();
        getOwner().refreshOverloaded();
    }

    public boolean validateWeight(int weight)
    {
        return (_totalWeight + weight <= _owner.getMaxLoad());
    }

    /**
     * 
     */
    @Override
    public void updateInventory(L2ItemInstance newItem,int count,StatusUpdate playerSU)
    {
        PetInventoryUpdate petIU = new PetInventoryUpdate();

        if (newItem.getCount() > count) petIU.addModifiedItem(newItem);
        else petIU.addNewItem(newItem);

        this.getOwner().getOwner().sendPacket(petIU);        
    }
}

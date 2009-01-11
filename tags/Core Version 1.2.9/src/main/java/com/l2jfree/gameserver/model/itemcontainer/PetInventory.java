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
package com.l2jfree.gameserver.model.itemcontainer;

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ItemInstance.ItemLocation;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.network.serverpackets.PetInventoryUpdate;

public class PetInventory extends Inventory 
{
	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner) 
    {
		_owner = owner;
	}
    
    @Override
    public L2PetInstance getOwner() 
    { 
        return _owner; 
    }

	@Override
	public int getOwnerId()
	{
		// gets the L2PcInstance-owner's ID
		int id = 0;

		if (_owner.getOwner() != null) {
			id = _owner.getOwner().getObjectId();
		}

		return id;
	}

	@Override
    protected ItemLocation getBaseLocation() 
    {
        return ItemLocation.PET; 
    }
    
    @Override
    protected ItemLocation getEquipLocation() 
    { 
        return ItemLocation.PET_EQUIP; 
    }
	
    @Override
    protected void refreshWeight()
    {
        super.refreshWeight();
        getOwner().refreshOverloaded();
    }

    @Override
    public boolean validateWeight(int weight)
    {
        return (_totalWeight + weight <= _owner.getMaxLoad());
    }

    @Override
    public void updateInventory(L2ItemInstance newItem)
    {
        PetInventoryUpdate petIU = new PetInventoryUpdate();
        petIU.addItem(newItem);
        getOwner().getOwner().sendPacket(petIU);        
    }
}

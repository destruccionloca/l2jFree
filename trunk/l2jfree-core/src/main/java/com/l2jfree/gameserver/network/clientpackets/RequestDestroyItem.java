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
package com.l2jfree.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.datatables.PetDataTable;
import com.l2jfree.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestDestroyItem extends L2GameClientPacket
{
	private static final String _C__59_REQUESTDESTROYITEM = "[C] 59 RequestDestroyItem";
	private final static Log _log = LogFactory.getLog(RequestDestroyItem.class.getName());

	private int _objectId;
	private int _count;
	/**
	 * packet type id 0x1f
	 * 
	 * sample
	 * 
	 * 59 
	 * 0b 00 00 40		// object id 
	 * 01 00 00 00		// count ??
	 * 
	 * 
	 * format:		cdd  
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
    {
		_objectId = readD();
		_count = readD();
	}

    @Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		
		if(_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar,"[RequestDestroyItem] count < 0! ban! oid: "+_objectId+" owner: "+activeChar.getName(),Config.DEFAULT_PUNISH);
			return;
		}
		
		int count = _count;
		
        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        
		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);

		// if we can't find the requested item, its actually a cheat
		if (itemToRemove == null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
	            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
	            return;
			}
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow())
		{
			if (activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId())
			{
	            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
	            return;
			}
		}

		int itemId = itemToRemove.getItemId();
        if (itemToRemove.isWear() || (!itemToRemove.isDestroyable() && !activeChar.isGM()) || (CursedWeaponsManager.getInstance().isCursed(itemId) &&  !activeChar.isGM()))
		{
			if (itemToRemove.isHeroItem())
				activeChar.sendPacket(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
			else
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
		    return;
		}
        
        if (Config.ALT_STRICT_HERO_SYSTEM)
        {
            if (itemToRemove.isHeroItem() && !activeChar.isGM())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
                return;
            }
        }

        if(!itemToRemove.isStackable() && count > 1)
        {
            Util.handleIllegalPlayerAction(activeChar,"[RequestDestroyItem] count > 1 but item is not stackable! oid: "+_objectId+" owner: "+activeChar.getName(),Config.DEFAULT_PUNISH);
            return;
        }
        
		if (_count > itemToRemove.getCount())
			count = itemToRemove.getCount();
		
		
		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
				activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot()); 
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped) {
				activeChar.checkSSMatch(null, element);
				
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if (PetDataTable.isPetItem(itemId))
		{
			Connection con = null;
			try
			{
				if (activeChar.getPet() != null && activeChar.getPet().getControlItemId() == _objectId)
				{
					activeChar.getPet().unSummon(activeChar);
				}

				// if it's a pet control item, delete the pet
				con = L2DatabaseFactory.getInstance().getConnection(con); 
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn( "could not delete pet objectid: ", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", _objectId, count, activeChar, null);

		if(removedItem == null)
			return;
		
		activeChar.getInventory().updateInventory(removedItem);

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
    public String getType()
	{
		return _C__59_REQUESTDESTROYITEM;
	}
}

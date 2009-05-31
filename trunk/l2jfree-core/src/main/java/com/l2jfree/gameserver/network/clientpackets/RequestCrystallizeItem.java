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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * This class represents a packet that is sent by the client when a player drags the item
 * on the crystallization hammer.
 * 
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestCrystallizeItem extends L2GameClientPacket
{
	private static final String _C__72_REQUESTDCRYSTALLIZEITEM = "[C] 72 RequestCrystallizeItem";

	private int _objectId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		if(Config.PACKET_FINAL)
			_count = toInt(readQ());
		else
			_count = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		if (Shutdown.isActionDisabled(DisableType.CREATEITEM))
		{
			requestFailed(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
			return;
		}
		else if (_count < 1)
		{
			requestFailed(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
		else if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
		{
			requestFailed(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			requestFailed(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance item = inventory.getItemByObjectId(_objectId);

		if (item == null || item.isWear() || item.isHeroItem() ||
				!item.getItem().isCrystallizable() ||
				item.getItem().getCrystalCount() <= 0 ||
				item.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)
		{
			requestFailed(SystemMessageId.ITEM_CANNOT_CRYSTALLIZED);
			return;
		}

		if (_count > item.getCount())
			_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();

		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;

		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_C:
			{
				if (skillLevel <= 1)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_B:
			{
				if (skillLevel <= 2)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_A:
			{
				if (skillLevel <= 3)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			{
				if (skillLevel <= 4)
					canCrystallize = false;
				break;
			}
		}

		if (!canCrystallize)
		{
			requestFailed(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			return;
		}

		activeChar.setInCrystallize(true);

		//unequip if needed
		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();

			for (L2ItemInstance element : unequiped)
				iu.addModifiedItem(element);

			sendPacket(iu); iu = null;
		}

		// remove from inventory
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);
		if (removedItem == null)
		{
			requestFailed(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}

		// add crystals
		int crystalId = item.getItem().getCrystalItemId();
		int crystalAmount = item.getCrystalCount();
		L2ItemInstance createditem = activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, removedItem);

		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(createditem);
		sm.addNumber(crystalAmount);
		sendPacket(sm);
		sm = null;

		// send inventory update
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
				iu.addRemovedItem(removedItem);
			else
				iu.addModifiedItem(removedItem);

			if (createditem.getCount() != crystalAmount)
				iu.addModifiedItem(createditem);
			else
				iu.addNewItem(createditem);

			sendPacket(iu);
			iu = null;
		}
		else
			sendPacket(new ItemList(activeChar, false));

		// status & user info
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		sendPacket(su);
		su = null;

		L2World.getInstance().removeObject(removedItem);
		activeChar.broadcastUserInfo();
		activeChar.setInCrystallize(false);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__72_REQUESTDCRYSTALLIZEITEM;
	}
}

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
import com.l2jfree.gameserver.model.Elementals;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExAttributeEnchantResult;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.templates.L2Item;
import com.l2jfree.gameserver.templates.L2WeaponType;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.tools.random.Rnd;

public class RequestExEnchantItemAttribute extends L2GameClientPacket
{
	private static final String D0_38_REQUEST_EX_ENCHANT_ITEM_ATTRIBUTE = "[C] D0 38 RequestExEnchantItemAttribute";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_objectId == 0xFFFF)
		{
			// Player canceled enchant
			player.setActiveEnchantAttrItem(null);
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}

		if (player.isOnline() == 0)
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}

		// Restrict enchant during restart/shutdown (because of an existing exploit)
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_ENCHANT && Shutdown.getCounterInstance() != null
				&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendMessage("Enchanting items is not allowed during restart/shutdown.");
			return;
		}

		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
			return;
		}

		// Restrict enchant during a trade (bug if enchant fails)
		if (player.getActiveRequester() != null)
		{
			// Cancel trade
			player.cancelActiveTrade();
			player.sendMessage("Enchanting items is not allowed during a trade.");
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance stone = player.getActiveEnchantAttrItem();
		if (item == null || stone == null)
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}
		if ((item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL))
			return;

		int itemId = item.getItemId();

		if (item.isWear())
		{
			Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
			return;
		}

		//can't enchant rods, shadow items, adventurers', hero items
		if (item.getItem().getItemType() == L2WeaponType.ROD || item.isShadowItem() || item.isHeroItem()
		|| (itemId >= 7816 && itemId <= 7831) || (item.getItem().getItemType() == L2WeaponType.NONE) ||
			!(item.getItem().getCrystalType() == L2Item.CRYSTAL_S || item.getItem().getCrystalType() == L2Item.CRYSTAL_S80))
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantItem(null);
			return;
		}

		switch (item.getLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					player.setActiveEnchantAttrItem(null);
					return;
				}
				break;
			}
			default:
			{
				player.setActiveEnchantAttrItem(null);
				Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to use enchant Exploit!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
		}

		int stoneId = stone.getItemId();
		Elementals oldElement = item.getElementals();
		int elementValue = oldElement == null ? 0 : oldElement.getValue();
		int limit = getLimit(stoneId, item);
		int powerToAdd = getPowerToAdd(stoneId, elementValue, item);
		byte elementToAdd = getElementFromItemId(stoneId);

		// Armors have the opposite element
		if (item.isArmor())
			elementToAdd = Elementals.getOppositeElement(elementToAdd);

		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}

		if (oldElement != null && oldElement.getElement() != elementToAdd)
		{
			player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
			player.setActiveEnchantItem(null);
			return;
		}

		if (powerToAdd == 0)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantItem(null);
			return;
		}

		if(!player.destroyItem("AttrEnchant", stone, 1, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to attribute enchant with a stone he doesn't have", Config.DEFAULT_PUNISH);
			player.setActiveEnchantAttrItem(null);
			return;
		}

		if (Rnd.get(100) < Elementals.ENCHANT_CHANCE)
		{
			SystemMessage sm;
			if (item.getEnchantLevel() == 0)
			{
				sm = new SystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1).addItemName(item).addNumber(powerToAdd);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2).addNumber(item.getEnchantLevel());
				sm.addItemName(item).addNumber(powerToAdd);
			}
			player.sendPacket(sm);
			item.setElementAttr(elementToAdd, newPower);

			// send packets
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		else
		{
			player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
		}

		player.sendPacket(new ExAttributeEnchantResult(powerToAdd));
		player.sendPacket(new UserInfo(player));
		player.setActiveEnchantItem(null);
	}

	public byte getElementFromItemId(int itemId)
	{
		byte element = 0;
		for (int id : Elementals.STONES)
		{
			if (id == itemId)
				return element;
			element++;
		}
		element = 0;
		for (int id : Elementals.CRYSTALS)
		{
			if (id == itemId)
				return element;
			element++;
		}
		element = 0;
		for (int id : Elementals.JEWELS)
		{
			if (id == itemId)
				return element;
			element++;
		}
		element = 0;
		for (int id : Elementals.ENERGIES)
		{
			if (id == itemId)
				return element;
			element++;
		}
		return -1;
	}

	public int getLimit(int itemId, L2ItemInstance item)
	{
		for (int id : Elementals.STONES)
		{
			if (id == itemId)
			{
				if (item.isWeapon())
					return Elementals.WEAPON_VALUES[3];
				return Elementals.ARMOR_VALUES[3];
			}
		}
		for (int id : Elementals.CRYSTALS)
		{
			if (id == itemId)
			{
				if (item.isWeapon())
					return Elementals.WEAPON_VALUES[6];
				return Elementals.ARMOR_VALUES[6];
			}
		}
		for (int id : Elementals.JEWELS)
		{
			if (id == itemId)
			{
				if (item.isWeapon())
					return Elementals.WEAPON_VALUES[9];
				return Elementals.ARMOR_VALUES[9];
			}
		}
		for (int id : Elementals.ENERGIES)
		{
			if (id == itemId)
			{
				if (item.isWeapon())
					return Elementals.WEAPON_VALUES[10]; // Should be 12
				return Elementals.ARMOR_VALUES[10]; //
			}
		}
		return 0;
	}

	public int getPowerToAdd(int stoneId, int oldValue, L2ItemInstance item)
	{
		boolean stone = false/*, crystal = false*/;
		//boolean jewel = false, energy = false;
		for (int id : Elementals.STONES)
		{
			if (id == stoneId)
			{
				stone = true;
				break;
			}
		}
		/*if (!stone)
		{
			for (int id : Elementals.CRYSTALS)
			{
				if (id == stoneId)
				{
					crystal = true;
					break;
				}
			}
			if (!crystal)
			{
				for (int id : Elementals.JEWELS)
				{
					if (id == stoneId)
					{
						//jewel = true;
						break;
					}
				}
				//if (!jewel)
					//energy = true;
			}
		}*/

		if (item.isWeapon())
		{
			if (stone)
			{
				if (oldValue == 0)
					return Elementals.FIRST_WEAPON_BONUS;
				else
					return Elementals.NEXT_WEAPON_BONUS;
			}
		}
		else if (item.isArmor())
		{
			if (stone)
				return Elementals.ARMOR_BONUS;
		}

		// Others not implemented
		return 0;
	}

	@Override
	public String getType()
	{
		return D0_38_REQUEST_EX_ENCHANT_ITEM_ATTRIBUTE;
	}
}

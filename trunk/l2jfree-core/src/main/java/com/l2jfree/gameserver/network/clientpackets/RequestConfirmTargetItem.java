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

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExPutItemResultForVariationMake;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * Format:(ch) d
 * @author  -Wooden-
 */
public final class RequestConfirmTargetItem extends L2GameClientPacket
{
	private static final String _C__D0_29_REQUESTCONFIRMTARGETITEM = "[C] D0:29 RequestConfirmTargetItem";

	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if (item == null || activeChar.getLevel() < 46)
		{
			requestFailed(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		// check if the item is augmentable
		int itemGrade = item.getItem().getItemGrade();
		int itemType = item.getItem().getType2();

		SystemMessageId fail = null;
		if (item.isAugmented())
			fail = SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN;
		else if (itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE2_WEAPON ||
				!item.isDestroyable() || item.isShadowItem() ||
				item.getItem().isCommonItem() || item.isTimeLimitedItem())
			fail = SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM;

		// check if the player can augment
		else if (activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION;
		else if (activeChar.getActiveTradeList() != null)
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING;
		else if (activeChar.isDead())
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD;
		else if (activeChar.isParalyzed() || activeChar.isPetrified())
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED;
		else if (activeChar.isFishing())
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING;
		else if (activeChar.isSitting())
			fail = SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN;

		if (fail != null)
		{
			requestFailed(fail);
			fail = null;
			return;
		}

		sendPacket(new ExPutItemResultForVariationMake(_itemObjId));
		sendPacket(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_29_REQUESTCONFIRMTARGETITEM;
	}
}

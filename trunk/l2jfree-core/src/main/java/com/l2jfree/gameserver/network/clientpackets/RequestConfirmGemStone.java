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
import com.l2jfree.gameserver.network.serverpackets.ExPutCommissionResultForVariationMake;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * Format:(ch) dddd
 * @author  -Wooden-
 */
public final class RequestConfirmGemStone extends L2GameClientPacket
{
	private static final String _C__D0_2B_REQUESTCONFIRMGEMSTONE = "[C] D0:2B RequestConfirmGemStone";
	public static final int GEMSTONE_D = 2130;
	public static final int GEMSTONE_C = 2131;
	public static final int GEMSTONE_B = 2132;

	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private long _gemstoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount= readCompQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		L2ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
		if (targetItem == null || refinerItem == null || gemstoneItem == null)
		{
			requestFailed(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		// Make sure the item is a gemstone
		int gemstoneItemId = gemstoneItem.getItem().getItemId();
		if (gemstoneItemId != GEMSTONE_D && gemstoneItemId != GEMSTONE_C && gemstoneItemId != GEMSTONE_B)
		{
			requestFailed(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		// Check if the gemstoneCount is sufficient
		switch (targetItem.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_C:
				if (_gemstoneCount != 20 || gemstoneItemId != GEMSTONE_D)
				{
					requestFailed(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (_gemstoneCount != 30 || gemstoneItemId != GEMSTONE_D)
				{
					requestFailed(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (_gemstoneCount != 20 || gemstoneItemId != GEMSTONE_C)
				{
					requestFailed(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			case L2Item.CRYSTAL_S:
				if (_gemstoneCount != 25 || gemstoneItemId != GEMSTONE_C)
				{
					requestFailed(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				if (_gemstoneCount != 36 || gemstoneItemId != GEMSTONE_B)
				{
					activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
		}

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_gemstoneItemObjId, _gemstoneCount, gemstoneItemId));
		sendPacket(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_2B_REQUESTCONFIRMGEMSTONE;
	}
}

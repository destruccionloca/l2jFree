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
import com.l2jfree.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * Fromat(ch) dd
 * @author  -Wooden-
 */
public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private static final String _C__D0_2A_REQUESTCONFIRMREFINERITEM = "[C] D0:2A RequestConfirmRefinerItem";
	//to avoid unnecessary string allocation
	private static final String GEMSTONE_D = "Gemstone D";
	private static final String GEMSTONE_C = "Gemstone C";

	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (targetItem == null || refinerItem == null)
		{
			requestFailed(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		int itemGrade = targetItem.getItem().getItemGrade();
		int refinerItemId = refinerItem.getItem().getItemId();
 
		// is the item a life stone?
		if (!refinerItem.getItem().isLifeStone())
		{
			requestFailed(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		int gemstoneCount = 0;
		int gemstoneItemId = 0;
		SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRES_S1_S2);
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
				gemstoneCount = 20;
				gemstoneItemId = RequestConfirmGemStone.GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString(GEMSTONE_D);
				break;
			case L2Item.CRYSTAL_B:
				gemstoneCount = 30;
				gemstoneItemId = RequestConfirmGemStone.GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString(GEMSTONE_D);
				break;
			case L2Item.CRYSTAL_A:
				gemstoneCount = 20;
				gemstoneItemId = RequestConfirmGemStone.GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString(GEMSTONE_C);
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
				gemstoneCount = 25;
				gemstoneItemId = RequestConfirmGemStone.GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString(GEMSTONE_C);
				break;
		}

		sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
		sendPacket(sm);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_2A_REQUESTCONFIRMREFINERITEM;
	}
}

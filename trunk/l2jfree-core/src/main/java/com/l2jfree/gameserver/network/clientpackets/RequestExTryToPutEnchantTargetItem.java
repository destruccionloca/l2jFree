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
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;

/** 
 * @author evill33t
 * 
 */
public class RequestExTryToPutEnchantTargetItem extends AbstractEnchantPacket
{
	private static final String	_C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM	= "[C] D0 4F RequestExTryToPutEnchantTargetItem";

	private int _objectId = 0;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_objectId == 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isEnchanting())
		{
			requestFailed(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return;
		}

		L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();

		if (item == null || scroll == null)
		{
			requestFailed(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			return;
		}

		// template for scroll
		EnchantScroll scrollTemplate = getEnchantScroll(scroll);
		if (!scrollTemplate.isValid(item) || !isEnchantable(item))
		{
			sendPacket(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			return;
		}
		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantTargetItemResult(_objectId));
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM;
	}
}

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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2FeedableBeastInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class BeastSpice implements IItemHandler
{
	// Golden Spice, Crystal Spice
	private static final int[]	ITEM_IDS	=
											{ 6643, 6644 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2Object[] targets = new L2Object[1];
		targets[0] = activeChar.getTarget();

		int itemId = item.getItemId();
		if (itemId == 6643)
		{ // Golden Spice
			activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
		}
		else if (itemId == 6644)
		{ // Crystal Spice
			activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
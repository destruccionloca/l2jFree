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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2FortBallistaInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Ballista Bombs Handler
 *
 * @author Kerberos
 */
public class BallistaBomb implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		9688
	};

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.actor.instance.L2Playable, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (playable == null)
			return;

		L2PcInstance player = null;
		if (playable instanceof L2Summon)
		{
			player = ((L2Summon) playable).getOwner();
			player.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		else if (playable instanceof L2PcInstance)
		{
			player = (L2PcInstance) playable;
		}
		else
			return;

		if (player.getTarget() instanceof L2FortBallistaInstance)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(2342, 1);
			player.useMagic(skill, false, false);
		}
		else
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
	}

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.IItemHandler#getItemIds()
	 */
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}

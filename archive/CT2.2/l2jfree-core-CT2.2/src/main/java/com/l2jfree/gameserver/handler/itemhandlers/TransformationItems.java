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
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class TransformationItems implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int	ITEM_IDS[]	=
											{ 9897, 10131, 10132, 10133, 10134, 10135, 10136, 10137, 10138, 10151, 10274 };

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance client = (L2PcInstance) playable;
		int itemId = item.getItemId();

		if (client.getPet() != null || client.isTransformed() || client.isMounted() || client.isFlying())
		{
			client.sendPacket((new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED)).addItemName(item));
			return;
		}

		int skillId = 0;

		switch (itemId)
		{
		case 9897:
			skillId = 2370;
			break;
		case 10131:
			skillId = 2371;
			break;
		case 10132:
			skillId = 2372;
			break;
		case 10133:
			skillId = 2373;
			break;
		case 10134:
			skillId = 2374;
			break;
		case 10135:
			skillId = 2375;
			break;
		case 10136:
			skillId = 2376;
			break;
		case 10137:
			skillId = 2377;
			break;
		case 10138:
			skillId = 2378;
			break;
		case 10151:
			skillId = 2394;
			break;
		case 10274:
			skillId = 2428;
			break;
		default:
			break;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		client.callSkill(skill, new L2Character[]
		{ client });

		client.destroyItem("Consume", item.getObjectId(), 1, null, true);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
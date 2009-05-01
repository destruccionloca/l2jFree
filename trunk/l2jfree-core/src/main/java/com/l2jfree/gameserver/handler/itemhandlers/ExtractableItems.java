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

import com.l2jfree.gameserver.datatables.ExtractableItemsData;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.items.model.L2ExtractableItem;
import com.l2jfree.gameserver.items.model.L2ExtractableProductItem;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author FBIagent 11/12/2006
 *
 */

public class ExtractableItems implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemID = item.getItemId();
		L2Skill skill = null;
		L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);

		for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			skill = expi.getSkill();
			if (skill != null)
				activeChar.useMagic(skill,false,false);
			return;
		}
	}

	public int[] getItemIds()
	{
		return ExtractableItemsData.getInstance().itemIDs();
	}
}
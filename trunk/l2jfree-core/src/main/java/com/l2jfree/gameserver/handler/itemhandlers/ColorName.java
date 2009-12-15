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

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.clientpackets.RequestChangeNicknameColor;
import com.l2jfree.gameserver.network.serverpackets.ExChangeNicknameNColor;

public class ColorName implements IItemHandler
{
	private static final int[] ITEM_IDS = {
		RequestChangeNicknameColor.COLOR_NAME_1, RequestChangeNicknameColor.COLOR_NAME_2
	};

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		playable.sendPacket(ExChangeNicknameNColor.PACKET);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}

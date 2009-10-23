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
import com.l2jfree.gameserver.instancemanager.MercTicketManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ConfirmDlg;

public class MercTicket implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance player = playable.getActingPlayer();
		MercTicketManager.getInstance().reqPosition(player, item);
		player.sendPacket(new ConfirmDlg(SystemMessageId.PLACE_S1_CURRENT_LOCATION_DIRECTION).addItemName(item));
	}

	public int[] getItemIds()
	{
		return MercTicketManager.getInstance().getItemIds();
	}
}

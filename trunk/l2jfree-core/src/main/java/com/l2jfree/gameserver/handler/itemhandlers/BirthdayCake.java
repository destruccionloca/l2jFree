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

import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2BirthdayHelperInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author savormix
 */
public final class BirthdayCake implements IItemHandler
{
	private static final int BIRTHDAY_HELPER = 32600;
	private static final int[] ITEM_IDS = { 20320 };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#getItemIds()
	 */
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.actor.L2Playable, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) playable;
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(BIRTHDAY_HELPER);
		if (template == null)
		{
			if (player.isGM())
				player.sendMessage("Missing NPC " + BIRTHDAY_HELPER);
			else
				player.sendPacket(SystemMessageId.TRY_AGAIN_LATER);
		}
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;
		L2Spawn spawn = new L2Spawn(template);
		spawn.setLocx(player.getX());
		spawn.setLocy(player.getY());
		spawn.setLocz(player.getZ() + 20);
		spawn.setAmount(1);
		spawn.setHeading(65535 - player.getHeading());
		spawn.setInstanceId(player.getInstanceId());
		L2BirthdayHelperInstance helper = (L2BirthdayHelperInstance) spawn.spawnOne(false);
		spawn.stopRespawn();
		helper.setOwner(player);
	}
}

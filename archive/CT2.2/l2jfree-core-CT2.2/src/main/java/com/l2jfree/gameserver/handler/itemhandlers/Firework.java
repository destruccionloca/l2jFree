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
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.util.FloodProtector.Protected;

/** 
 * This class ... 
 * 
 * @version $Revision: 1.0.0.0.0.0 $ $Date: 2005/09/02 19:41:13 $
 * @author Baghak, Skatershi
 */
public class Firework implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS	=
											{ 6403, 6406, 6407 };

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		int skillId = -1;

		if (!FloodProtector.tryPerformAction(activeChar, Protected.FIREWORK))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}

		switch (itemId)
		{
		case 6403: // Elven Firecracker
			skillId = 2023; // elven_firecracker, xml: 2023
			break;
		case 6406: // Firework
			skillId = 2024; // firework, xml: 2024
			break;
		case 6407: // Large Firework
			skillId = 2025; // large_firework, xml: 2025
			break;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, skillId, 1, 1, 0);

		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);

		if (skill != null)
			activeChar.useMagic(skill, false, false);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
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

import com.l2jfree.gameserver.datatables.ForgottenScrollTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.ForgottenScrollTable.ForgottenScrollData;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Cromir, Kreastr
 */
public final class ForgottenScroll implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		final L2PcInstance activeChar = (L2PcInstance)playable;
		
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isSubClassActive())
		{
			activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
			return;
		}
		
		ForgottenScrollData sd = ForgottenScrollTable.getInstance().getForgottenScroll(item.getItemId(), activeChar.getActiveClass());
		
		if (sd == null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.getLevel() < sd.getMinLevel())
		{
			activeChar.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
			return;
		}
		
		L2Skill sk = activeChar.getKnownSkill(sd.getSkillId());
		
		if (sk != null)
		{
			activeChar.sendMessage("That skill is already learned."); // Retail MSG?
			return;
		}
		
		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);
		
		L2Skill skill = SkillTable.getInstance().getInfo(sd.getSkillId(), 1);
		activeChar.addSkill(skill, true);
		activeChar.sendSkillList();
		activeChar.sendMessage("You learned the skill " + skill.getName()); // Retail MSG?
	}
	
	public int[] getItemIds()
	{
		return ForgottenScrollTable.getInstance().getItemIds();
	}
}

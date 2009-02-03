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
package com.l2jfree.gameserver.handler.skillhandlers;

import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.FloodProtector;

/**
 * @author theonn
 */
public final class SummonHorse implements ISkillHandler
{
	private static final int HORSE_ID = 13130;

	private static final L2SkillType[] SKILL_IDS = { L2SkillType.SUMMON_HORSE };

	public void useSkill(L2Character playable, L2Skill skill, L2Object... targets)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance)playable;

		if (!FloodProtector.tryPerformAction(activeChar, FloodProtector.PROTECTED_ITEMPETSUMMON))
			return;

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}

		if (activeChar.inObserverMode())
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		if (activeChar.getPet() != null || activeChar.isMounted())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}

		if (activeChar.isCursedWeaponEquipped())
		{
			// TODO Get Correct MessageID
			activeChar.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
			return;
		}

		activeChar.mount(HORSE_ID, 0);
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

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
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author theonn
 *
 */
public class SummonHorse implements ISkillHandler
{
	private static final int Horse_Id = 13130;
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SUMMON_HORSE
	};
	
	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#useSkill(com.l2jfree.gameserver.model.L2Character, com.l2jfree.gameserver.model.L2Skill, com.l2jfree.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character playable, L2Skill skill, L2Object[] targets)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_ITEMPETSUMMON))
			return;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if ((activeChar.getPet() != null || activeChar.isMounted()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			//TODO Get Correct MessageID
			activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return;
		}
		
		activeChar.mount(Horse_Id, 0);
	}
	
	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
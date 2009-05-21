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
import com.l2jfree.gameserver.model.L2Trap;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class Trap implements ISkillHandler
{
	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.DETECT_TRAP, SkillType.REMOVE_TRAP };

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#useSkill(com.l2jfree.gameserver.model.L2Character, com.l2jfree.gameserver.model.L2Skill, com.l2jfree.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || skill == null)
			return;

		switch (skill.getSkillType())
		{
		case DETECT_TRAP:
		{
			for (int index = 0; index < targets.length; index++)
			{
				L2Character target = (L2Character) targets[index];

				if (!(target instanceof L2TrapInstance))
					continue;

				if (target.isAlikeDead())
					continue;

				if (((L2Trap) target).getLevel() <= skill.getPower())
				{
					(((L2Trap) target)).setDetected();
					if (activeChar instanceof L2PcInstance)
						((L2PcInstance) activeChar).sendPacket(new SystemMessage(SystemMessageId.S1).addString("A Trap has been detected!"));
				}
			}
			break;
		}
		case REMOVE_TRAP:
		{
			for (int index = 0; index < targets.length; index++)
			{
				L2Character target = (L2Character) targets[index];

				if (!(target instanceof L2Trap))
					continue;

				if (!((L2Trap) target).isDetected())
					continue;

				if (((L2Trap) target).getLevel() > skill.getPower())
					continue;

				L2PcInstance trapOwner = null;
				L2Trap trap = null;
				trapOwner = ((L2Trap) target).getOwner();
				trap = trapOwner.getTrap();

				trap.unSummon(trapOwner);
				if (activeChar instanceof L2PcInstance)
					((L2PcInstance) activeChar).sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
			}
		}
		}
	}

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

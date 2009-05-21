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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public class Trap implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.DETECT_TRAP, L2SkillType.REMOVE_TRAP };

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#useSkill(com.l2jfree.gameserver.model.actor.L2Character, com.l2jfree.gameserver.model.L2Skill, com.l2jfree.gameserver.model.actor.L2Character...)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (skill == null)
			return;
		
		switch (skill.getSkillType())
		{
			case DETECT_TRAP:
			{
				for (L2Character element : targets)
				{
					if (!(element instanceof L2TrapInstance))
						continue;
	
					L2TrapInstance target = (L2TrapInstance) element;
	
					if (target.isAlikeDead())
						continue;
	
					if (target.getLevel() <= skill.getPower())
					{
						target.setDetected();
						if (activeChar instanceof L2PcInstance)
							activeChar.sendMessage("A Trap has been detected!");
					}
				}
				break;
			}
			case REMOVE_TRAP:
			{
				for (L2Character element : targets)
				{
					if (!(element instanceof L2TrapInstance))
						continue;
	
					L2TrapInstance target = (L2TrapInstance) element;
	
					if (!target.isDetected())
						continue;
	
					if (target.getLevel() > skill.getPower())
						continue;
	
					L2PcInstance trapOwner = null;
					trapOwner = target.getOwner();
	
					target.unSummon(trapOwner);
					if (activeChar instanceof L2PcInstance)
						activeChar.sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
				}
			}
		}
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

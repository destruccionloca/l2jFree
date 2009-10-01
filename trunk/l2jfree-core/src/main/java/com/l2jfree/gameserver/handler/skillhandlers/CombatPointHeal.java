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
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public class CombatPointHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.COMBATPOINTHEAL, L2SkillType.CPHEAL_PERCENT };
	
	public void useSkill(L2Character actChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().useSkill(L2SkillType.BUFF, actChar, skill, targets);
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			double cp = skill.getPower();
			
			if (skill.getSkillType() == L2SkillType.CPHEAL_PERCENT)
				cp = target.getMaxCp() * cp / 100;
			
			// From CT2 u will receive exact CP, you can't go over it, if you have full CP and you get CP buff, you will receive 0CP restored message
			cp = Math.min(cp, target.getMaxCp() - target.getStatus().getCurrentCp());
			
			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
				sm.addNumber((int)cp);
				((L2PcInstance)target).sendPacket(sm);
			}
			
			target.getStatus().setCurrentCp(cp + target.getStatus().getCurrentCp());
		}
	}
	
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

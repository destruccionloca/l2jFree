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
package net.sf.l2j.gameserver.handler.skillhandlers;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

/**
 * @author Sephiroth
 */
public class ShiftTarget implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = { SkillType.SHIFT_TARGET};
    
    	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Attackable _attacker = null;
		L2NpcInstance attacker = null;
		L2Character _target = null;
		
		boolean targetShifted = false;
			
		for (L2Object target : targets)
		{
			if (target instanceof L2PcInstance)
			{
				_target = (L2Character)target;
				break;
			}
		}
		
		for (L2Object nearby : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
		{
			if (!targetShifted)
			{
				if (nearby instanceof L2Attackable)
				{
					_attacker = (L2Attackable) nearby;
					targetShifted = true;
					break;
				}
			}
		}

		if (targetShifted)
		{
			attacker = (L2NpcInstance)_attacker;
			int aggro = _attacker.getHating(activeChar);
			
			if (aggro == 0)
			{
				if (_target.isRunning()) attacker.setRunning();
				{
					_attacker.addDamageHate(_target,0,1);
					attacker.setTarget(_target);
					_attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _target);
				}
			}
			else
			{
				_attacker.stopHating(activeChar);
				if (_target.isRunning()) attacker.setRunning();
				{
					_attacker.addDamageHate(_target,0,aggro);
					attacker.setTarget(_target);
					_attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _target);
				}
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

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
package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillCpDrain extends L2Skill
{
	public L2SkillCpDrain(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (target.isAlikeDead())
			{
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
					target.stopFakeDeath(true);
				else
					continue;
			}

			int _cp = (int) target.getStatus().getCurrentCp();
			final int damage = (int) Math.min(getPower(), _cp);
			
			if (damage > 0)
			{
				double newCp = Math.min(activeChar.getStatus().getCurrentCp() + damage, activeChar.getMaxCp());
				activeChar.getStatus().setCurrentCp(newCp);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				getEffects(activeChar, target);
				
				activeChar.sendDamageMessage(target, damage, false, false, false);
				target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() - damage);
			}
		}
	}
}

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
package com.l2jfree.gameserver.skills.conditions;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.Env;

class ConditionTargetActiveSkillId extends Condition
{
	private final int _skillId;
	private final int _minSkillLvl;
	private final int _maxSkillLvl;
	
	ConditionTargetActiveSkillId(int skillId, int minSkillLvl, int maxSkillLvl)
	{
		_skillId = skillId;
		_minSkillLvl = minSkillLvl;
		_maxSkillLvl = maxSkillLvl;
	}
	
	@Override
	boolean testImpl(Env env)
	{
		if (env.target != null)
			for (L2Skill sk : env.target.getAllSkills())
				if (sk != null && sk.getId() == _skillId)
					if (_minSkillLvl == -1 || _minSkillLvl <= sk.getLevel() && sk.getLevel() <= _maxSkillLvl)
						return true;
		
		return false;
	}
}

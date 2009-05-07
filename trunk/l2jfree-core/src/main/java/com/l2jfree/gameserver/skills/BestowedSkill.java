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
package com.l2jfree.gameserver.skills;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.StatsSet;

/**
 * @author NB4L1
 */
public final class BestowedSkill
{
	public static BestowedSkill parse(StatsSet set)
	{
		try
		{
			final int bestowedSkillId = set.getInteger("bestowedSkillId", 0);
			final int bestowedSkillLevel = set.getInteger("bestowedSkillLevel", 0);
			final boolean bestowSkillOnAddition = set.getBool("bestowSkillAutomatically", false);
			
			if (bestowedSkillId > 0 || bestowedSkillLevel > 0)
			{
				if (bestowedSkillId > 0 && bestowedSkillLevel > 0)
					return new BestowedSkill(bestowedSkillId, bestowedSkillLevel, bestowSkillOnAddition);
				else
					throw new IllegalStateException();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private final int _skillId;
	private final int _skillLvl;
	private final boolean _bestowAutomatically;
	
	private BestowedSkill(int skillId, int skillLvl, boolean bestowAutomatically)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
		_bestowAutomatically = bestowAutomatically;
	}
	
	public L2Skill getAutomaticallyBestowedSkill()
	{
		return _bestowAutomatically ? getBestowedSkill() : null;
	}
	
	public L2Skill getEffectBestowedSkill()
	{
		return !_bestowAutomatically ? getBestowedSkill() : null;
	}
	
	private L2Skill getBestowedSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
}

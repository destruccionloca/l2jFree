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
public final class TriggeredSkill
{
	public static TriggeredSkill parse(StatsSet set)
	{
		if (!set.contains("triggeredId") && !set.contains("triggeredLevel"))
			return null;
		
		final int triggeredId = set.getInteger("triggeredId");
		final int triggeredLevel = set.getInteger("triggeredLevel");

		if (triggeredId >= 0 && triggeredLevel >= 0)
			return new TriggeredSkill(triggeredId, triggeredLevel);
		else
			throw new IllegalStateException();
	}

	public static TriggeredSkill parse(Integer triggeredId, Integer triggeredLevel)
	{
		if (triggeredId == null && triggeredLevel == null)
			return null;

		if (triggeredLevel == null)
			triggeredLevel = 1;
		if (triggeredId == null)
			triggeredId = 0;

		if (triggeredId >= 0 && triggeredLevel >= 0)
			return new TriggeredSkill(triggeredId, triggeredLevel);
		else
			throw new IllegalStateException();
	}

	private final int _skillId;
	private final int _skillLvl;

	private TriggeredSkill(int skillId, int skillLvl)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
	}

	public L2Skill getTriggeredSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
}

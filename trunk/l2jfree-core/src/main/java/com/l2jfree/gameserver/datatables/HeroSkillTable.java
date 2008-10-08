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
package com.l2jfree.gameserver.datatables;

import com.l2jfree.gameserver.model.L2Skill;

import javolution.util.FastList;

/**
 *
 * @author G1ta0
 */
public class HeroSkillTable
{
	private static HeroSkillTable		_instance;
	private static FastList<L2Skill>	_heroSkills;
	private static final int[]			_heroSkillsIds	=
														{ 395, 396, 1374, 1375, 1376 };

	private HeroSkillTable()
	{
		_heroSkills = new FastList<L2Skill>();
		for (int _skillId : _heroSkillsIds)
			_heroSkills.add(SkillTable.getInstance().getInfo(_skillId, 1));
	}

	public static HeroSkillTable getInstance()
	{
		if (_instance == null)
			_instance = new HeroSkillTable();
		return _instance;
	}

	public static FastList<L2Skill> getHeroSkills()
	{
		return _heroSkills;
	}

	public static boolean isHeroSkill(int skillId)
	{
		for (L2Skill skill : getHeroSkills())
		{
			if (skill.getId() == skillId)
				return true;
		}
		return false;
	}
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.SkillsEngine;

public final class SkillTable
{
	private static final Log _log = LogFactory.getLog(SkillTable.class);
	
	private static final class SingletonHolder
	{
		private static SkillTable INSTANCE = new SkillTable();
	}
	
	public static SkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public static void reload()
	{
		SingletonHolder.INSTANCE = new SkillTable();
	}
	
	private final L2Skill[][] _skillTable;
	private final int[] _maxLevels;
	
	private SkillTable()
	{
		final List<L2Skill> skills = SkillsEngine.loadSkills();
		_log.info("SkillTable: Loaded " + skills.size() + " skill templates from XML files.");
		
		int highestId = 0;
		for (L2Skill skill : skills)
			if (highestId < skill.getId())
				highestId = skill.getId();
		
		_maxLevels = new int[highestId + 1];
		
		int[] highestLevels = new int[highestId + 1];
		for (L2Skill skill : skills)
		{
			if (highestLevels[skill.getId()] < skill.getLevel())
				highestLevels[skill.getId()] = skill.getLevel();
			
			if (_maxLevels[skill.getId()] < skill.getLevel() && skill.getLevel() < 100)
				_maxLevels[skill.getId()] = skill.getLevel();
		}
		
		_skillTable = new L2Skill[highestId + 1][];
		
		for (int i = 0; i < highestLevels.length; i++)
			_skillTable[i] = new L2Skill[highestLevels[i] + 1];
		
		for (L2Skill skill : skills)
			_skillTable[skill.getId()][skill.getLevel()] = skill;
		
		int length = _skillTable.length;
		for (L2Skill[] array : _skillTable)
			length += array.length;
		
		_log.info("SkillTable: Occupying arrays for " + length + ".");
		
		SingletonHolder.INSTANCE = this;
		
		Map<Integer, L2Skill> skillsByUID = new HashMap<Integer, L2Skill>();
		
		for (L2Skill skill : skills)
		{
			try
			{
				L2Skill old = skillsByUID.put(SkillTable.getSkillUID(skill), skill);
				
				if (old != null)
					throw new IllegalStateException("Overlapping UIDs for: " + old + ", " + skill);
				
				skill.validate();
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
		}
	}
	
	public static int getSkillUID(L2Skill skill)
	{
		return skill == null ? 0 : getSkillUID(skill.getId(), skill.getLevel());
	}
	
	public static int getSkillUID(int skillId, int skillLevel)
	{
		return skillId * 1023 + skillLevel;
	}
	
	public L2Skill getInfo(int skillId, int level)
	{
		if (skillId < 0 || _skillTable.length <= skillId)
			return null;
		
		L2Skill[] array = _skillTable[skillId];
		
		if (array == null)
			return null;
		
		if (level < 0 || array.length <= level)
			return null;
		
		return array[level];
	}
	
	public int getMaxLevel(int skillId)
	{
		if (skillId < 0 || _maxLevels.length <= skillId)
			return 0;
		
		return _maxLevels[skillId];
	}
	
	public int getNormalLevel(L2Skill skill)
	{
		if (skill.getLevel() < 100)
			return skill.getLevel();
		
		return getMaxLevel(skill.getId());
	}
	
	private L2Skill[] _siegeSkills;
	
	public L2Skill[] getSiegeSkills(boolean addNoble)
	{
		if (_siegeSkills == null)
		{
			List<L2Skill> list = new ArrayList<L2Skill>();
			
			list.add(getInfo(246, 1));
			list.add(getInfo(247, 1));
			
			if (addNoble)
				list.add(getInfo(326, 1));
			
			_siegeSkills = list.toArray(new L2Skill[list.size()]);
		}
		
		return _siegeSkills;
	}
}

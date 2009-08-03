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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.SkillsEngine;
import com.l2jfree.lang.L2Integer;

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
	
	public synchronized static void reload()
	{
		SingletonHolder.INSTANCE = new SkillTable();
	}
	
	public static final class SkillInfo
	{
		private final Integer _skillId;
		private final int _level;
		private L2Skill _skill;
		
		private SkillInfo(int skillId, int level)
		{
			_skillId = L2Integer.valueOf(skillId);
			_level = level;
		}
		
		public Integer getId()
		{
			return _skillId;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	private static SkillInfo[][] SKILL_INFOS = new SkillInfo[0][];
	
	private final L2Skill[][] _skillTable;
	private final int[] _maxLevels;
	private final Set<L2Skill> _learnedSkills = new HashSet<L2Skill>();
	
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
		
		// clear previously stored skills
		for (SkillInfo[] infos : SKILL_INFOS)
			if (infos != null)
				for (SkillInfo info : infos)
					if (info != null)
						info._skill = null;
		
		_skillTable = new L2Skill[highestId + 1][];
		
		SKILL_INFOS = Arrays.copyOf(SKILL_INFOS, Math.max(SKILL_INFOS.length, highestId + 1));
		
		for (int i = 0; i < highestLevels.length; i++)
		{
			_skillTable[i] = new L2Skill[highestLevels[i] + 1];
			
			if (SKILL_INFOS[i] == null)
				SKILL_INFOS[i] = new SkillInfo[highestLevels[i] + 1];
			else
				SKILL_INFOS[i] = Arrays.copyOf(SKILL_INFOS[i], Math.max(SKILL_INFOS[i].length, highestLevels[i] + 1));
		}
		
		for (L2Skill skill : skills)
		{
			_skillTable[skill.getId()][skill.getLevel()] = skill;
			
			if (SKILL_INFOS[skill.getId()][skill.getLevel()] == null)
				SKILL_INFOS[skill.getId()][skill.getLevel()] = new SkillInfo(skill.getId(), skill.getLevel());
			
			SKILL_INFOS[skill.getId()][skill.getLevel()]._skill = skill;
		}
		
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
		
		for (L2Skill skill : skills)
		{
			final L2Skill learnedSkill = getInfo(skill.getNewSkillId(), skill.getNewSkillLvl());
			
			if (learnedSkill != null)
				_learnedSkills.add(learnedSkill);
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
	
	public SkillInfo getSkillInfo(int skillId, int level)
	{
		if (skillId < 0 || SKILL_INFOS.length <= skillId)
			return null;
		
		SkillInfo[] array = SKILL_INFOS[skillId];
		
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
	
	public L2Skill[] getSiegeSkills(boolean addNoble)
	{
		final L2Skill[] skills = new L2Skill[addNoble ? 3 : 2];
		
		skills[0] = getInfo(246, 1);
		skills[1] = getInfo(247, 1);
		
		if (addNoble)
			skills[2] = getInfo(326, 1);
		
		return skills;
	}
	
	public boolean isLearnedSkill(L2Skill skill)
	{
		return _learnedSkills.contains(skill);
	}
}

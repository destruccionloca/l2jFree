package com.l2jfree.gameserver.model.actor.skills;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.util.LookupTable;

public final class PcSkills
{
	private static final Log _log = LogFactory.getLog(PcSkills.class);
	
	private final LookupTable<SkillMap> _storedSkills = new LookupTable<SkillMap>();
	private final L2PcInstance _owner;
	
	public PcSkills(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	private L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public void storeSkill(L2Skill skill, int classIndex)
	{
		if (skill == null)
			return;
		
		final SkillMap map = getSkillMap();
		
		final Integer oldLevel = map.put(skill);
		
		checkSkill(skill);
		
		if (oldLevel != null && oldLevel.intValue() == skill.getLevel())
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			if (oldLevel != null)
			{
				PreparedStatement statement = con
						.prepareStatement("UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?");
				statement.setInt(1, skill.getLevel());
				statement.setInt(2, skill.getId());
				statement.setInt(3, getOwner().getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else
			{
				PreparedStatement statement = con
						.prepareStatement("INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)");
				statement.setInt(1, getOwner().getObjectId());
				statement.setInt(2, skill.getId());
				statement.setInt(3, skill.getLevel());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void deleteSkill(L2Skill skill)
	{
		if (skill == null)
			return;
		
		final SkillMap map = getSkillMap();
		
		if (map.remove(skill) == null)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?");
			statement.setInt(1, skill.getId());
			statement.setInt(2, getOwner().getObjectId());
			statement.setInt(3, getOwner().getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restoreSkills()
	{
		final SkillMap map = getSkillMap();
		
		ArrayList<L2Skill> tmp = new ArrayList<L2Skill>();
		
		for (Map.Entry<Integer, Integer> entry : map.entrySet())
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(entry.getKey(), entry.getValue());
			if (skill == null)
				continue;
			
			tmp.add(skill);
		}
		
		L2Skill[] skills = tmp.toArray(new L2Skill[tmp.size()]);
		
		Arrays.sort(skills, getOwner().SKILL_LIST_COMPARATOR);
		
		for (L2Skill skill : skills)
			getOwner().addSkill(skill);
	}
	
	public void deleteSkills(Connection con, int classIndex) throws SQLException
	{
		PreparedStatement statement = con
				.prepareStatement("DELETE FROM character_skills WHERE charId=? AND class_index=?");
		statement.setInt(1, getOwner().getObjectId());
		statement.setInt(2, classIndex);
		statement.execute();
		statement.close();
		
		_storedSkills.remove(classIndex);
	}
	
	private SkillMap getSkillMap()
	{
		SkillMap map = _storedSkills.get(getOwner().getClassIndex());
		
		if (map != null)
			return map;
		
		map = new SkillMap();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?");
			statement.setInt(1, getOwner().getObjectId());
			statement.setInt(2, getOwner().getClassIndex());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLvl = rset.getInt("skill_level");
				
				map.put(skillId, skillLvl);
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if (skill == null)
					continue;
				
				checkSkill(skill);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		_storedSkills.set(getOwner().getClassIndex(), map);
		
		return map;
	}
	
	private void checkSkill(L2Skill skill)
	{
		if (getOwner().isGM() || Config.ALT_GAME_SKILL_LEARN)
			return;
		
		if (getOwner().isTemporarySkill(skill))
			_log.warn("Temporary skill " + skill + " was saved for " + this);
		
		if (getOwner().isStoredSkill(skill))
			return;
	}
	
	private static final class SkillMap extends FastMap<Integer, Integer>
	{
		private static final long serialVersionUID = -222036343002486892L;
		
		public Integer put(L2Skill skill)
		{
			return put(skill.getId(), skill.getLevel());
		}
		
		public Integer get(L2Skill skill)
		{
			return get(skill.getId());
		}
		
		public Integer remove(L2Skill skill)
		{
			return remove(skill.getId());
		}
	}
}

package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillClanGate extends L2Skill
{
	private final int _summonTotalLifeTime;
	
	public L2SkillClanGate(StatsSet set)
	{
		super(set);
		
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 120000); // 2 minutes default
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
}

package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.StatsSet;

/**
 * @author NB4L1
 */
public final class L2SkillPdam extends L2Skill
{
	private final int _numberOfHits;
	
	public L2SkillPdam(StatsSet set)
	{
		super(set);
		
		_numberOfHits = set.getInteger("numberOfHits", 1);
	}
	
	public int getNumberOfHits()
	{
		return _numberOfHits;
	}
}

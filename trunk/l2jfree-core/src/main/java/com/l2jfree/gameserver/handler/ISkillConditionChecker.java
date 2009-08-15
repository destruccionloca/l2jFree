package com.l2jfree.gameserver.handler;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;

/**
 * @author NB4L1
 */
public abstract class ISkillConditionChecker implements ISkillHandler
{
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		return true;
	}
	
	public boolean checkConditions(L2Character activeChar, L2Skill skill, L2Character target)
	{
		return true;
	}
}

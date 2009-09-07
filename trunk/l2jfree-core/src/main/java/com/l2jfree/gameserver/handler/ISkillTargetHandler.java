package com.l2jfree.gameserver.handler;

import java.util.List;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;

/**
 * @author NB4L1
 */
public interface ISkillTargetHandler
{
	public List<L2Character> getTargetList(SkillTargetType type, L2Character activeChar, L2Skill skill, L2Character target);
	
	public SkillTargetType[] getSkillTargetTypes();
}

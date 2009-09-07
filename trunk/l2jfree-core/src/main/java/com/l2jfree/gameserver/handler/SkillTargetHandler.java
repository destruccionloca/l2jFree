package com.l2jfree.gameserver.handler;

import java.util.List;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.util.EnumHandlerRegistry;

/**
 * @author NB4L1
 */
public class SkillTargetHandler extends EnumHandlerRegistry<SkillTargetType, ISkillTargetHandler>
{
	private static final class SingletonHolder
	{
		private static final SkillTargetHandler INSTANCE = new SkillTargetHandler();
	}
	
	public static SkillTargetHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private SkillTargetHandler()
	{
		super(SkillTargetType.class);
		
		_log.info("SkillTargetHandler: Loaded " + size() + " handlers.");
	}
	
	public void registerSkillHandler(ISkillTargetHandler skillTargetList)
	{
		registerAll(skillTargetList, skillTargetList.getSkillTargetTypes());
	}
	
	public List<L2Character> getTargetList(SkillTargetType type, L2Character activeChar, L2Skill skill, L2Character target)
	{
		final List<L2Character> targets = GlobalRestrictions.getTargetList(type, activeChar, skill, target);
		
		if (targets != null)
			return targets;
		
		final ISkillTargetHandler list = get(type);
		
		if (list != null)
			return list.getTargetList(type, activeChar, skill, target);
		
		return null;
	}
	
	public List<L2Character> getTargetList(SkillTargetType type, L2Character activeChar, L2Skill skill)
	{
		return getTargetList(type, activeChar, skill, activeChar.getTarget(L2Character.class));
	}
	
	public List<L2Character> getTargetList(L2Character activeChar, L2Skill skill, L2Character target)
	{
		return getTargetList(skill.getTargetType(), activeChar, skill, target);
	}
	
	public List<L2Character> getTargetList(L2Character activeChar, L2Skill skill)
	{
		return getTargetList(skill.getTargetType(), activeChar, skill, activeChar.getTarget(L2Character.class));
	}
}

package com.l2jfree.gameserver.handler;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;

public interface ICubicSkillHandler extends ISkillHandler
{
	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Object... targets);
}

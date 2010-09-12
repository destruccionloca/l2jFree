package com.l2jfree.gameserver.model;

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignetCasttime;

public final class SignetCasttimeSkill extends CasttimeSkill<L2SkillSignetCasttime>
{
	public SignetCasttimeSkill(L2Character caster, L2Character target, L2SkillSignetCasttime skill)
	{
		super(caster, target, skill);
	}
	
	@Override
	public void onCastAbort()
	{
		super.onCastAbort();
		
		_target.stopSkillEffects(_skill.getId());
	}
}

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
package com.l2jfree.gameserver.model;

import java.util.concurrent.Future;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.effects.EffectFusion;
import com.l2jfree.gameserver.skills.l2skills.L2SkillFusion;
import com.l2jfree.gameserver.util.Util;

/**
 * @author kombat/crion
 */
public final class FusionSkill extends CasttimeSkill<L2SkillFusion> implements Runnable
{
	private final Future<?> _geoCheckTask;
	
	public FusionSkill(L2Character caster, L2Character target, L2SkillFusion skill)
	{
		super(caster, target, skill);
		
		EffectFusion effect = getFusionTriggeredEffect();
		
		if (effect != null)
			effect.increaseEffect();
		
		else
			skill.getFusionTriggeredSkill().getEffects(_caster, _target);
		
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 1000, 1000);
	}
	
	private EffectFusion getFusionTriggeredEffect()
	{
		return (EffectFusion)_target.getFirstEffect(_skill.getFusionTriggeredSkill().getId());
	}
	
	@Override
	public void onCastAbort()
	{
		super.onCastAbort();
		
		EffectFusion effect = getFusionTriggeredEffect();
		if (effect != null)
			effect.decreaseEffect();
		
		_geoCheckTask.cancel(true);
	}
	
	@Override
	public void run()
	{
		if (!Util.checkIfInRange(_skill.getCastRange(), _caster, _target, true))
			_caster.abortCast();
		
		else if (!GeoData.getInstance().canSeeTarget(_caster, _target))
			_caster.abortCast();
	}
}

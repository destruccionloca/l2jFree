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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.effects.EffectFusion;
import com.l2jfree.gameserver.util.Util;

/**
 * @author kombat/crion
 */
public final class FusionSkill implements Runnable
{
	private static final Log _log = LogFactory.getLog(FusionSkill.class);
	
	private final L2Character _caster;
	private final L2Character _target;
	private final L2Skill _skill;
	
	private final Future<?> _geoCheckTask;
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public FusionSkill(L2Character caster, L2Character target, L2Skill skill)
	{
		_caster = caster;
		_target = target;
		_skill = skill;
		
		EffectFusion effect = getTriggeredEffect();
		
		if (effect != null)
			effect.increaseEffect();
		else
		{
			L2Skill triggered = skill.getTriggeredSkill();
			
			if (triggered != null)
				triggered.getEffects(_caster, _target);
			else
				_log.warn("Triggered skill for " + skill + " not found!");
		}
		
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 1000, 1000);
	}
	
	private EffectFusion getTriggeredEffect()
	{
		return (EffectFusion)_target.getFirstEffect(_skill.getTriggeredSkillId());
	}
	
	public void onCastAbort()
	{
		_caster.setFusionSkill(null);
		
		EffectFusion effect = getTriggeredEffect();
		if (effect != null)
			effect.decreaseEffect();
		
		_geoCheckTask.cancel(true);
	}
	
	public void run()
	{
		if (!Util.checkIfInRange(_skill.getCastRange(), _caster, _target, true))
			_caster.abortCast();
		
		else if (!GeoData.getInstance().canSeeTarget(_caster, _target))
			_caster.abortCast();
	}
}

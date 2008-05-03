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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.tools.random.Rnd;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.skills.Env;

/**
 * 
 * @author Darki699
 */
public final class EffectConditionHit extends L2Effect
{

	private boolean wasHit = false;
	
	public EffectConditionHit(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONDITION_HIT;
	}

	@Override
	public void onStart()
	{
	}

	@Override
	public void onExit()
	{
		if (getEffected() == null)
			return;
		
		else if (wasHit)
		{
			onHit();
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	public void onHit()
	{
		L2Skill ts = getSkill().getTriggeredSkill(); 
		if (ts != null)
		{
			if (Rnd.get(100) < ts.getLandingPercent())
			{
				getEffected().doCast(ts);

				L2Effect effects[] = ts.getEffects(getEffected(), getEffected());
				if (effects != null)
				{
					for (L2Effect effect : effects)
					{
						if (effect.getTotalTaskTime()>0)
							effect.setFirstTime(getElapsedTaskTime());
					}
				}
			}
		}
	}
	
	public void setWasHit(boolean value)
	{
		wasHit = value;
	}
}

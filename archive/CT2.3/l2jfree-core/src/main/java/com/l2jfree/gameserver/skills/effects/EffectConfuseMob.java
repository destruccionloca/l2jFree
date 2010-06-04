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
package com.l2jfree.gameserver.skills.effects;

import java.util.List;

import javolution.util.FastList;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.tools.random.Rnd;

/**
 * @author littlecrow Implementation of the Confusion Effect
 */
public final class EffectConfuseMob extends L2Effect
{
	
	public EffectConfuseMob(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CONFUSE_MOB_ONLY;
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		getEffected().startConfused();
		onActionTime();
		return true;
	}
	
	/** Notify exited */
	@Override
	protected void onExit()
	{
		getEffected().stopConfused(false);
	}
	
	@Override
	protected boolean onActionTime()
	{
		List<L2Character> targetList = new FastList<L2Character>();
		
		// Getting the possible targets
		
		for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2Attackable && obj != getEffected())
				targetList.add((L2Character)obj);
		}
		// if there is no target, exit function
		if (targetList.isEmpty())
			return true;
		
		// Choosing randomly a new target
		int nextTargetIdx = Rnd.nextInt(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		// getEffected().setTarget(target);
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		
		return true;
	}
}

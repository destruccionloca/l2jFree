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

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.skills.Env;

/**
 * @author decad
 * 
 * Implementation of the CANCEL TARGET
 */
public final class EffectCancelTarget extends L2Effect
{

	public EffectCancelTarget(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CANCEL_TARGET;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		getEffected().setTarget(null);
		getEffected().breakAttack();
		getEffected().breakCast();
		getEffected().abortAttack();
		getEffected().abortCast();
		onActionTime();
	}

	@Override
	public void onExit()
	{

	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}

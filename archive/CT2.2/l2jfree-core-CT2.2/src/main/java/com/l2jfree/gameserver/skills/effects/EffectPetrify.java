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

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

public final class EffectPetrify extends L2Effect
{
	public EffectPetrify(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PETRIFY;
	}
	
	@Override
	protected boolean onStart()
	{
		getEffected().startParalyze();
		getEffected().setIsPetrified(true);
		return true;
	}
	
	@Override
	protected void onExit()
	{
		getEffected().stopParalyze(false);
		getEffected().setIsPetrified(false);
	}
	
	@Override
	protected int getTypeBasedAbnormalEffect()
	{
		return L2Character.ABNORMAL_EFFECT_HOLD_2;
	}
}

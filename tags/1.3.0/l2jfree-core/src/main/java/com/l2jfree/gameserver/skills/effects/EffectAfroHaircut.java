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
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author Kerberos
 */
public class EffectAfroHaircut extends L2Effect
{
	public EffectAfroHaircut(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.AFROHAIR;
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#onStart()
	 */
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance)getEffected()).setAfroHaircutId(getSkill().getAfroColor());
			return true;
		}
		return false;
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		((L2PcInstance)getEffected()).setAfroHaircutId(0);
	}
}

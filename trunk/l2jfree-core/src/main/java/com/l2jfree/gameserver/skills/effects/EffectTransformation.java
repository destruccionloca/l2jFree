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

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author nBd
 */
public final class EffectTransformation extends L2Effect
{
	public EffectTransformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectTransformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TRANSFORMATION;
	}
	
	@Override
	protected boolean onStart()
	{
		if (getEffected().isAlikeDead())
			return false;
		
		if (!(getEffected() instanceof L2PcInstance))
			return false;
		
		L2PcInstance trg = (L2PcInstance)getEffected();
		
		// No transformation if dead or cursed by cursed weapon
		if (trg.isAlikeDead() || trg.isCursedWeaponEquipped())
			return false;
		
		int transformId = getSkill().getTransformId();
		
		if (!trg.isTransformed())
		{
			TransformationManager.getInstance().transformPlayer(transformId, trg);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onExit()
	{
		getEffected().stopTransformation(false);
	}
}

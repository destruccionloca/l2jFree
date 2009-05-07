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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author kombat/crion
 */
final class EffectBestowSkill extends L2Effect
{
	public EffectBestowSkill(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	protected boolean onStart()
	{
		final L2Skill tempSkill = getSkill().getEffectBestowedSkill();
		if (tempSkill == null)
			return false;
		
		// Removing every other skill bestowing effect with the same stack type
		for (L2Effect e : getEffected().getAllEffects())
			if (e != null && e != this && e instanceof EffectBestowSkill)
				if (e.getStackType().equals(getStackType()))
					e.exit();
		
		getEffected().addSkill(tempSkill);
		return true;
	}
	
	@Override
	protected void onExit()
	{
		getEffected().removeSkill(getSkill().getEffectBestowedSkill());
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}

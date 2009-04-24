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
package com.l2jfree.gameserver.skills.conditions;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.skills.Env;

class ConditionTargetActiveEffectId extends Condition
{
	private final int _effectId;
	private final int _minEffectLvl;
	private final int _maxEffectLvl;
	
	ConditionTargetActiveEffectId(int effectId, int minEffectLvl, int maxEffectLvl)
	{
		_effectId = effectId;
		_minEffectLvl = minEffectLvl;
		_maxEffectLvl = maxEffectLvl;
	}
	
	@Override
	boolean testImpl(Env env)
	{
		if (env.target != null)
			for (L2Effect e : env.target.getAllEffects())
				if (e != null && e.getId() == _effectId)
					if (_minEffectLvl == -1 || _minEffectLvl <= e.getLevel() && e.getLevel() <= _maxEffectLvl)
						return true;
		
		return false;
	}
}

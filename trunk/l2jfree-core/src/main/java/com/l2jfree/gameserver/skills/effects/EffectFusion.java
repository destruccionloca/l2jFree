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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author kombat
 */
public final class EffectFusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;

	public EffectFusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = SkillTable.getInstance().getMaxLevel(getSkill().getId());
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FUSION;
	}

	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			exit();
			_effect++;
			renewBuff();
		}
	}

	public void decreaseEffect()
	{
		_effect--;
		exit();
		if (_effect >= 1)
			renewBuff();
	}

	private void renewBuff()
	{
		SkillTable.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
	
	@Override
	public boolean canBeStoredInDb()
	{
		return false;
	}
}

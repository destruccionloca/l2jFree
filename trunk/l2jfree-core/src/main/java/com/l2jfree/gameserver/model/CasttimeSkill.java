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

import com.l2jfree.gameserver.model.actor.L2Character;

public abstract class CasttimeSkill<T extends L2Skill>
{
	protected final L2Character _caster;
	protected final L2Character _target;
	protected final T _skill;
	
	protected CasttimeSkill(L2Character caster, L2Character target, T skill)
	{
		_caster = caster;
		_target = target;
		_skill = skill;
	}
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public void onCastAbort()
	{
		_caster.setCasttimeSkill(null);
	}
}

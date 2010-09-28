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
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignetCasttime;

public final class SignetCasttimeSkill extends CasttimeSkill<L2SkillSignetCasttime>
{
	public SignetCasttimeSkill(L2Character caster, L2Character target, L2SkillSignetCasttime skill)
	{
		super(caster, target, skill);
	}
	
	@Override
	public void onCastAbort()
	{
		super.onCastAbort();
		
		_target.stopSkillEffects(_skill.getId());
	}
}

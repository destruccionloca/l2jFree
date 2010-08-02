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
package com.l2jfree.gameserver.ai;

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Character.AIAccessor;
import com.l2jfree.gameserver.skills.SkillUsageRequest;

/**
 * Everything l2jserver uses this class for is done in GlobalRestrictions.
 * Maintained for compatibility purposes.
 * @see com.l2jfree.gameserver.model.restriction.global.CursedWeaponRestriction
 * @see com.l2jfree.gameserver.model.restriction.global.ProtectionBlessingRestriction
 * @author JIV
 */
public abstract class L2PlayableAI extends L2CharacterAI
{
	/**
	 * @param accessor
	 */
	public L2PlayableAI(AIAccessor accessor)
	{
		super(accessor);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.ai.L2CharacterAI#onIntentionAttack(com.l2jfree.gameserver.model.actor.L2Character)
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		super.onIntentionAttack(target);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.ai.L2CharacterAI#onIntentionCast(com.l2jfree.gameserver.skills.SkillUsageRequest)
	 */
	@Override
	protected void onIntentionCast(SkillUsageRequest request)
	{
		super.onIntentionCast(request);
	}
}

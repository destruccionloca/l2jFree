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
package com.l2jfree.gameserver.model.restriction.global;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;

/**
 * @author NB4L1
 */
final class DefaultRestriction extends AbstractRestriction
{
	@Override
	public boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if (skill.isPassive() || !skill.hasEffects())
			return false;
		
		if (activeChar == target)
			return true;
		
		// doors and siege flags cannot receive any effects
		if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			return false;
		
		if (target.isInvul())
			return false;
		
		if (skill.isOffensive() || skill.isDebuff())
		{
			L2PcInstance activePlayer = activeChar.getActingPlayer();
			
			if (activePlayer != null && activePlayer.isGM())
			{
				if (activePlayer.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					return false;
			}
		}
		
		return true;
	}
}

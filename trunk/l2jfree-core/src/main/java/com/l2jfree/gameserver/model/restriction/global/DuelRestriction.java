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

import com.l2jfree.gameserver.instancemanager.DuelManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Duel;

/**
 * @author NB4L1
 */
final class DuelRestriction extends AbstractRestriction
{
	@Override
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar.isInDuel() || target.isInDuel())
			return false;
		
		return true;
	}
	
	@Override
	public boolean isInvul(L2Character activeChar, L2Character target, boolean isOffensive)
	{
		return Duel.isInvul(target, activeChar);
	}
	
	@Override
	public void effectCreated(L2Effect effect)
	{
		// Let the duel manager know about it, to remove it after the duel
		// so the debuff can be removed after the duel
		// (player & target must be in the same duel)
		L2PcInstance effectedPlayer = effect.getEffected().getActingPlayer();
		
		if (effectedPlayer == null || !effectedPlayer.isInDuel() || !effect.getSkill().isOffensive())
			return;
		
		DuelManager.getInstance().onBuff(effectedPlayer, effect);
	}
}

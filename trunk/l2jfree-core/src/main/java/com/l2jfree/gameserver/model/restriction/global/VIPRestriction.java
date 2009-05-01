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
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.VIP;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * @author NB4L1
 */
final class VIPRestriction extends AbstractRestriction
{
	@Override
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (VIP._started && !Config.VIP_ALLOW_INTERFERENCE && !activeChar.isGM())
		{
			if (target._inEventVIP != activeChar._inEventVIP)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isInvul(L2Character activeChar, L2Character target, boolean isOffensive)
	{
		L2PcInstance attacker_ = L2Object.getActingPlayer(activeChar);
		L2PcInstance target_ = L2Object.getActingPlayer(target);
		
		if (attacker_ == null || target_ == null || attacker_ == target_)
			return false;
		
		if (attacker_._inEventVIP != target_._inEventVIP && !Config.VIP_ALLOW_INTERFERENCE)
			return true;
		
		return false;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (VIP._savePlayers.contains(activeChar.getName()))
			VIP.addDisconnectedPlayer(activeChar);
	}
}

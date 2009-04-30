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
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * @author NB4L1
 */
final class TvTRestriction extends AbstractRestriction
{
	@Override
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (TvT._started && !Config.TVT_ALLOW_INTERFERENCE && !activeChar.isGM())
		{
			if (target._inEventTvT != activeChar._inEventTvT)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void levelChanged(L2PcInstance activeChar)
	{
		if (activeChar._inEventTvT && TvT._maxlvl == activeChar.getLevel() && !TvT._started)
		{
			TvT.removePlayer(activeChar);
			
			activeChar.sendMessage("Your event sign up was canceled.");
		}
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (TvT._savePlayers.contains(activeChar.getName()))
			TvT.addDisconnectedPlayer(activeChar);
	}
}

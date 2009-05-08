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
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.CTF;

/**
 * @author NB4L1
 */
final class CTFRestriction extends AbstractFunEventRestriction
{
	@Override
	boolean started()
	{
		return CTF._started;
	}
	
	@Override
	boolean allowInterference()
	{
		return Config.CTF_ALLOW_INTERFERENCE;
	}
	
	@Override
	boolean isInFunEvent(L2PcInstance player)
	{
		return player._inEventCTF;
	}
	
	@Override
	public void levelChanged(L2PcInstance activeChar)
	{
		if (activeChar._inEventCTF && CTF._maxlvl == activeChar.getLevel() && !CTF._started)
		{
			CTF.removePlayer(activeChar);
			
			activeChar.sendMessage("Your event sign up was canceled.");
		}
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (CTF._savePlayers.contains(activeChar.getName()))
			CTF.addDisconnectedPlayer(activeChar);
	}
	
	@Override
	public boolean playerKilled(L2Character activeChar, final L2PcInstance target)
	{
		if (!target._inEventCTF)
			return false;
		
		if (CTF._teleport || CTF._started)
		{
			target.sendMessage("You will be revived and teleported to team flag in " + Config.CTF_REVIVE_DELAY / 1000
				+ " seconds!");
			
			if (target._haveFlagCTF)
			{
				CTF._flagsTaken.set(CTF._teams.indexOf(target._teamNameHaveFlagCTF), false);
				CTF.spawnFlag(target._teamNameHaveFlagCTF);
				CTF.removeFlagFromPlayer(target);
				target.broadcastUserInfo();
				target._haveFlagCTF = false;
				CTF.AnnounceToPlayers(false, CTF._eventName + "(CTF): " + target._teamNameHaveFlagCTF
					+ "'s flag returned.");
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				public void run()
				{
					int x = CTF._teamsX.get(CTF._teams.indexOf(target._teamNameCTF));
					int y = CTF._teamsY.get(CTF._teams.indexOf(target._teamNameCTF));
					int z = CTF._teamsZ.get(CTF._teams.indexOf(target._teamNameCTF));
					
					target.teleToLocation(x, y, z, false);
					target.doRevive();
				}
			}, Config.CTF_REVIVE_DELAY);
		}
		
		return true;
	}
}

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
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;

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
	public boolean isInvul(L2Character activeChar, L2Character target, boolean isOffensive)
	{
		L2PcInstance attacker_ = L2Object.getActingPlayer(activeChar);
		L2PcInstance target_ = L2Object.getActingPlayer(target);
		
		if (attacker_ == null || target_ == null || attacker_ == target_)
			return false;
		
		if (attacker_._inEventTvT != target_._inEventTvT && !Config.TVT_ALLOW_INTERFERENCE)
			return true;
		
		return false;
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
	
	@Override
	public boolean playerKilled(L2Character activeChar, final L2PcInstance target)
	{
		if (!target._inEventTvT)
			return false;
		
		if (TvT._teleport || TvT._started)
		{
			L2PcInstance pk = activeChar.getActingPlayer();
			
			if (pk != null && pk._inEventTvT)
			{
				if (!(pk._teamNameTvT.equals(target._teamNameTvT)))
				{
					target._countTvTdies++;
					pk._countTvTkills++;
					//pk.setTitle("Kills: " + ((L2PcInstance) killer)._countTvTkills);
					pk.sendPacket(new PlaySound(0, "ItemSound.quest_itemget", 1, target.getObjectId(), target.getX(), target.getY(), target.getZ()));
					TvT.setTeamKillsCount(pk._teamNameTvT, TvT.teamKillsCount(pk._teamNameTvT) + 1);
				}
				else
				{
					pk.sendMessage("You are a teamkiller! Teamkills are not allowed, you will get death penalty and your team will lose one kill!");
					
					// Give Penalty for Team-Kill:
					// 1. Death Penalty + 5
					// 2. Team will lost 1 Kill
					if (pk.getDeathPenaltyBuffLevel() < 10)
					{
						pk.setDeathPenaltyBuffLevel(pk.getDeathPenaltyBuffLevel() + 4);
						pk.increaseDeathPenaltyBuffLevel();
					}
					TvT.setTeamKillsCount(target._teamNameTvT, TvT.teamKillsCount(target._teamNameTvT) - 1);
				}
			}
			
			target.sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000
				+ " seconds!");
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				public void run()
				{
					int x = TvT._teamsX.get(TvT._teams.indexOf(target._teamNameTvT));
					int y = TvT._teamsY.get(TvT._teams.indexOf(target._teamNameTvT));
					int z = TvT._teamsZ.get(TvT._teams.indexOf(target._teamNameTvT));
					
					target.teleToLocation(x, y, z, false);
					target.doRevive();
				}
			}, Config.TVT_REVIVE_DELAY);
		}
		
		return true;
	}
}

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

/**
 * @author NB4L1
 */
final class TvTiRestriction// extends AbstractFunEventRestriction
{
	/*
	@Override
	boolean started()
	{
		return activeChar.getActingPlayer().
	}
	
	@Override
	boolean allowSummon()
	{
		return Config.TVT_ALLOW_SUMMON;
	}
	
	@Override
	boolean allowPotions()
	{
		return Config.TVT_ALLOW_POTIONS;
	}
	
	@Override
	boolean allowInterference()
	{
		return false;
	}
	
	@Override
	boolean isInFunEvent(L2PcInstance player)
	{
		return player._inEventTvT;
	}
	
	@Override
	public void levelChanged(L2PcInstance activeChar)
	{
		if (activeChar._inEventTvT && TvTIMain.getM _maxlvl == activeChar.getLevel() && !TvT._started)
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
		
		if (((killer instanceof L2PcInstance && ((L2PcInstance) killer)._inEventTvTi) || (killer instanceof L2Summon && ((L2Summon) killer).getOwner()._inEventTvTi)) && _inEventTvTi)
		{
			L2PcInstance tempKiller = null;
			if (killer instanceof L2Summon)
				tempKiller = ((L2Summon) killer).getOwner();
			else
				tempKiller = (L2PcInstance) killer;
			if (!TvTIMain.checkSameTeam(tempKiller, this))
			{
				PlaySound ps;
				ps = new PlaySound(0, "ItemSound.quest_itemget", 1, getObjectId(), getX(), getY(), getZ());
				tempKiller._countTvTiKills++;
				tempKiller.setTitle("Kills: " + tempKiller._countTvTiKills);
				tempKiller.sendPacket(ps);
				TvTIMain.addKill(tempKiller);
			}
			else if (TvTIMain.checkSameTeam(tempKiller, this))
			{
				tempKiller.sendMessage("You are a teamkiller! Teamkills are not allowed, you will get death penalty and your team will lose one kill!");
				tempKiller._countTvTITeamKills++;
				// Give Penalty for Team-Kill:
				// 1. Death Penalty + 5
				// 2. Team will lost 1 Kill
				if (tempKiller.getDeathPenaltyBuffLevel() < 10)
				{
					tempKiller.setDeathPenaltyBuffLevel(tempKiller.getDeathPenaltyBuffLevel() + 4);
					tempKiller.increaseDeathPenaltyBuffLevel();
				}
				TvTIMain.removePoint(tempKiller);
				if (tempKiller._countTvTITeamKills >= 2)
					TvTIMain.kickPlayerFromEvent(tempKiller, 1);
			}
			TvTIMain.respawnPlayer(this);
		}
	}
	
	@Override
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		if (command.startsWith("tvti_player_join_page"))
		{
			TvTIMain.showInstancesHtml(activeChar, String.valueOf(TvTIMain.getJoinNpc().getLastSpawn().getObjectId()));
		}

		else if (command.startsWith("tvti_player_join "))
		{
			int instanceId = Integer.parseInt(command.substring(17));

			TvTIMain.addPlayer(activeChar, instanceId);
		}

		else if (command.startsWith("tvti_player_leave"))
		{
			TvTIMain.removePlayer(activeChar);
		}		
		return false;
	}
	
	@Override
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		if (npc._isEventMobTvTi)
		{
			TvTIMain.showEventHtml(activeChar, String.valueOf(npc.getObjectId()));
			return true;
		}
		
		return false;
	}*/
}

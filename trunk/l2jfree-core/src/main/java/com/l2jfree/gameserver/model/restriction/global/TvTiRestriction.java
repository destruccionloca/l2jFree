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
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.TvTInstanced.TvTIMain;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;

/**
 * @author NB4L1
 */
public final class TvTiRestriction extends AbstractFunEventRestriction
{
	private static final class SingletonHolder
	{
		private static final TvTiRestriction INSTANCE = new TvTiRestriction();
	}
	
	public static TvTiRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private TvTiRestriction()
	{
	}
	
	@Override
	boolean started()
	{
		return true;
	}
	
	@Override
	boolean allowSummon()
	{
		return Config.TVTI_ALLOW_SUMMON;
	}
	
	@Override
	boolean allowPotions()
	{
		return Config.TVTI_ALLOW_POTIONS;
	}
	
	@Override
	boolean allowInterference()
	{
		return Config.TVTI_ALLOW_INTERFERENCE;
	}
	
	@Override
	boolean joinCursed()
	{
		return Config.TVTI_JOIN_CURSED;
	}
	
	@Override
	boolean isInFunEvent(L2PcInstance player)
	{
		return player._inEventTvTi;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (TvTIMain.isPlayerInList(activeChar))
			TvTIMain.addDisconnectedPlayer(activeChar);
	}
	
	@Override
	public boolean playerKilled(L2Character killer, final L2PcInstance target)
	{
		if (!target._inEventTvT)
			return false;
		
		final L2PcInstance tempKiller = killer.getActingPlayer();
		
		if (tempKiller == null || !tempKiller._inEventTvTi || !target._inEventTvTi)
			return false;
		
		if (!TvTIMain.checkSameTeam(tempKiller, target))
		{
			tempKiller._countTvTiKills++;
			tempKiller.setTitle("Kills: " + tempKiller._countTvTiKills);
			tempKiller.sendPacket(new PlaySound(0, "ItemSound.quest_itemget", 1, target.getObjectId(), target.getX(),
				target.getY(), target.getZ()));
			TvTIMain.addKill(tempKiller);
		}
		else if (TvTIMain.checkSameTeam(tempKiller, target))
		{
			tempKiller
				.sendMessage("You are a teamkiller! Teamkills are not allowed, you will get death penalty and your team will lose one kill!");
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
		TvTIMain.respawnPlayer(target);
		
		return true;
	}
	
	@Override
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		if (command.startsWith("tvti_player_join_page"))
		{
			TvTIMain.showInstancesHtml(activeChar, String.valueOf(TvTIMain.getJoinNpc().getLastSpawn().getObjectId()));
			return true;
		}
		else if (command.startsWith("tvti_player_join "))
		{
			int instanceId = Integer.parseInt(command.substring(17));
			
			TvTIMain.addPlayer(activeChar, instanceId);
			return true;
		}
		else if (command.startsWith("tvti_player_leave"))
		{
			TvTIMain.removePlayer(activeChar);
			return true;
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
	}
}

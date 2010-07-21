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
package com.l2jfree.gameserver.model.actor.instance;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2FortSiegeGuardAI;
import com.l2jfree.gameserver.ai.L2SiegeGuardAI;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Guard;
import com.l2jfree.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfree.gameserver.model.actor.knownlist.DefenderKnownList;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class L2DefenderInstance extends L2Guard
{
	public L2DefenderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public int getMyTargetSelectedColor(L2PcInstance player)
	{
		return player.getLevel() - getLevel();
	}
	
	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList)_knownList;
	}
	
	@Override
	public CharKnownList initKnownList()
	{
		return new DefenderKnownList(this);
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		synchronized (this)
		{
			if (getCastle() == null)
				return new L2FortSiegeGuardAI(new AIAccessor());
			else
				return new L2SiegeGuardAI(new AIAccessor());
		}
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR>
	 * <BR>
	 * 
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Summons and traps are attackable, too
		L2PcInstance player = L2Object.getActingPlayer(attacker);
		if (player == null)
			return false;
		
		if (shouldAttack(player))
			return true;
		
		return false;
	}
	
	private int getActiveSiegeId(L2PcInstance player)
	{
		if (player == null)
			return -1;
		
		final Castle castle = getCastle();
		final Fort fortress = getFort();
		
		// Check if siege is in progress
		if (fortress != null && fortress.getSiege().getIsInProgress())
			return fortress.getFortId();
		
		if (castle != null && castle.getSiege().getIsInProgress())
			return castle.getCastleId();
		
		return -1;
	}
	
	public boolean shouldAttack(L2PcInstance player)
	{
		final int activeSiegeId = getActiveSiegeId(player);
		
		if (activeSiegeId == -1)
			return false;
		
		// Check if player is an enemy of this defender npc
		if (player.getSiegeState() != L2PcInstance.SIEGE_STATE_DEFENDER
				|| !player.isRegisteredOnThisSiegeField(activeSiegeId))
			return true;
		
		return false;
	}
	
	public boolean shouldDefend(L2PcInstance player)
	{
		final int activeSiegeId = getActiveSiegeId(player);
		
		if (activeSiegeId == -1)
			return false;
		
		// Check if player is an enemy of this defender npc
		if (player.getSiegeState() == L2PcInstance.SIEGE_STATE_DEFENDER
				&& player.isRegisteredOnThisSiegeField(activeSiegeId))
			return true;
		
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	protected int getMaxAllowedDistanceFromHome()
	{
		return 40;
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		// TODO: 1.4.0
		if (Config.SIEGE_ONLY_REGISTERED)
		{
			boolean opp = false;
			Siege siege = SiegeManager.getInstance().getSiege(player);
			FortSiege fortSiege = FortSiegeManager.getInstance().getSiege(player);
			L2Clan oppClan = player.getClan();
			//Castle Sieges
			if (siege != null && siege.getIsInProgress() && oppClan != null)
			{
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
				
				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
			}
			//Fort Sieges
			else if (fortSiege != null && fortSiege.getIsInProgress() && oppClan != null)
			{
				for (L2SiegeClan clan : fortSiege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
			}
			
			if (!opp)
				return;
		}
		// TODO: 1.4.0
		
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2DefenderInstance))
		{
			if (damage == 0 && aggro == 0)
				if (shouldDefend(attacker.getActingPlayer()))
					return;
			
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}

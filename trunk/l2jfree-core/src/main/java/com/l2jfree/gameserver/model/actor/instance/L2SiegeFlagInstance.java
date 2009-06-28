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
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.status.SiegeFlagStatus;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public final class L2SiegeFlagInstance extends L2Npc
{
	private L2Clan _clan;
	private L2PcInstance _player;
	private Siege _siege;
	private FortSiege _fortSiege;
	private boolean _isAdvanced;
	private long _talkProtectionTime;

	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced)
	{
		super(objectId, template);

		_isAdvanced = advanced;
		_player = player;
		_clan = player == null ? null : player.getClan();
		_talkProtectionTime = 0;
		if (_clan == null)
		{
			deleteMe();
			return;
		}
		_siege = SiegeManager.getInstance().getSiege(_player);
		_fortSiege = FortSiegeManager.getInstance().getSiege(_player);

		if (_siege == null && _fortSiege == null)
		{
			deleteMe();
			return;
		}

		if (_siege != null && _fortSiege == null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
			if (sc == null)
				deleteMe();
			else
				sc.addFlag(this);
		}
		else if (_siege == null && _fortSiege != null)
		{
			L2SiegeClan sc = _fortSiege.getAttackerClan(_player.getClan());
			if (sc == null)
				deleteMe();
			else
				sc.addFlag(this);
		}
	}

	@Override
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return true;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (_siege != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
			if (sc != null)
				sc.removeFlag(this);
		}
		else if (_fortSiege != null)
		{
			L2SiegeClan sc = _fortSiege.getAttackerClan(_player.getClan());
			if (sc != null)
				sc.removeFlag(this);
		}

		return true;
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if(!_player.canBeTargetedByAtSiege(player) && Config.SIEGE_ONLY_REGISTERED)
			return;
		
		if (player == null || !canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp() );
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
			player.sendPacket(su);
		}
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	public void flagAttacked()
	{
		// send warning to owners of headquarters that theirs base is under attack
		if (_clan != null && canTalk())
			_clan.broadcastToOnlineMembers(SystemMessageId.BASE_UNDER_ATTACK.getSystemMessage());
		
		_talkProtectionTime = System.currentTimeMillis() + 20000;
	}
	
	public boolean canTalk()
	{
		return System.currentTimeMillis() > _talkProtectionTime;
	}
	
	@Override
	public SiegeFlagStatus getStatus()
	{
		if (_status == null)
			_status = new SiegeFlagStatus(this);
		
		return (SiegeFlagStatus)_status;
	}
	
	public boolean isAdvanced()
	{
		return _isAdvanced;
	}
}
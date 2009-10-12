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

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.handler.itemhandlers.Potions;
import com.l2jfree.gameserver.handler.itemhandlers.SummonItems;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;

/**
 * @author NB4L1
 */
abstract class AbstractFunEventRestriction extends AbstractRestriction
{
	abstract boolean started();
	
	abstract boolean allowSummon();
	
	abstract boolean allowPotions();
	
	abstract boolean allowInterference();
	
	boolean sitForced()
	{
		return false;
	}
	
	boolean joinCursed()
	{
		return false;
	}
	
	boolean reviveRecovery()
	{
		return false;
	}
	
	abstract boolean isInFunEvent(L2PcInstance player);
	
	@Override
	public final boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (isInFunEvent(activeChar))
		{
			if (callingRestriction == CursedWeaponRestriction.class)
			{
				if (joinCursed())
					return false;
			}
			
			activeChar.sendMessage("You are participating in a fun event!");
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		if (isInFunEvent(activeChar) && started())
			return false;
		
		return true;
	}
	
	@Override
	public final boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (started() && !allowInterference() && !activeChar.isGM())
		{
			if (isInFunEvent(target) != isInFunEvent(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public final boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage,
			L2PcInstance attacker_, L2PcInstance target_)
	{
		if (attacker_ == null || target_ == null || attacker_ == target_)
			return true;
		
		if (started() && !allowInterference() && !attacker_.isGM())
		{
			if (isInFunEvent(attacker_) != isInFunEvent(target_))
			{
				if (sendMessage)
					attacker_.sendMessage("You can't interact because of the fun event!");
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public final boolean canTeleport(L2PcInstance activeChar)
	{
		if (isInFunEvent(activeChar))
		{
			activeChar.sendMessage("You can't teleport during an event.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public final boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar,
			L2ItemInstance item, L2PcInstance player)
	{
		if (clazz == SummonItems.class)
		{
			if (player != null && isInFunEvent(player) && started() && !allowSummon())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		else if (clazz == Potions.class)
		{
			if (player != null && isInFunEvent(player) && started() && !allowPotions())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public final boolean canBeInsidePeaceZone(L2PcInstance activeChar, L2PcInstance target)
	{
		if (isInFunEvent(activeChar) && isInFunEvent(target) && started())
			return false;
		
		return true;
	}
	
	@Override
	public final Boolean isInsideZone(L2Character activeChar, byte zone)
	{
		if (activeChar instanceof L2Playable && isInFunEvent(activeChar.getActingPlayer()) && started())
		{
			switch (zone)
			{
				case L2Zone.FLAG_PEACE:
				{
					return Boolean.FALSE;
				}
				case L2Zone.FLAG_PVP:
				{
					return Boolean.TRUE;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean canStandUp(L2PcInstance activeChar)
	{
		if (sitForced() && isInFunEvent(activeChar))
		{
			activeChar.sendMessage("The Admin/GM handle if you sit or stand in this match!");
			return false;
		}
		
		return true;
	}
	
	@Override
	public void playerRevived(L2PcInstance player)
	{
		if (started() && reviveRecovery() && isInFunEvent(player))
		{
			player.getStatus().setCurrentHp(player.getMaxHp());
			player.getStatus().setCurrentMp(player.getMaxMp());
			player.getStatus().setCurrentCp(player.getMaxCp());
		}
	}
}

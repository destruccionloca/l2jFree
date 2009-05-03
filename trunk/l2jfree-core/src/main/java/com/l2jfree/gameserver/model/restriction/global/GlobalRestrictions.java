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

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * @author NB4L1
 */
public final class GlobalRestrictions
{
	private GlobalRestrictions()
	{
	}
	
	private static GlobalRestriction[] _activeRestrictions = new GlobalRestriction[0];
	
	public synchronized static void activate(GlobalRestriction restriction)
	{
		if (!ArrayUtils.contains(_activeRestrictions, restriction))
			_activeRestrictions = (GlobalRestriction[])ArrayUtils.add(_activeRestrictions, restriction);
	}
	
	public synchronized static void deactivate(GlobalRestriction restriction)
	{
		while (ArrayUtils.contains(_activeRestrictions, restriction))
			_activeRestrictions = (GlobalRestriction[])ArrayUtils.removeElement(_activeRestrictions, restriction);
	}
	
	static
	{
		/**
		 * Temporary solution until events reworked to fit the new scheme.
		 */
		activate(new CTFRestriction());
		activate(new DMRestriction());
		activate(new TvTRestriction());
		activate(new VIPRestriction());
		
		activate(new CursedWeaponRestriction());
		activate(new DuelRestriction());
		activate(new JailRestriction());
		activate(new OlympiadRestriction());
	}
	
	public static boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			if (!restriction.canInviteToParty(activeChar, target))
				return false;
		
		return true;
	}
	
	public static boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if (skill.isPassive())
			return false;
		
		if (activeChar == target)
			return true;
		
		final L2PcInstance attacker_ = L2Object.getActingPlayer(activeChar);
		
		if (attacker_ != null && attacker_ == target.getActingPlayer())
			return true;
		
		// doors and siege flags cannot receive any effects
		if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			return false;
		
		if (target.isInvul())
			return false;
		
		if (skill.isOffensive() || skill.isDebuff())
		{
			if (attacker_ != null && attacker_.isGM())
				if (attacker_.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					return false;
		}
		
		for (GlobalRestriction restriction : _activeRestrictions)
			if (!restriction.canCreateEffect(activeChar, target, skill))
				return false;
		
		return true;
	}
	
	public static boolean isInvul(L2Character activeChar, L2Character target, L2Skill skill)
	{
		boolean isOffensive = (skill == null || skill.isOffensive() || skill.isDebuff());
		
		L2PcInstance attacker_ = L2Object.getActingPlayer(activeChar);
		L2PcInstance target_ = L2Object.getActingPlayer(target);
		
		if (target.isInvul())
			return true;
		
		if (attacker_ == null || target_ == null || attacker_ == target_)
			return false;
		
		if (attacker_.isGM())
		{
			if (isOffensive && attacker_.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return true;
			else if (!target_.isGM())
				return false;
		}
		
		if (isOffensive)
		{
			if (attacker_.inObserverMode() || target_.inObserverMode())
			{
				attacker_.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				return true;
			}
			else if (attacker_.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
			{
				attacker_.sendMessage("Your level is too low to participate in a PvP combat.");
				return true;
			}
			else if (target_.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
			{
				attacker_.sendMessage("Target is under newbie protection.");
				return true;
			}
		}
		
		for (GlobalRestriction restriction : _activeRestrictions)
			if (restriction.isInvul(activeChar, target, isOffensive))
				return true;
		
		return false;
	}
	
	public static void levelChanged(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			restriction.levelChanged(activeChar);
	}
	
	public static void effectCreated(L2Effect effect)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			restriction.effectCreated(effect);
	}
	
	public static void playerLoggedIn(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			restriction.playerLoggedIn(activeChar);
	}
	
	public static void playerDisconnected(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			restriction.playerDisconnected(activeChar);
	}
	
	public static boolean  playerKilled(L2Character activeChar, L2PcInstance target)
	{
		for (GlobalRestriction restriction : _activeRestrictions)
			if (restriction.playerKilled(activeChar, target))
				return true;
		
		return false;
	}
}

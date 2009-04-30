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

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

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
		activate(new DefaultRestriction());
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
		for (GlobalRestriction restriction : _activeRestrictions)
			if (!restriction.canCreateEffect(activeChar, target, skill))
				return false;
		
		return true;
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
}

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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * @author NB4L1
 */
public final class GlobalRestrictions
{
	private GlobalRestrictions()
	{
	}
	
	private static enum RestrictionMode implements Comparator<GlobalRestriction>
	{
		isRestricted,
		canInviteToParty,
		canCreateEffect,
		isInvul,
		canRequestRevive,
		canTeleport,
		canUseItemHandler,
		// TODO
		
		isInsideZoneModifier,
		calcDamage,
		// TODO
		
		levelChanged,
		effectCreated,
		playerLoggedIn,
		playerDisconnected,
		playerKilled,
		isInsideZoneStateChanged,
		onBypassFeedback,
		onAction,
		// TODO
		;
		
		private final Method _method;
		
		private RestrictionMode()
		{
			for (Method method : GlobalRestriction.class.getMethods())
			{
				if (name().equals(method.getName()))
				{
					_method = method;
					return;
				}
			}
			
			throw new InternalError();
		}
		
		private boolean equalsMethod(Method method)
		{
			if (!_method.getName().equals(method.getName()))
				return false;
			
			if (!_method.getReturnType().equals(method.getReturnType()))
				return false;
			
			return Arrays.equals(_method.getParameterTypes(), method.getParameterTypes());
		}
		
		private static final RestrictionMode[] VALUES = RestrictionMode.values();
		
		private static RestrictionMode parse(Method method)
		{
			for (RestrictionMode mode : VALUES)
				if (mode.equalsMethod(method))
					return mode;
			
			return null;
		}
		
		@Override
		public int compare(GlobalRestriction o1, GlobalRestriction o2)
		{
			return Double.compare(getPriority(o2), getPriority(o1));
		}
		
		private double getPriority(GlobalRestriction restriction)
		{
			RestrictionPriority a1 = getMatchingMethod(restriction.getClass()).getAnnotation(RestrictionPriority.class);
			if (a1 != null)
				return a1.value();
			
			RestrictionPriority a2 = restriction.getClass().getAnnotation(RestrictionPriority.class);
			if (a2 != null)
				return a2.value();
			
			return RestrictionPriority.DEFAULT_PRIORITY;
		}
		
		private Method getMatchingMethod(Class<? extends GlobalRestriction> clazz)
		{
			for (Method method : clazz.getMethods())
				if (equalsMethod(method))
					return method;
			
			throw new InternalError();
		}
	}
	
	private static final GlobalRestriction[][] _restrictions = new GlobalRestriction[RestrictionMode.VALUES.length][0];
	
	public synchronized static void activate(GlobalRestriction restriction)
	{
		for (Method method : restriction.getClass().getMethods())
		{
			RestrictionMode mode = RestrictionMode.parse(method);
			
			if (mode == null)
				continue;
			
			if (method.getAnnotation(DisabledRestriction.class) != null)
				continue;
			
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			
			if (!ArrayUtils.contains(restrictions, restriction))
				restrictions = (GlobalRestriction[])ArrayUtils.add(restrictions, restriction);
			
			Arrays.sort(restrictions, mode);
			
			_restrictions[mode.ordinal()] = restrictions;
		}
	}
	
	public synchronized static void deactivate(GlobalRestriction restriction)
	{
		for (RestrictionMode mode : RestrictionMode.VALUES)
		{
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			
			for (int index; (index = ArrayUtils.indexOf(restrictions, restriction)) != -1;)
				restrictions = (GlobalRestriction[])ArrayUtils.remove(restrictions, index);
			
			_restrictions[mode.ordinal()] = restrictions;
		}
	}
	
	static
	{
		/**
		 * Temporary solution until events reworked to fit the new scheme.
		 */
		activate(new AutomatedTvTRestriction()); // TODO: must be checked
		activate(new CTFRestriction());
		activate(new DMRestriction());
		activate(new L2EventRestriction());
		activate(new SHRestriction()); // TODO: must be checked
		//activate(new TvTiRestriction()); // TODO: must be checked to be able to activate it
		activate(new TvTRestriction());
		activate(new VIPRestriction());
		
		activate(new CursedWeaponRestriction());
		activate(new DuelRestriction());
		activate(new JailRestriction());
		activate(new OlympiadRestriction());
	}
	
	public static boolean isRestricted(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isRestricted.ordinal()])
			if (restriction.isRestricted(activeChar))
				return true;
		
		return false;
	}
	
	public static boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canInviteToParty.ordinal()])
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
		
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canCreateEffect.ordinal()])
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
		
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isInvul.ordinal()])
			if (restriction.isInvul(activeChar, target, isOffensive))
				return true;
		
		return false;
	}
	
	public static boolean canRequestRevive(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canRequestRevive.ordinal()])
			if (!restriction.canRequestRevive(activeChar))
				return false;
		
		return true;
	}
	
	public static boolean canTeleport(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canTeleport.ordinal()])
			if (!restriction.canTeleport(activeChar))
				return false;
		
		return true;
	}
	
	public static boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar,
		L2ItemInstance item)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseItemHandler.ordinal()])
			if (!restriction.canUseItemHandler(clazz, itemId, activeChar, item))
				return false;
		
		return true;
	}
	
	// TODO
	
	public static int isInsideZoneModifier(L2Character activeChar, byte zone)
	{
		int result = 0;
		
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isInsideZoneModifier.ordinal()])
			result += restriction.isInsideZoneModifier(activeChar, zone);
		
		return result;
	}
	
	public static double calcDamage(L2Character activeChar, L2Character target, double damage, L2Skill skill)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.calcDamage.ordinal()])
			damage = restriction.calcDamage(activeChar, target, damage, skill);
		
		return damage;
	}
	
	// TODO
	
	public static void levelChanged(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.levelChanged.ordinal()])
			restriction.levelChanged(activeChar);
	}
	
	public static void effectCreated(L2Effect effect)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.effectCreated.ordinal()])
			restriction.effectCreated(effect);
	}
	
	public static void playerLoggedIn(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerLoggedIn.ordinal()])
			restriction.playerLoggedIn(activeChar);
	}
	
	public static void playerDisconnected(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerDisconnected.ordinal()])
			restriction.playerDisconnected(activeChar);
	}
	
	public static boolean playerKilled(L2Character activeChar, L2PcInstance target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerKilled.ordinal()])
			if (restriction.playerKilled(activeChar, target))
				return true;
		
		return false;
	}
	
	public static void isInsideZoneStateChanged(L2Character activeChar, byte zone, boolean isInsideZone)
	{
		switch (zone)
		{
			case L2Zone.FLAG_PVP:
			{
				if (isInsideZone)
					activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				else
					activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isInsideZoneStateChanged.ordinal()])
			restriction.isInsideZoneStateChanged(activeChar, zone, isInsideZone);
	}
	
	public static boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onBypassFeedback.ordinal()])
			if (restriction.onBypassFeedback(npc, activeChar, command))
				return true;
		
		return false;
	}
	
	public static boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onAction.ordinal()])
			if (restriction.onAction(npc, activeChar))
				return true;
		
		return false;
	}
	
	// TODO
}

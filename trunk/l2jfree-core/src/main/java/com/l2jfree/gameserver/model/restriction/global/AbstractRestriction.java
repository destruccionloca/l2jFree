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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author NB4L1
 */
public abstract class AbstractRestriction implements GlobalRestriction
{
	static final Log _log = LogFactory.getLog(AbstractRestriction.class);
	
	public void activate()
	{
		GlobalRestrictions.activate(this);
	}
	
	public void deactivate()
	{
		GlobalRestrictions.deactivate(this);
	}
	
	/**
	 * To avoid accidentally multiple times activated restrictions.
	 */
	@Override
	public boolean equals(Object obj)
	{
		return getClass().equals(obj.getClass());
	}
	
	@DisabledRestriction
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean isInvul(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
		L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
		L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage,
		L2PcInstance attacker_, L2PcInstance target_)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canTeleport(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar,
		L2ItemInstance item)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canBeInsidePeaceZone(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean canStandUp(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
	
	@DisabledRestriction
	public int isInsideZoneModifier(L2Character activeChar, byte zone)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public double calcDamage(L2Character activeChar, L2Character target, double damage, L2Skill skill)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
	
	@DisabledRestriction
	public void levelChanged(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public void effectCreated(L2Effect effect)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public void playerDisconnected(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean playerKilled(L2Character activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public void isInsideZoneStateChanged(L2Character activeChar, byte zone, boolean isInsideZone)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		throw new AbstractMethodError();
	}
	
	@DisabledRestriction
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
}

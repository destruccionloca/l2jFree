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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions.CombatState;

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
	
	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}
	
	/**
	 * To avoid accidentally multiple times activated restrictions.
	 */
	@Override
	public boolean equals(Object obj)
	{
		return getClass().equals(obj.getClass());
	}
	
	@Override
	@DisabledRestriction
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean isInvul(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
			L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
			L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_,
			L2PcInstance target_)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canTeleport(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar,
			L2ItemInstance item, L2PcInstance player)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canDropItem(L2PcInstance player, int itemId, L2ItemInstance item)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canDestroyItem(L2PcInstance player, int itemId, L2ItemInstance item)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public CombatState getCombatState(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canStandUp(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canPickUp(L2PcInstance activeChar, L2ItemInstance item, L2PetInstance pet)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public int getNameColor(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public int getTitleColor(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
	
	@Override
	@DisabledRestriction
	public Boolean isInsideZone(L2Character activeChar, byte zone)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public double calcDamage(L2Character activeChar, L2Character target, double damage, L2Skill skill)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public List<L2Character> getTargetList(SkillTargetType type, L2Character activeChar, L2Skill skill,
			L2Character target)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
	
	@Override
	@DisabledRestriction
	public void levelChanged(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void effectCreated(L2Effect effect)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerDisconnected(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerRevived(L2PcInstance player)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void isInsideZoneStateChanged(L2Character activeChar, byte zone, boolean isInsideZone)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void instanceChanged(L2PcInstance activeChar, int oldInstance, int newInstance)
	{
		throw new AbstractMethodError();
	}
	
	// TODO
}

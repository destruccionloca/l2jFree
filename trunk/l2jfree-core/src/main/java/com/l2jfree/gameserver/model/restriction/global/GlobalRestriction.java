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

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions.CombatState;

/**
 * @author NB4L1
 */
public interface GlobalRestriction
{
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction);
	
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target);
	
	public boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill);
	
	public boolean isInvul(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
			L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive);
	
	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
			L2PcInstance attacker_, L2PcInstance target_, boolean isOffensive);
	
	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_,
			L2PcInstance target_);
	
	public boolean canRequestRevive(L2PcInstance activeChar);
	
	public boolean canTeleport(L2PcInstance activeChar);
	
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar,
			L2ItemInstance item, L2PcInstance player);
	
	public CombatState getCombatState(L2PcInstance activeChar, L2PcInstance target);
	
	public boolean canStandUp(L2PcInstance activeChar);
	
	// TODO
	
	public Boolean isInsideZone(L2Character activeChar, byte zone);
	
	public double calcDamage(L2Character activeChar, L2Character target, double damage, L2Skill skill);
	
	public List<L2Character> getTargetList(SkillTargetType type, L2Character activeChar, L2Skill skill,
			L2Character target);
	
	// TODO
	
	public void levelChanged(L2PcInstance activeChar);
	
	public void effectCreated(L2Effect effect);
	
	public void playerLoggedIn(L2PcInstance activeChar);
	
	public void playerDisconnected(L2PcInstance activeChar);
	
	public boolean playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer);
	
	public void playerRevived(L2PcInstance player);
	
	public void isInsideZoneStateChanged(L2Character activeChar, byte zone, boolean isInsideZone);
	
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command);
	
	public boolean onAction(L2Npc npc, L2PcInstance activeChar);
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target);
	
	// TODO
}

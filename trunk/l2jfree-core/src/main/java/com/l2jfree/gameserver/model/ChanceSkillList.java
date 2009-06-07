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
package com.l2jfree.gameserver.model;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.skills.ChanceCondition;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.IChanceSkillTrigger;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.util.L2Arrays;

/**
 * @author kombat/crion
 */
public final class ChanceSkillList
{
	private IChanceSkillTrigger[] _triggers = IChanceSkillTrigger.EMPTY_ARRAY;
	
	private final L2Character _owner;
	
	public ChanceSkillList(L2Character owner)
	{
		_owner = owner;
	}
	
	public synchronized void add(IChanceSkillTrigger trigger)
	{
		int nullIndex = -1;
		for (int i = 0; i < _triggers.length; i++)
		{
			final IChanceSkillTrigger tmp = _triggers[i];
			
			if (tmp == null)
				nullIndex = i;
			
			if (trigger.equals(tmp))
				return;
		}
		
		if (nullIndex < 0)
			_triggers = (IChanceSkillTrigger[])ArrayUtils.add(_triggers, trigger);
		
		else
			_triggers[nullIndex] = trigger;
	}
	
	public synchronized void remove(IChanceSkillTrigger trigger)
	{
		int nullCount = 0;
		for (int i = 0; i < _triggers.length; i++)
		{
			final IChanceSkillTrigger tmp = _triggers[i];
			
			if (trigger.equals(tmp))
			{
				_triggers[i] = null;
				
				nullCount++;
			}
			else if (tmp == null)
				nullCount++;
		}
		
		if (nullCount > 5)
			_triggers = L2Arrays.compact(_triggers);
	}
	
	public void onHit(L2Character evtInitiator, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_CRIT;
		}
		
		onEvent(event, evtInitiator);
	}
	
	public void onEvadedHit(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, attacker);
	}
	
	public void onSkillHit(L2Character evtInitiator, boolean ownerWasHit, L2Skill trigger)
	{
		int event;
		if (ownerWasHit)
		{
			if (!ChanceSkillList.canTriggerByCast(evtInitiator, _owner, trigger))
				return;
			
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (trigger.isOffensive())
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			if (!ChanceSkillList.canTriggerByCast(_owner, evtInitiator, trigger))
				return;
			
			event = ChanceCondition.EVT_CAST;
			event |= trigger.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= trigger.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		onEvent(event, evtInitiator);
	}
	
	public static boolean canTriggerByCast(L2Character caster, L2Character target, L2Skill trigger)
	{
		if (trigger.isToggle() || trigger.isPotion())
			return false; // No buffing with toggle skills or potions
			
		if (1320 <= trigger.getId() && trigger.getId() <= 1322)
			return false; // No buffing with Common and Dwarven Craft
			
		if (trigger.isOffensive() && !Formulas.calcMagicSuccess(caster, target, trigger))
			return false; // Low grade skills won't trigger for high level targets
			
		return true;
	}
	
	public void onEvent(int event, L2Character evtInitiator)
	{
		for (IChanceSkillTrigger trigger : _triggers)
		{
			if (trigger != null && trigger.getChanceCondition().trigger(event))
			{
				makeCast(trigger.getChanceTriggeredSkill(_owner), evtInitiator);
			}
		}
	}
	
	private void makeCast(L2Skill skill, L2Character evtInitiator)
	{
		if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
			return;
		
		L2Character[] targets = skill.getTargetList(_owner, false, evtInitiator);
		
		if (targets == null || targets.length == 0)
			return;
		
		//anyone has a better idea?
		boolean hasValidTarget = false;
		for (int i = 0; i < targets.length; i++)
		{
			final L2Character target = targets[i];
			
			if (target == null)
				continue;
			
			final L2Effect effect = target.getFirstEffect(skill.getId());
			
			// if we already have a greater or equal effect of it
			if (effect != null && effect.getSkill().getLevel() >= skill.getLevel())
			{
				targets[i] = null;
				continue;
			}
			else if (!skill.checkCondition(_owner, target))
			{
				targets[i] = null;
				continue;
			}
			
			hasValidTarget = true;
		}
		
		if (!hasValidTarget)
			return;
		
		targets = L2Arrays.compact(targets);
		
		_owner.broadcastPacket(new MagicSkillUse(_owner, skill, 0, 0));
		_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill, targets));
		
		// Launch the magic skill and calculate its effects
		// TODO: once core will support all posible effects, use effects (not handler)
		SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(_owner, skill, targets);
	}
}

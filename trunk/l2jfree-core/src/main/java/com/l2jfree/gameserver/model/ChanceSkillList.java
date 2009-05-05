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

import javolution.util.FastMap;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;

/**
 *
 * @author  kombat/crion
 */
public class ChanceSkillList extends FastMap<L2Skill, ChanceCondition>
{
	private static final long serialVersionUID = -2542073222059976854L;
	
	private L2Character _owner;

	public ChanceSkillList(L2Character owner)
	{
		super();
		setShared(true);
		_owner = owner;
	}

	public L2Character getOwner()
	{
		return _owner;
	}

	public void setOwner(L2Character owner)
	{
		_owner = owner;
	}

	public void onHit(L2Character target, boolean ownerWasHit, boolean wasCrit)
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

		onEvent(event, target);
	}

	public void onSkillHit(L2Character target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (wasOffensive)
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
			event = ChanceCondition.EVT_CAST;
			event |= wasMagic ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= wasOffensive ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}

		onEvent(event, target);
	}

	public void onEvadedHit(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, attacker);
	}

	public void onEvent(int event, L2Character target)
	{
		for (FastMap.Entry<L2Skill, ChanceCondition> e = head(), end = tail(); (e = e.getNext()) != end;)
		{
			if (e.getValue() != null && e.getValue().trigger(event))
			{
				L2Skill s = e.getKey();
				if (e.getValue().improve())
				{
					//improve an active effect
					L2Effect ef = target.getFirstEffect(s.getId());
					if (ef != null) //effect exists, try level+1
						s = SkillTable.getInstance().getInfo(ef.getId(), ef.getLevel()+1);
					else //effect doesn't exist?
						s = null;
					if (s != null) //an improved effect exists
						makeCast(s, target);
				}
			}
		}
	}

	private void makeCast(L2Skill skill, L2Character target)
	{
		try
		{
			if (skill.getWeaponDependancy(_owner, false))
			{
				if (skill.triggerAnotherSkill()) //should we use this skill or this skill is just referring to another one ...
				{
					skill = SkillTable.getInstance().getInfo(skill.getTriggeredId(), skill.getTriggeredLevel());
					if(skill == null)
						return;
				}
				
				L2Character[] targets = skill.getTargetList(_owner, false, target);
				if (targets != null && targets.length > 0)
				{
					_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill, targets));
					_owner.broadcastPacket(new MagicSkillUse(_owner, targets[0], skill.getDisplayId(), skill.getLevel(), 0, 0));

					// Launch the magic skill and calculate its effects
					SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(_owner, skill, targets);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
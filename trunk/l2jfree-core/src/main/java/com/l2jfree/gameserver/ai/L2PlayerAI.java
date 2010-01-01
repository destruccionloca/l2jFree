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
package com.l2jfree.gameserver.ai;

import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Character.AIAccessor;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2StaticObjectInstance;

public class L2PlayerAI extends L2CharacterAI
{
	private volatile boolean _thinking; // to prevent recursive thinking
	
	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	public L2PcInstance getActor()
	{
		return (L2PcInstance)_actor;
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		
		super.clientNotifyDead();
	}
	
	private void thinkAttack()
	{
		final L2Character target = getAttackTarget();
		
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (checkTargetLostOrDead(target))
		{
			clientActionFailed();
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			clientActionFailed();
			return;
		}
		
		_accessor.doAttack(target);
	}
	
	private void thinkCast()
	{
		final L2Skill skill = getCastSkill();
		final L2Character target = getCastTarget();
		
		if (skill.getTargetType() == SkillTargetType.TARGET_GROUND)
		{
			if (maybeMoveToPosition(getActor().getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(skill)))
			{
				_actor.setIsCastingNow(false, true);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (maybeMoveToPawn(target, _actor.getMagicalAttackRange(skill)))
			{
				clientActionFailed();
				_actor.setIsCastingNow(false, true);
				return;
			}
		}
		
		if (skill.getHitTime() > 50)
			clientStopMoving(null);
		
		final L2Object oldTarget = _actor.getTarget();
		
		if (oldTarget != null && target != null && oldTarget != target)
		{
			// Replace the current target by the cast target
			_actor.setTarget(target);
			
			// Launch the Cast of the skill
			_accessor.doCast(skill);
			
			// Restore the initial target
			_actor.setTarget(oldTarget);
		}
		else
			_accessor.doCast(skill);
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		
		final L2Object target = getTarget();
		
		if (checkTargetLost(target))
		{
			clientActionFailed();
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			clientActionFailed();
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		
		((L2PcInstance.AIAccessor)_accessor).doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		
		final L2Object target = getTarget();
		
		if (checkTargetLost(target))
		{
			clientActionFailed();
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			clientActionFailed();
			return;
		}
		
		if (!(target instanceof L2StaticObjectInstance))
			((L2PcInstance.AIAccessor)_accessor).doInteract((L2Character)target);
		
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking && getIntention() != AI_INTENTION_CAST) // casting must always continue
		{
			clientActionFailed();
			return;
		}
		
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		getActor().getKnownList().updateKnownObjects();
		
		super.onEvtArrivedRevalidate();
	}
}

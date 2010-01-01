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

import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Character.AIAccessor;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfree.gameserver.skills.SkillUsageRequest;

public class L2PlayerAI extends L2CharacterAI
{
	private volatile boolean _thinking; // to prevent recursive thinking

	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
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
		L2Character target = getAttackTarget();
		if (target == null)
		{
			clientActionFailed();
			return;
		}

		if (checkTargetLostOrDead(target))
		{
			// Notify the target
			setAttackTarget(null);
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
		L2Character target = getCastTarget();
		if (_log.isDebugEnabled())
			_log.warn("L2PlayerAI: thinkCast -> Start");

		if (_skill.getTargetType() == SkillTargetType.TARGET_GROUND && _actor instanceof L2PcInstance)
		{
			if (maybeMoveToPosition(((L2PcInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false, true);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if (_skill.isOffensive() && getAttackTarget() != null)
				{
					//Notify the target
					setCastTarget(null);
				}
				_actor.setIsCastingNow(false);
				return;
			}
			if (target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
			{
				clientActionFailed();
				_actor.setIsCastingNow(false, true);
				return;
			}
		}

		if (_skill.getHitTime() > 50)
			clientStopMoving(null);

		L2Object oldTarget = _actor.getTarget();
		if (oldTarget != null && target != null && oldTarget != target)
		{
			// Replace the current target by the cast target
			_actor.setTarget(getCastTarget());

			// Launch the Cast of the skill
			_accessor.doCast(_skill);

			// Restore the initial target
			_actor.setTarget(oldTarget);
		}
		else
			_accessor.doCast(_skill);
	}

	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		L2Object target = getTarget();
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
		((L2PcInstance.AIAccessor) _accessor).doPickupItem(target);
	}

	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		L2Object target = getTarget();
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
			((L2PcInstance.AIAccessor) _accessor).doInteract((L2Character) target);
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

		/*
		 if (_log.isDebugEnabled())
		 _log.warn("L2PlayerAI: onEvtThink -> Check intention");
		 */

		_thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
			else if (getIntention() == AI_INTENTION_CAST)
				thinkCast();
			else if (getIntention() == AI_INTENTION_PICK_UP)
				thinkPickUp();
			else if (getIntention() == AI_INTENTION_INTERACT)
				thinkInteract();
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

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

import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.ArrayList;

import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2ItemInstance.ItemLocation;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2AirShipInstance;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.util.Util.Direction;
import com.l2jfree.tools.geometry.Point3D;
import com.l2jfree.util.L2Collections;

/**
 * This class manages AI of L2Character.<BR><BR>
 *
 * L2CharacterAI :<BR><BR>
 * <li>L2AttackableAI</li>
 * <li>L2DoorAI</li>
 * <li>L2PlayerAI</li>
 * <li>L2SummonAI</li><BR><BR>
 *
 */
public class L2CharacterAI extends AbstractAI
{
	class IntentionCommand
	{
		protected CtrlIntention	_crtlIntention;
		protected Object		_arg0, _arg1;

		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}
	}

	public IntentionCommand getNextIntention()
	{
		return null;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (!(attacker instanceof L2Attackable) || !attacker.isCoreAIDisabled())
			clientStartAutoAttack();
	}

	/**
	 * Constructor of L2CharacterAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 *
	 */
	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}

	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast) </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);

		// Init cast and attack target
		setCastTarget(null);
		setAttackTarget(null);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
	}

	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.<BR><BR>
	 *
	 * <B><U> Actions</U> : <I>if the Intention is not already Active</I></B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_ACTIVE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 *
	 */

	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);

			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();

			// Also enable random animations for this L2Character if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (_actor instanceof L2Attackable)
				((L2Npc)_actor).broadcastRandomAnimation(false);
			
			// Launch the Think Event
			onEvtThink();
		}
	}

	/**
	 * Manage the Rest Intention.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li><BR><BR>
	 */
	@Override
	protected void onIntentionRest()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_ATTACK </li>
	 * <li>Set or change the AI attack target </li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 *
	 *
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2AttackableAI : Calculate attack timeout</li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// stop invul effect if exist
		if (_actor.getInvulEffect() != null)
			_actor.getInvulEffect().exit();

		if (target == null)
		{
			clientActionFailed();
			return;
		}

		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Check if the Intention is already AI_INTENTION_ATTACK
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			// Check if the AI already targets the L2Character
			if (getAttackTarget() != target)
			{
				// Set the AI attack target (change target)
				setAttackTarget(target);

				stopFollow();

				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);

			}
			else
				clientActionFailed(); // else client freezes until cancel target

		}
		else
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);

			// Set the AI attack target
			setAttackTarget(target);

			stopFollow();

			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}

	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI cast target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor </li>
	 * <li>Set the AI skill used by INTENTION_CAST </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_CAST </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		// stop invul effect if exist
		if (_actor.getInvulEffect() != null)
			_actor.getInvulEffect().exit();

		if (getIntention() == AI_INTENTION_REST && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}

		// Set the AI cast target
		setCastTarget((L2Character) target);

		// Stop actions client-side to cast the skill
		if (skill.getHitTime() > 50)
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			_actor.abortAttack();

			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			// no need for second ActionFailed packet, abortAttack() already sent it
			//clientActionFailed();
		}

		// Set the AI skill used by INTENTION_CAST
		_skill = skill;

		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);

		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO </li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast) </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionMoveTo(L2CharPosition pos)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveTo(pos.x, pos.y, pos.z);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.ai.AbstractAI#onIntentionMoveToInABoat(com.l2jfree.gameserver.model.L2CharPosition, com.l2jfree.gameserver.model.L2CharPosition)
	 */
	@Override
	protected void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled()|| _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		//
		//changeIntention(AI_INTENTION_MOVE_TO, new L2CharPosition(((L2PcInstance)_actor).getBoat().getX() - destination.x, ((L2PcInstance)_actor).getBoat().getY() - destination.y, ((L2PcInstance)_actor).getBoat().getZ() - destination.z, 0)  , null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveToInABoat(destination, origin);
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.ai.AbstractAI#onIntentionMoveToInAirShip(com.l2jfree.gameserver.model.L2CharPosition, com.l2jfree.gameserver.model.L2CharPosition)
	 */
	@Override
	protected void onIntentionMoveToInAirShip(L2CharPosition destination, L2CharPosition origin)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		//
		//changeIntention(AI_INTENTION_MOVE_TO, new L2CharPosition(((L2PcInstance)_actor).getBoat().getX() - destination.x, ((L2PcInstance)_actor).getBoat().getY() - destination.y, ((L2PcInstance)_actor).getBoat().getZ() - destination.z, 0)  , null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveToInAirShip(destination, origin);
	}

	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_FOLLOW </li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isImmobilized() || _actor.isRooted())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}

		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);

		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}

	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI pick up target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_PICK_UP </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		if (object instanceof L2ItemInstance && ((L2ItemInstance)object).getLocation() != ItemLocation.VOID)
			return;

			// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);

		// Set the AI pick up target
		setTarget(object);

		if (object.getX() == 0 && object.getY() == 0)
		{
			_log.warn("Object in coords 0,0 - using a temporary fix");
			object.getPosition().setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}

		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}

	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the AI interact target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_INTERACT </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		if (getIntention() != AI_INTENTION_INTERACT)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);

			// Set the AI interact target
			setTarget(object);

			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character </li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character </li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode) </li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtParalyzed(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Sleeping.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character </li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character </li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);

		// stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}

	/**
	 * Launch actions corresponding to the Event Rooted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		//_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		//if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		//    AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Confused.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Muted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character </li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Arrived.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtArrived()
	{
		_accessor.getActor().revalidateZone(true);

		if (_accessor.getActor().moveToNextRoutePoint())
		{
			clientActionFailed();
			return;
		}

		if (_accessor.getActor() instanceof L2Attackable)
		{
			((L2Attackable) _accessor.getActor()).setisReturningToSpawnPoint(false);
		}
		clientStoppedMoving();

		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO)
			setIntention(AI_INTENTION_ACTIVE);

		// Launch actions corresponding to the Event Think
		onEvtThink();

		if (_actor instanceof L2BoatInstance)
		{
			((L2BoatInstance) _actor).evtArrived();
		}
		else if (_actor instanceof L2AirShipInstance)
		{
			((L2AirShipInstance) _actor).evtArrived();
		}
	}

	/**
	 * Launch actions corresponding to the Event ArrivedRevalidate.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO || getIntention() == AI_INTENTION_CAST)
			setIntention(AI_INTENTION_ACTIVE);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blocked_at_pos);

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ForgetObject.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to attack, stop the auto-attack, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to cast, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to follow, stop the movement, cancel AI Follow Task and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the targeted object was the actor , cancel AI target, stop AI Follow Task, stop the movement and set the Intention to AI_INTENTION_IDLE </li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		// If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE
		if (getTarget() == object)
		{
			setTarget(null);

			if (getIntention() == AI_INTENTION_INTERACT)
				setIntention(AI_INTENTION_ACTIVE);
			else if (getIntention() == AI_INTENTION_PICK_UP)
				setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to attack
		if (getAttackTarget() == object)
		{
			// Cancel attack target
			setAttackTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to cast
		if (getCastTarget() == object)
		{
			// Cancel cast target
			setCastTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to follow
		if (getFollowTarget() == object)
		{
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop an AI Follow Task
			stopFollow();

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the targeted object was the actor
		if (_actor == object)
		{
			// Cancel AI target
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);

			// Stop an AI Follow Task
			stopFollow();

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_IDLE
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}

	/**
	 * Launch actions corresponding to the Event Cancel.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();

		// Stop an AI Follow Task
		stopFollow();

		if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event Dead.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li><BR><BR>
	 *
	 */
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Follow Task
		stopFollow();

		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();

		if (!(_actor instanceof L2PcInstance))
			_actor.setWalking();
	}

	/**
	 * Launch actions corresponding to the Event Fake Death.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 *
	 */
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();

		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Init AI
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}

	protected boolean maybeMoveToPosition(Point3D worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.warn("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}

		if (offset < 0)
			return false; // skill radius -1

		if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().getCollisionRadius(), false))
		{
			if (_actor.isMovementDisabled())
				return true;

			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
				_actor.setRunning();

			stopFollow();

			int x = _actor.getX();
			int y = _actor.getY();

			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;

			double dist = Math.sqrt(dx * dx + dy * dy);

			double sin = dy / dist;
			double cos = dx / dist;

			dist -= offset - 5;

			x += (int) (dist * cos);
			y += (int) (dist * sin);

			moveTo(x, y, worldPosition.getZ());
			return true;
		}

		if (getFollowTarget() != null)
			stopFollow();

		return false;
	}

	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the distance between the current position of the L2Character and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 * @param offset The Interact area radius
	 *
	 * @return True if a movement must be done
	 *
	 */
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		final int originalOffset = offset;
		
		// Get the distance between the current position of the L2Character and the target (x,y)
		if (target == null)
		{
			_log.warn("maybeMoveToPawn: target == NULL!");
			return false;
		}
		if (offset < 0)
			return false; // skill radius -1

		offset += _actor.getTemplate().getCollisionRadius();
		if (target instanceof L2Character)
			offset += ((L2Character) target).getTemplate().getCollisionRadius();

		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			// Caller should be L2Playable and thinkAttack/thinkCast/thinkInteract/thinkPickUp
			if (getFollowTarget() != null)
			{
				if (!target.isMoving())
					return true;
				// allow larger hit range only if the target runs towards the character
				if (Direction.getDirection(_actor, target) != Direction.FRONT)
					return true;
				// allow larger hit range when the target is moving (check is run only once per second)
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
					return true;
				stopFollow();
				return false;
			}

			if (_actor.isMovementDisabled())
				return true;

			// while flying there is no move to cast
			if (_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST &&
					_actor instanceof L2PcInstance && ((L2PcInstance)_actor).isTransformed())
			{
				if (!((L2PcInstance)_actor).getTransformation().canStartFollowToCast())
				{
					((L2PcInstance)_actor).sendPacket(new SystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					((L2PcInstance)_actor).sendPacket(ActionFailed.STATIC_PACKET);
					
					return true;
				}
			}
			
			// If not running, set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
				_actor.setRunning();

			stopFollow();
			if ((target instanceof L2Character) && !(target instanceof L2DoorInstance))
			{
				startFollow((L2Character)target, originalOffset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}

			return true;
		}

		if (getFollowTarget() != null)
			stopFollow();

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		// clientStopMoving(null);
		return false;
	}

	public void stopAITask()
	{
	}

	/**
	 * Modify current Intention and actions if the target is lost or dead.<BR><BR>
	 *
	 * <B><U> Actions</U> : <I>If the target is lost or dead</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 *
	 * @return True if the target is lost or dead (false if fakedeath)
	 *
	 */
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if (target == null || target.isAlikeDead())
		{
			//check if player is fakedeath
			if (target != null && target.isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	/**
	 * Modify current Intention and actions if the target is lost.<BR><BR>
	 *
	 * <B><U> Actions</U> : <I>If the target is lost</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 *
	 * @return True if the target is lost
	 *
	 */
	protected boolean checkTargetLost(L2Object target)
	{
		// check if player is fakedeath
		if (target instanceof L2PcInstance)
		{
			L2PcInstance target2 = (L2PcInstance) target; //convert object to chara

			if (target2.isFakeDeath())
			{
				target2.stopFakeDeath(true);
				return false;
			}
		}
		if (target == null)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	protected static final class SelfAnalysis
	{
		protected boolean		isMage						= false;
		protected boolean		isBalanced					= false;
		protected boolean		isArcher					= false;
		protected boolean		isHealer					= false;
		protected boolean		isFighter					= false;
		protected boolean		cannotMoveOnLand			= false;
		protected L2Skill[]		generalSkills				= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		buffSkills					= L2Skill.EMPTY_ARRAY;
		protected int			lastBuffTick				= 0;
		protected L2Skill[]		debuffSkills				= L2Skill.EMPTY_ARRAY;
		protected int			lastDebuffTick				= 0;
		protected L2Skill[]		cancelSkills				= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		healSkills					= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		trickSkills					= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		generalDisablers			= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		sleepSkills					= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		rootSkills					= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		muteSkills					= L2Skill.EMPTY_ARRAY;
		protected L2Skill[]		resurrectSkills				= L2Skill.EMPTY_ARRAY;
		protected boolean		hasHealOrResurrect			= false;
		protected boolean		hasLongRangeSkills			= false;
		protected boolean		hasLongRangeDamageSkills	= false;
		protected int			maxCastRange				= 0;
		
		protected SelfAnalysis()
		{
		}
		
		protected void init(L2Character actor)
		{
			switch (((L2NpcTemplate)actor.getTemplate()).getAI())
			{
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			// water movement analysis
			if (actor instanceof L2Npc)
			{
				switch (((L2Npc)actor).getNpcId())
				{
					case 20314: // Great White Shark
					case 20849: // Light Worm
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			
			final ArrayList<L2Skill> generalSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> buffSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> debuffSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> cancelSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> healSkills0 = L2Collections.newArrayList();
			//final ArrayList<L2Skill> trickSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> generalDisablers0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> sleepSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> rootSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> muteSkills0 = L2Collections.newArrayList();
			final ArrayList<L2Skill> resurrectSkills0 = L2Collections.newArrayList();
			
			// skill analysis
			for (L2Skill sk : actor.getAllSkills())
			{
				if (sk.isPassive())
					continue;
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch (sk.getSkillType())
				{
					case HEAL:
					case HEAL_PERCENT:
					case HEAL_STATIC:
					case BALANCE_LIFE:
					case HOT:
						healSkills0.add(sk);
						hasHealOrResurrect = true;
						continue; // won't be considered something for fighting
					case BUFF:
						buffSkills0.add(sk);
						continue; // won't be considered something for fighting
					case PARALYZE:
					case STUN:
						// hardcoding petrification until improvements are made to
						// EffectTemplate... petrification is totally different for
						// AI than paralyze
						switch (sk.getId())
						{
							case 367:
							case 4111:
							case 4383:
							case 4616:
							case 4578:
								sleepSkills0.add(sk);
								break;
							default:
								generalDisablers0.add(sk);
								break;
						}
						break;
					case MUTE:
						muteSkills0.add(sk);
						break;
					case SLEEP:
						sleepSkills0.add(sk);
						break;
					case ROOT:
						rootSkills0.add(sk);
						break;
					case FEAR: // could be used as an alternative for healing?
					case CONFUSION:
						//  trickSkills.add(sk);
					case DEBUFF:
						debuffSkills0.add(sk);
						break;
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case NEGATE:
						cancelSkills0.add(sk);
						break;
					case RESURRECT:
						resurrectSkills0.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue; // won't be considered something for fighting
					default:
						generalSkills0.add(sk);
						hasLongRangeDamageSkill = true;
						break;
				}
				if (castRange > 70)
				{
					hasLongRangeSkills = true;
					if (hasLongRangeDamageSkill)
						hasLongRangeDamageSkills = true;
				}
				if (castRange > maxCastRange)
					maxCastRange = castRange;
				
			}
			// Because of missing skills, some mages/balanced cannot play like mages
			if (!hasLongRangeDamageSkills && isMage)
			{
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			if (!hasLongRangeSkills && (isMage || isBalanced))
			{
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			if (generalSkills0.isEmpty() && isMage)
			{
				isBalanced = true;
				isMage = false;
			}
			
			generalSkills = convert(generalSkills0);
			buffSkills = convert(buffSkills0);
			debuffSkills = convert(debuffSkills0);
			cancelSkills = convert(cancelSkills0);
			healSkills = convert(healSkills0);
			//this.trickSkills = convert(trickSkills0);
			generalDisablers = convert(generalDisablers0);
			sleepSkills = convert(sleepSkills0);
			rootSkills = convert(rootSkills0);
			muteSkills = convert(muteSkills0);
			resurrectSkills = convert(resurrectSkills0);
		}
		
		private L2Skill[] convert(ArrayList<L2Skill> list)
		{
			try
			{
				return list.toArray(new L2Skill[list.size()]);
			}
			finally
			{
				L2Collections.recycle(list);
			}
		}
	}
	
	protected static final class TargetAnalysis
	{
		protected L2Character	character;
		protected boolean		isMage;
		protected boolean		isBalanced;
		protected boolean		isArcher;
		protected boolean		isFighter;
		protected boolean		isCanceled;
		protected boolean		isSlower;
		protected boolean		isMagicResistant;
		
		protected TargetAnalysis()
		{
		}
		
		protected void update(L2Character actor, L2Character target)
		{
			// update status once in 4 tries
			if (target == character && System.nanoTime() % 4 == 0)
				return;
			
			character = target;
			if (target == null)
				return;
			
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			
			double multi = (double)target.getMAtk(null, null) / target.getPAtk(null);
			
			if (multi > 1.5)
			{
				isMage = true;
			}
			else if (multi > 0.8)
			{
				isBalanced = true;
			}
			else
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if (weapon != null && weapon.getItemType().isBowType())
					isArcher = true;
				else
					isFighter = true;
			}
			
			isCanceled = target.getBuffCount() < 4;
			isSlower = target.getRunSpeed() < actor.getRunSpeed() - 3;
			isMagicResistant = target.getMDef(null, null) * 1.2 > actor.getMAtk(null, null);
		}
	}
}

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
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Collection;
import java.util.Set;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Boss;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jfree.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.tools.random.Rnd;

/**
 * This class manages AI of L2Attackable.<BR><BR>
 * 
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	private static final class AttackableAiTaskManager extends AbstractIterativePeriodicTaskManager<L2AttackableAI>
	{
		private static final AttackableAiTaskManager _instance = new AttackableAiTaskManager();
		
		private static AttackableAiTaskManager getInstance()
		{
			return _instance;
		}
		
		private AttackableAiTaskManager()
		{
			super(1000);
		}
		
		@Override
		protected void callTask(L2AttackableAI task)
		{
			task.run();
		}
		
		@Override
		protected String getCalledMethodName()
		{
			return "run()";
		}
	}
	
	private static final int RANDOM_WALK_RATE = 30;
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 300;
	
	/** The delay after which the attacked is stopped */
	private int	_attackTimeout;
	
	/** The L2Attackable aggro counter */
	private int	_globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress */
	private volatile boolean _thinking; // to prevent recursive thinking
	
	private String _clan;
	private String _enemyClan;
	
	private int _timePass = 0;
	private int _chaosTime = 0;
	private final L2NpcTemplate _skillRender;
	private int _lastBuffTick;
	
	/**
	 * Constructor of L2AttackableAI.<BR><BR>
	 * 
	 * @param accessor The AI accessor of the L2Character
	 * 
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		L2Attackable me = getActor();
		_skillRender = me.getTemplate();
		// _selfAnalysis.init(_actor);
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
		if (!"none".equals(me.getEnemyClan()))
			setEnemyClan(me.getEnemyClan());
	}
	
	@Override
	public L2Attackable getActor()
	{
		return (L2Attackable) _actor;
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		notifyEvent(CtrlEvent.EVT_THINK);
	}
	
	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
	 * 
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 * 
	 * @param target The targeted L2Object
	 * 
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if (target == null)
			return false;
		
		L2Attackable me = getActor();
		
		if (target instanceof L2Attackable)
		{
			setClan(((L2Attackable) target).getClan());
		}
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			// However EffectInvincible requires to check GMs specially
			if (target instanceof L2Playable && target.getActingPlayer().isGM())
				return false;
		}

		// Check if the target isn't a Folk or a Door
		if (target instanceof L2DoorInstance)
			return false;

		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead()
				|| (target instanceof L2PcInstance && !me.isInsideRadius(target, me.getAggroRange(), false, false))
				|| Math.abs(_actor.getZ() - target.getZ()) > 300
				|| (target instanceof L2Summon && !me.isInsideRadius(target, me.getAggroRange(), false, false)))
			return false;
		
		// Check if the target is a L2Playable
		if (target instanceof L2Playable)
		{
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!(me.isRaid()) && !(me.canSeeThroughSilentMove()) && ((L2Playable)target).isSilentMoving())
				return false;
		}

		// Check if the target is a L2PcInstance
		if (target instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) target;
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (player.isGM() && player.getAccessLevel() <= Config.GM_DONT_TAKE_AGGRO)
				return false;

			// TODO: Ideally, autoattack condition should be called from the AI script.  In that case,
			// it should only implement the basic behaviors while the script will add more specific
			// behaviors (like varka/ketra alliance, etc).  Once implemented, remove specialized stuff
			// from this location.  (Fulminus)

			// Check if player is an ally (comparing mem addr)
			if (me.getFactionId() == "varka" && player.isAlliedWithVarka())
				return false;
			else if (me.getFactionId() == "ketra" && player.isAlliedWithKetra())
				return false;
			
			// if player is disguised beleth faction ignores him
			if (me.getFactionId() == "beleth" && player.isTransformed() && player.getTransformationId() == 101)
				return false;
			
			// event players are also ignored
			if (player.isInFunEvent())
				return false;
			
			// check if the target is within the grace period for JUST getting up from fake death
			if (player.isRecentFakeDeath())
				return false;
			
			// if (_selfAnalysis.cannotMoveOnLand && !target.isInsideZone(L2Zone.FLAG_WATER))
			// return false;
		}
		
		// Check if the target is a L2Summon
		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (owner != null)
			{
				// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
				if (owner.isGM() && (owner.isInvul() || owner.getAccessLevel() <= Config.GM_DONT_TAKE_AGGRO))
					return false;
				// Check if player is an ally (comparing mem addr)
				if (me.getFactionId() == "varka" && owner.isAlliedWithVarka())
					return false;
				if (me.getFactionId() == "ketra" && owner.isAlliedWithKetra())
					return false;
			}
		}
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				// Los Check
				return GeoData.getInstance().canSeeTarget(me, target);

			//if (target instanceof L2Summon)
			//  return ((L2Summon)target).getKarma() > 0;

			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof L2MonsterInstance && Config.ALLOW_GUARDS)
				return (((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target));

			return false;
		}
		else if (_actor instanceof L2FriendlyMobInstance)
		{ // the actor is a L2FriendlyMobInstance
			
			// Check if the target isn't another L2Npc
			if (target instanceof L2Npc)
				return false;

			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			return false;
		}
		else
		{
			if (target instanceof L2Attackable)
			{
				if ("none".equals(((L2Attackable) _actor).getEnemyClan())
						|| "none".equals(((L2Attackable) target).getClan()))
					return false;
				
				if (me.getEnemyClan().equals(((L2Attackable) target).getClan()))
				{
					if (_actor.isInsideRadius(target, me.getEnemyRange(), false, false))
					{
						return GeoData.getInstance().canSeeTarget(_actor, target);
					}
					else
						return false;
				}
				if (((L2Attackable) _actor).getIsChaos() > 0
						&& me.isInsideRadius(target, me.getIsChaos(), false, false))
				{
					if (me.getFactionId() != null
							&& me.getFactionId().equals(((L2Attackable) target).getFactionId()))
					{
						return false;
					}
					// Los Check
					return GeoData.getInstance().canSeeTarget(me, target);
				}
			}

			if (target instanceof L2Attackable || target instanceof L2Npc)
				return false;

			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGGRO_IN_PEACEZONE && target.isInsideZone(L2Zone.FLAG_PEACE))
				return false;

			if (me.isChampion() && Config.CHAMPION_PASSIVE)
				return false;

			// Check if the actor is Aggressive
			return (me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
		}
	}
	
	public void startAITask()
	{
		AttackableAiTaskManager.getInstance().startTask(this);
		
		if (_actor instanceof L2GuardInstance)
			((L2GuardInstance) _actor).startReturnTask();
	}
	
	@Override
	public void stopAITask()
	{
		if (_actor instanceof L2GuardInstance)
			((L2GuardInstance) _actor).stopReturnTask();
		
		AttackableAiTaskManager.getInstance().stopTask(this);
		_accessor.detachAI();
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}

	/**
	 * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 * 
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			// Check if actor is not dead
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = getActor();

				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
					intention = AI_INTENTION_ACTIVE;
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), range + range, true, false))
							intention = AI_INTENTION_ACTIVE;
					}
				}
			}

			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);

				// Stop AI task and detach AI from NPC
				stopAITask();
				return;
			}
		}

		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);

		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
	 *
	 * @param target The L2Character to attack
	 *
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		// self and buffs
		
		if (_lastBuffTick + 30 < GameTimeController.getGameTicks())
		{
			if (getSkillRender().hasBuffSkill())
				for (L2Skill sk : getSkillRender()._buffskills)
					if (cast(sk))
						break;
			
			_lastBuffTick = GameTimeController.getGameTicks();
		}
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR><BR>
	 * 
	 */
	protected void thinkActive()
	{
		L2Attackable npc = getActor();

		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
/*
		switch (npc.getFleeingStatus())
		{
		// 10 million
		case L2Attackable.FLEEING_STARTED:
		// 1 million
		case L2Attackable.FLEEING_DONE_RETURNING:
			return;
		}
*/
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			Collection<L2Object> objs = _actor.getKnownList().getKnownObjects().values();
			for (L2Object obj : objs)
			{
				if (!(obj instanceof L2Character))
					continue;
				L2Character target = (L2Character) obj;

				/*
				 * Check to see if this is a festival mob spawn.
				 * If it is, then check to see if the aggro trigger
				 * is a festival participant...if so, move to attack it.
				 */
				if ((_actor instanceof L2FestivalMonsterInstance) && obj instanceof L2PcInstance)
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;

					if (!(targetPlayer.isFestivalParticipant()))
						continue;
				}
				
				// TODO: The AI Script ought to handle aggro behaviors in onSee.  Once implemented, aggro behaviors ought
				// to be removed from here.  (Fulminus)
				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					int hating = npc.getHating(target);

					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
						npc.addDamageHate(target, 0, 0);
				}
			}

			// Chose a target from its aggroList
			L2Character hated;
			if (_actor.isConfused())
				hated = getAttackTarget(); // effect handles selection
			else
				hated = npc.getMostHated();

			// Order to the L2Attackable to attack the target
			if (hated != null)
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);

				if (aggro + _globalAggro > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
						_actor.setRunning();

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
					// [L2J_JP ADD]
					// following boss
					L2MinionInstance minion;
					L2MonsterInstance boss;
					Set<L2MinionInstance> minions;
					if (_actor instanceof L2MonsterInstance)
					{
						boss = (L2MonsterInstance) _actor;
						if (boss.hasMinions())
						{
							minions = boss.getSpawnedMinions();
							for (L2MinionInstance m : minions)
							{
								if (!m.isRunning())
									m.setRunning();
								m.getAI().startFollow(_actor);
							}
						}
					}
					else if (_actor instanceof L2MinionInstance)
					{
						minion = (L2MinionInstance) _actor;
						boss = minion.getLeader();
						if (!boss.isRunning())
							boss.setRunning();
						boss.getAI().startFollow(_actor);
						minions = boss.getSpawnedMinions();
						for (L2MinionInstance m : minions)
						{
							if (!(m.getObjectId() == _actor.getObjectId()))
							{
								if (!m.isRunning())
									m.setRunning();
								m.getAI().startFollow(_actor);
							}
						}

					}
				}

				return;
			}

		}

		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			((L2GuardInstance) _actor).returnHome();
		}

		// If this is a festival monster, then it remains in the same location.
		if (_actor instanceof L2FestivalMonsterInstance)
			return;

		// Check if the mob should not return to spawn point
		if (!npc.canReturnToSpawnPoint())
			return;

		// Minions following leader
		if (_actor instanceof L2MinionInstance && ((L2MinionInstance) _actor).getLeader() != null)
		{
			int offset;

			if (_actor.isRaid())
				offset = 500; // for Raid minions - need correction
			else
				offset = 200; // for normal minions - need correction :)

			if (((L2MinionInstance) _actor).getLeader().isRunning())
				_actor.setRunning();
			else
				_actor.setWalking();

			if (_actor.getPlanDistanceSq(((L2MinionInstance) _actor).getLeader()) > offset * offset)
			{
				int x1, y1, z1;
				x1 = ((L2MinionInstance) _actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				y1 = ((L2MinionInstance) _actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				z1 = ((L2MinionInstance) _actor).getLeader().getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
				moveTo(x1, y1, z1);
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				if (getSkillRender().hasBuffSkill())
					for (L2Skill sk : getSkillRender()._buffskills)
						if (cast(sk))
							return;
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0
				&& !_actor.isNoRndWalk())
		{
			if (getSkillRender().hasBuffSkill())
				for (L2Skill sk : getSkillRender()._buffskills)
					if (cast(sk))
						return;
			
			if (_actor instanceof L2Boss
					|| _actor instanceof L2MinionInstance
					|| _actor instanceof L2ChestInstance
					|| _actor instanceof L2GuardInstance)
				return;
			
			final int range = Config.MAX_DRIFT_RANGE;
			
			int x1;
			int y1;
			int z1;
			if (npc.getFleeingStatus() == L2Attackable.FLEEING_NOT_STARTED
					|| npc.getMoveAroundPos() == null)
			{
				x1 = npc.getSpawn().getLocx();
				y1 = npc.getSpawn().getLocy();
				z1 = npc.getSpawn().getLocz();
			}
			else
			{
				x1 = npc.getMoveAroundPos().x;
				y1 = npc.getMoveAroundPos().y;
				z1 = npc.getMoveAroundPos().z;
			}
			
			if (!_actor.isInsideRadius(x1, y1, z1, range + range, true, false))
			{
				npc.setisReturningToSpawnPoint(true);
			}
			else
			{
				x1 += Rnd.nextInt(range * 2) - range;
				y1 += Rnd.nextInt(range * 2) - range;
				z1 = npc.getZ();
			}
			
			//_log.config("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
			moveTo(x1, y1, z1);
			// [L2J_JP ADD]
			// following boss
			if (_actor instanceof L2MonsterInstance)
			{
				L2MonsterInstance boss = (L2MonsterInstance) _actor;
				boss.callMinions();
			}
		}
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
	 */
	protected void thinkAttack()
	{
		if (_attackTimeout < GameTimeController.getGameTicks())
		{
			// Check if the actor is running
			if (_actor.isRunning())
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				_actor.setWalking();

				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}

		if (_actor.isCastingNow())
			return;

		L2Character originalAttackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() || _attackTimeout < GameTimeController.getGameTicks())
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
				getActor().stopHating(originalAttackTarget);

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);

			_actor.setWalking();
			return;
		}

		// Handle all L2Object of its Faction inside the Faction Range
		L2Attackable me = getActor();
		if (me.getFactionId() != null)
		{
			String faction_id = me.getFactionId();

			// Go through all L2Object that belong to its faction
			for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2Npc)
				{
					L2Npc npc = (L2Npc) obj;

					//Handle SevenSigns mob Factions
					String npcfaction = npc.getFactionId();
					boolean sevenSignFaction = false;

					// TODO: Unhardcode this by AI scripts (DrHouse)
					//Catacomb mobs should assist lilim and nephilim other than dungeon
					if ( (faction_id == "c_dungeon_clan") &&
						((npcfaction == "c_dungeon_lilim") || npcfaction == "c_dungeon_nephi"))
						sevenSignFaction = true;
					//Lilim mobs should assist other Lilim and catacomb mobs
					else if ( (faction_id == "c_dungeon_lilim") &&
						(npcfaction == "c_dungeon_clan"))
						sevenSignFaction = true;
					//Nephilim mobs should assist other Nephilim and catacomb mobs
					else if ( (faction_id == "c_dungeon_nephi") &&
						(npcfaction == "c_dungeon_clan"))
						sevenSignFaction = true;
					
					if (faction_id != npc.getFactionId() && !sevenSignFaction)
						continue;

					// Check if the L2Object is inside the Faction Range of the actor
					if (_actor.isInsideRadius(npc, npc.getFactionRange() + npc.getTemplate().getCollisionRadius(), true, false) && npc.getAI() != null)
					{
						if (Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600 && _actor.getAttackByList().contains(originalAttackTarget)
								&& (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
								&& GeoData.getInstance().canSeeTarget(_actor, npc))
						{
							if ((originalAttackTarget instanceof L2PcInstance) || (originalAttackTarget instanceof L2Summon))
							{
								L2PcInstance player = (originalAttackTarget instanceof L2PcInstance) ? (L2PcInstance) originalAttackTarget : ((L2Summon) originalAttackTarget)
										.getOwner();
								Quest[] quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL);
								if (quests != null)
								{
									for (Quest quest : quests)
										quest.notifyFactionCall(npc, me, player, (originalAttackTarget instanceof L2Summon));
								}
							}
							else if (npc instanceof L2Attackable && getAttackTarget() != null
									&& npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
							{
								((L2Attackable) npc).addDamageHate(getAttackTarget(), 0, me.getHating(getAttackTarget()));
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
							}
							
						}
					}
				}
			}
		}
		
		// Return when Attack been disable
		if (_actor.isAttackingDisabled())
			return;

		switch (me.getFleeingStatus())
		{
		// 10 million
		case L2Attackable.FLEEING_STARTED:
		// 1 million
		case L2Attackable.FLEEING_DONE_RETURNING:
			return;
		}

		/*
		 * if(_actor.getTarget() == null || this.getAttackTarget() == null || this.getAttackTarget().isDead() || ctarget == _actor) AggroReconsider();
		 */

		// ----------------------------------------------------------------
		// ------------------------------------------------------------------------------
		// Initialize data
		double dist = 0;
		int dist2 = 0;
		int range = 0;
		L2Character MostHate = me.getMostHated();
		// L2Character ctarget = (L2Character)_actor.getTarget();
		try
		{
			setAttackTarget(MostHate);
			_actor.setTarget(MostHate);
			dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
			dist2 = (int) dist;
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius()
					+ getAttackTarget().getTemplate().getCollisionRadius();
			if (getAttackTarget().isMoving())
			{
				range = range + 50;
				if (_actor.isMoving())
					range = range + 50;
			}
		}
		catch (NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		// ------------------------------------------------------
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		if (!_actor.isRooted() && Rnd.nextInt(100) <= 33) // check it once per 3 seconds
		{
			int combinedCollision = _actor.getTemplate().getCollisionRadius()
					+ getAttackTarget().getTemplate().getCollisionRadius();
			int collision = _actor.getTemplate().getCollisionRadius();
			for (L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(collision))
			{
				if (nearby instanceof L2Attackable && nearby != getAttackTarget())
				{
					int diffx = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
						diffx = -diffx;
					int diffy = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
						diffy = -diffy;
					moveTo(getAttackTarget().getX() + diffx, getAttackTarget().getY() + diffy, getAttackTarget().getZ());
					return;
				}
			}
		}
		// Dodge if its needed
		if (!_actor.isRooted() && me.getCanDodge() > 0)
		{
			if (Rnd.get(100) <= me.getCanDodge())
			{
				// Micht: kepping this one otherwise we should do 2 sqrt
				double distance2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
				if (Math.sqrt(distance2) <= (60 + _actor.getTemplate().getCollisionRadius() + getAttackTarget()
						.getTemplate().getCollisionRadius()))
				{
					// Diasable the RND for increasing the performance
					// int chance = 60;
					// if (chance >= Rnd.get(100))
					// {
					int posX = _actor.getX();
					int posY = _actor.getY();
					int posZ = _actor.getZ();
					/*
					 * if (Rnd.get(1) > 0) posX =((L2Attackable) _actor).getSpawn().getLocx() + Rnd.get(100); else posX = ((L2Attackable) _actor).getSpawn().getLocx() - Rnd.get(100); if (Rnd.get(1) > 0) posY = ((L2Attackable) _actor).getSpawn().getLocy() + Rnd.get(100); else
					 * posY =((L2Attackable) _actor).getSpawn().getLocy() - Rnd.get(100); setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
					 */
					if (Rnd.get(1) > 0)
						posX = posX + Rnd.get(100);
					else
						posX = posX - Rnd.get(100);
					
					if (Rnd.get(1) > 0)
						posY = posY + Rnd.get(100);
					else
						posY = posY - Rnd.get(100);
					
					setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
					return;
					// }
				}
			}
		}

		// ------------------------------------------------------------------------------
		// BOSS/Raid Minion Target Reconsider
		if (_actor.isRaid() || _actor.isRaidMinion())
		{
			_chaosTime++;
			if (_actor instanceof L2RaidBossInstance && !((L2MonsterInstance) _actor).hasMinions())
			{
				if (_chaosTime > Config.RAID_CHAOS_TIME)
					if (Rnd.get(100) <= 100 - (_actor.getCurrentHp() * 100 / _actor.getMaxHp()))
					{
						aggroReconsider();
						_chaosTime = 0;
					}
			}
			else if (_actor instanceof L2RaidBossInstance && ((L2MonsterInstance)_actor).hasMinions())
			{
				if (_chaosTime > Config.RAID_CHAOS_TIME)
					if (Rnd.get(100) <= 100 - (_actor.getCurrentHp() * 200 / _actor.getMaxHp()))
					{
						aggroReconsider();
						_chaosTime = 0;
					}
			}
			else if (_actor instanceof L2GrandBossInstance)
			{
				if (_chaosTime > Config.GRAND_CHAOS_TIME)
					if (Rnd.get(100) <= 100 - (_actor.getCurrentHp() * 300 / _actor.getMaxHp()))
					{
						aggroReconsider();
						_chaosTime = 0;
					}
			}
			else
			{
				if (_chaosTime > Config.MINION_CHAOS_TIME)
					if (Rnd.get(100) <= 100 - (_actor.getCurrentHp() * 200 / _actor.getMaxHp()))
					{
						aggroReconsider();
						_chaosTime = 0;
					}
			}
		}
		
		if (getSkillRender().hasSkill())
		{
			// -------------------------------------------------------------------------------
			// Heal Condition
			if (getSkillRender().hasHealSkill() && getSkillRender()._healskills != null)
			{
				double percentage = _actor.getCurrentHp() / _actor.getMaxHp() * 100;
				if (_actor instanceof L2MinionInstance)
				{
					L2Character leader = ((L2MinionInstance)_actor).getLeader();
					if (leader != null && !leader.isDead()
							&& Rnd.get(100) > (leader.getCurrentHp() / leader.getMaxHp() * 100))
						for (L2Skill sk : getSkillRender()._healskills)
						{
							if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
								continue;
							if ((sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId())
									|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted())))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + _actor.getTemplate().getCollisionRadius() + leader
									.getTemplate().getCollisionRadius()), _actor, leader, false)
									&& !isParty(sk) && !_actor.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
										+ leader.getTemplate().getCollisionRadius());
							}
							if (GeoData.getInstance().canSeeTarget(_actor, leader))
							{
								clientStopMoving(null);
								_actor.setTarget(leader);
								clientStopMoving(null);
								_actor.doCast(sk);
								return;
							}
						}
				}
				if (Rnd.get(100) < (100 - percentage) / 3)
					for (L2Skill sk : getSkillRender()._healskills)
					{
						if ((sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId()) || (sk
								.isMagic() && _actor.isMuted()))
								|| (!sk.isMagic() && _actor.isPhysicalMuted()))
						{
							continue;
						}
						clientStopMoving(null);
						_actor.setTarget(_actor);
						_actor.doCast(sk);
						return;
					}
				for (L2Skill sk : getSkillRender()._healskills)
				{
					if ((sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId()) || (sk
							.isMagic() && _actor.isMuted()))
							|| (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
						for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
								sk.getCastRange() + _actor.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
								continue;
							
							L2Attackable targets = ((L2Attackable)obj);
							if (me.getFactionId() != targets.getFactionId()
									&& me.getFactionId() != null)
								continue;
							percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
							if (Rnd.get(100) < (100 - percentage) / 10)
							{
								if (GeoData.getInstance().canSeeTarget(_actor, targets))
								{
									clientStopMoving(null);
									_actor.setTarget(obj);
									_actor.doCast(sk);
									return;
								}
							}
						}
					if (isParty(sk))
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return;
					}
				}
			}
			// -------------------------------------------------------------------------------
			// Res Skill Condition
			if (getSkillRender().hasResSkill())
			{
				if (_actor instanceof L2MinionInstance)
				{
					L2Character leader = ((L2MinionInstance)_actor).getLeader();
					if (leader != null && leader.isDead())
						for (L2Skill sk : getSkillRender()._resskills)
						{
							if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
								continue;
							if ((sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId())
									|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted())))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + _actor.getTemplate().getCollisionRadius() + leader
									.getTemplate().getCollisionRadius()), _actor, leader, false)
									&& !isParty(sk) && !_actor.isRooted())
							{
								moveToPawn(leader, sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
										+ leader.getTemplate().getCollisionRadius());
							}
							if (GeoData.getInstance().canSeeTarget(_actor, leader))
							{
								clientStopMoving(null);
								_actor.setTarget(((L2MinionInstance) _actor).getLeader());
								_actor.doCast(sk);
								return;
							}
						}
				}
				for (L2Skill sk : getSkillRender()._resskills)
				{
					if ((sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId()) || (sk
							.isMagic() && _actor.isMuted()))
							|| (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
						for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
								sk.getCastRange() + _actor.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable) || !obj.isDead())
								continue;
							
							L2Attackable targets = ((L2Attackable)obj);
							if (me.getFactionId() != targets.getFactionId()
									&& me.getFactionId() != null)
								continue;
							if (Rnd.get(100) < 10)
							{
								if (GeoData.getInstance().canSeeTarget(_actor, targets))
								{
									clientStopMoving(null);
									_actor.setTarget(obj);
									_actor.doCast(sk);
									return;
								}
							}
						}
					if (isParty(sk))
					{
						clientStopMoving(null);
						L2Object target = getAttackTarget();
						_actor.setTarget(_actor);
						_actor.doCast(sk);
						_actor.setTarget(target);
						return;
					}
				}
			}
		}

		// -------------------------------------------------------------------------------
		// Immobilize Condition
		if ((_actor.isRooted() && (dist > range || getAttackTarget().isMoving()))
				|| (dist > range && getAttackTarget().isMoving()))
		{
			movementDisable();
			return;
		}
		setTimePass(0);
		// --------------------------------------------------------------------------------
		// Skill Use
		if (getSkillRender().hasSkill())
		{
			if (Rnd.get(100) <= me.getSkillChance())
			{
				L2Skill skills = getSkillRender()._generalskills.get(Rnd.nextInt(getSkillRender()._generalskills.size()));
				if (cast(skills))
					return;
				for (L2Skill sk : getSkillRender()._generalskills)
					if (cast(sk))
						return;
			}
			
			// --------------------------------------------------------------------------------
			// Long/Short Range skill Usage
			if (me.hasLSkill() || me.hasSSkill())
			{
				if (me.hasSSkill() && dist2 <= 150 && Rnd.get(100) <= me.getSSkillChance())
				{
					sSkillRender();
					if (getSkillRender()._Srangeskills != null)
						for (L2Skill sk : getSkillRender()._Srangeskills)
							if (cast(sk))
								return;
				}
				if (me.hasLSkill() && dist2 >= 300 && Rnd.get(100) <= me.getSSkillChance())
				{
					lSkillRender();
					if (getSkillRender()._Lrangeskills != null)
						for (L2Skill sk : getSkillRender()._Lrangeskills)
							if (cast(sk))
								return;
				}
				
			}
		}
		
		// --------------------------------------------------------------------------------
		// Starts Melee or Primary Skill
		if (dist2 > range || !GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
		{
			if (_actor.isRooted())
			{
				targetReconsider();
				return;
			}
			else
			{
				if (getAttackTarget().isMoving())
					range -= 100;
				if (range < 5)
					range = 5;
				moveToPawn(getAttackTarget(), range);
				return;
			}
		}
		else
		{
			melee(me.getPrimaryAttack());
		}
	}
	
	private void melee(int type)
	{
		if (type != 0)
		{
			switch (type)
			{
				case -1:
				{
					if (getSkillRender()._generalskills != null)
						for (L2Skill sk : getSkillRender()._generalskills)
							if (cast(sk))
								return;
					break;
				}
				case 1:
				{
					if (getSkillRender().hasAtkSkill())
						for (L2Skill sk : getSkillRender()._atkskills)
							if (cast(sk))
								return;
					break;
				}
				default:
				{
					if (getSkillRender()._generalskills != null)
						for (L2Skill sk : getSkillRender()._generalskills)
							if (sk.getId() == getActor().getPrimaryAttack())
								if (cast(sk))
									return;
				}
					break;
			}
		}
		
		_accessor.doAttack(getAttackTarget());
	}
	
	private boolean cast(L2Skill sk)
	{
		if (sk == null)
			return false;
		if (sk.getMpConsume() >= _actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId())
				|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
			return false;
		L2Attackable me = getActor();
		if (getAttackTarget() == null)
			if (me.getMostHated() != null)
				setAttackTarget(me.getMostHated());
		if (getAttackTarget() == null)
			return false;
		double dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
		double dist2 = dist + getAttackTarget().getTemplate().getCollisionRadius();
		double range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius()
				+ getAttackTarget().getTemplate().getCollisionRadius();
		double srange = sk.getCastRange() + _actor.getTemplate().getCollisionRadius();
		if (getAttackTarget().isMoving())
			dist2 = dist2 - 400;
		
		switch (sk.getSkillType())
		{
			case BUFF:
			case REFLECT:
			{
				if (_actor.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					// L2Object target = getAttackTarget();
					_actor.setTarget(_actor);
					_actor.doCast(sk);
					// _actor.setTarget(target);
					return true;
				}
				// ----------------------------------------
				// If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
					return false;
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, true);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = getAttackTarget();
						_actor.setTarget(target);
						_actor.doCast(sk);
						_actor.setTarget(targets);
						return true;
					}
				}
				if (canParty(sk))
				{
					clientStopMoving(null);
					L2Object targets = getAttackTarget();
					_actor.setTarget(_actor);
					_actor.doCast(sk);
					_actor.setTarget(targets);
					return true;
				}
				break;
			}
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE:
			{
				double percentage = _actor.getCurrentHp() / _actor.getMaxHp() * 100;
				if (_actor instanceof L2MinionInstance && sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
				{
					L2Character leader = ((L2MinionInstance)_actor).getLeader();
					if (leader != null && !leader.isDead()
							&& Rnd.get(100) > (leader.getCurrentHp() / leader.getMaxHp() * 100))
					{
						if (!Util.checkIfInRange((sk.getCastRange() + _actor.getTemplate().getCollisionRadius() + leader
								.getTemplate().getCollisionRadius()), _actor, leader, false)
								&& !isParty(sk) && !_actor.isRooted())
						{
							moveToPawn(leader, sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ leader.getTemplate().getCollisionRadius());
						}
						if (GeoData.getInstance().canSeeTarget(_actor, leader))
						{
							clientStopMoving(null);
							_actor.setTarget(leader);
							_actor.doCast(sk);
							return true;
						}
					}
				}
				if (Rnd.get(100) < (100 - percentage) / 3)
				{
					clientStopMoving(null);
					_actor.setTarget(_actor);
					_actor.doCast(sk);
					return true;
				}
				
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
							sk.getCastRange() + _actor.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable) || obj.isDead())
							continue;
						L2Attackable targets = ((L2Attackable)obj);
						if (me.getFactionId() != targets.getFactionId()
								&& me.getFactionId() != null)
							continue;
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if (Rnd.get(100) < (100 - percentage) / 10)
						{
							if (GeoData.getInstance().canSeeTarget(_actor, targets))
							{
								clientStopMoving(null);
								_actor.setTarget(obj);
								_actor.doCast(sk);
								return true;
							}
						}
					}
				}
				if (isParty(sk))
				{
					for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
							sk.getSkillRadius() + _actor.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						L2Npc actors = me;
						if (actors.getFactionId() != null && targets.getFactionId().equals(actors.getFactionId()))
						{
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
							{
								clientStopMoving(null);
								_actor.setTarget(_actor);
								_actor.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case RESURRECT:
			{
				if (!isParty(sk))
				{
					if (_actor instanceof L2MinionInstance && sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
					{
						L2Character leader = ((L2MinionInstance) _actor).getLeader();
						if (leader != null && leader.isDead())
							if (!Util.checkIfInRange((sk.getCastRange() + _actor.getTemplate().getCollisionRadius() + leader
									.getTemplate().getCollisionRadius()), _actor, leader, false)
									&& !isParty(sk) && !_actor.isRooted())
							{
								moveToPawn(leader, sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
										+ leader.getTemplate().getCollisionRadius());
							}
						if (GeoData.getInstance().canSeeTarget(_actor, leader))
						{
							clientStopMoving(null);
							_actor.setTarget(((L2MinionInstance) _actor).getLeader());
							_actor.doCast(sk);
							return true;
						}
					}
					
					for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
							sk.getCastRange() + _actor.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable) || !obj.isDead())
							continue;
						
						L2Attackable targets = ((L2Attackable) obj);
						if (me.getFactionId() != targets.getFactionId()
								&& me.getFactionId() != null)
							continue;
						if (Rnd.get(100) < 10)
						{
							if (GeoData.getInstance().canSeeTarget(_actor, targets))
							{
								clientStopMoving(null);
								_actor.setTarget(obj);
								_actor.doCast(sk);
								return true;
							}
						}
					}
				}
				else if (isParty(sk))
				{
					for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(
							sk.getSkillRadius() + _actor.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						L2Npc actors = me;
						if (actors.getFactionId() != null && actors.getFactionId().equals(targets.getFactionId()))
						{
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
							{
								clientStopMoving(null);
								_actor.setTarget(_actor);
								_actor.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case DEBUFF:
			case WEAKNESS:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
			{
				if (GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()) && !canAOE(sk)
						&& !getAttackTarget().isDead() && dist2 <= srange)
				{
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA || sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				break;
			}
			case SLEEP:
			{
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					if (getAttackTarget() != null && !getAttackTarget().isDead() && dist2 <= srange)
					{
						if (dist2 > range || getAttackTarget().isMoving())
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								// _actor.setTarget(getAttackTarget());
								_actor.doCast(sk);
								return true;
							}
						}
					}
					
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA || sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				break;
			}
			case ROOT:
			case STUN:
			case PARALYZE:
			{
				if (GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()) && !canAOE(sk) && dist2 <= srange)
				{
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					else if ((sk.getTargetType() == SkillTargetType.TARGET_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA || sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				break;
			}
			case MUTE:
			case FEAR:
			{
				if (GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()) && !canAOE(sk) && dist2 <= srange)
				{
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA || sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				break;
			}
			case CANCEL:
			case NEGATE:
			{
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = getAttackTarget();
						_actor.setTarget(target);
						_actor.doCast(sk);
						_actor.setTarget(targets);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == SkillTargetType.TARGET_AURA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))

					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					else if ((sk.getTargetType() == SkillTargetType.TARGET_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
							|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA || sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
							&& GeoData.getInstance().canSeeTarget(_actor, getAttackTarget())
							&& !getAttackTarget().isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
				}
				break;
			}
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case DEATHLINK:
			case CPDAM:
			case MANADAM:
			{
				if (!canAura(sk))
				{
					if (GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()) && !getAttackTarget().isDead()
							&& dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
					else
					{
						L2Character target = skillTargetReconsider(sk);
						if (target != null)
						{
							clientStopMoving(null);
							L2Object targets = getAttackTarget();
							_actor.setTarget(target);
							_actor.doCast(sk);
							_actor.setTarget(targets);
							return true;
						}
					}
				}
				else
				{
					clientStopMoving(null);
					_actor.doCast(sk);
					return true;
				}
				break;
			}
			default:
			{
				if (!canAura(sk))
				{
					if (GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()) && !getAttackTarget().isDead()
							&& dist2 <= srange)
					{
						clientStopMoving(null);
						_actor.doCast(sk);
						return true;
					}
					else
					{
						L2Character target = skillTargetReconsider(sk);
						if (target != null)
						{
							clientStopMoving(null);
							L2Object targets = getAttackTarget();
							_actor.setTarget(target);
							_actor.doCast(sk);
							_actor.setTarget(targets);
							return true;
						}
					}
				}
				else
				{
					clientStopMoving(null);
					// L2Object targets = getAttackTarget();
					// _actor.setTarget(_actor);
					_actor.doCast(sk);
					// _actor.setTarget(targets);
					return true;
				}
			}
			break;
		}
		
		return false;
	}
	
	/**
	 * This AI task will start when ACTOR cannot move and attack range larger than distance
	 */
	private void movementDisable()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try
		{
			if (_actor.getTarget() == null)
				_actor.setTarget(getAttackTarget());
			dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
			dist2 = dist;
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius()
					+ getAttackTarget().getTemplate().getCollisionRadius();
			if (getAttackTarget().isMoving())
			{
				dist = dist - 30;
				if (_actor.isMoving())
					dist = dist - 50;
			}
		}
		catch (NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		// Check if activeChar has any skill
		if (getSkillRender().hasSkill())
		{
			// -------------------------------------------------------------
			// Try to stop the target or disable the target as priority
			if (getSkillRender().hasImmobiliseSkill())
			{
				for (L2Skill sk : getSkillRender()._immobiliseskills)
				{
					if (sk.getMpConsume() >= _actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							|| (sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
									|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (!GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
						continue;
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
			}
			// -------------------------------------------------------------
			// Same as Above, but with Mute/FEAR etc....
			if (getSkillRender().hasCOTSkill())
			{
				for (L2Skill sk : getSkillRender()._cotskills)
				{
					if (sk.getMpConsume() >= _actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							|| (sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
									|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (!GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
						continue;
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
			}
			// -------------------------------------------------------------
			if (getSkillRender().hasDebuffSkill() && Rnd.get(10) <= 2)
			{
				for (L2Skill sk : getSkillRender()._debuffskills)
				{
					if (sk.getMpConsume() >= _actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							|| (sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
							|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (!GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
						continue;
					if (getAttackTarget().getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
			}
			// -------------------------------------------------------------
			// Some side effect skill like CANCEL or NEGATE
			if (getSkillRender().hasNegativeSkill() && Rnd.get(10) == 1)
			{
				for (L2Skill sk : getSkillRender()._negativeskills)
				{
					if (sk.getMpConsume() >= _actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							|| (sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
							|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (!GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
						continue;
					if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
					{
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						_actor.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
			}
			// -------------------------------------------------------------
			// Start ATK SKILL when nothing can be done
			if (getSkillRender().hasAtkSkill())
			{
				for (L2Skill sk : getSkillRender()._atkskills)
				{
					if (sk.getMpConsume() >= _actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							|| (sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
							|| (sk.isMagic() && _actor.isMuted()) || (!sk.isMagic() && _actor.isPhysicalMuted()))
					{
						continue;
					}
					if (!GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
						continue;
					clientStopMoving(null);
					// L2Object target = getAttackTarget();
					// _actor.setTarget(_actor);
					_actor.doCast(sk);
					// _actor.setTarget(target);
					return;
				}
			}
			// -------------------------------------------------------------
			// if there is no ATK skill to use, then try Universal skill
			/*
			 * if(getSkillRender().hasUniversalSkill()) { for(L2Skill sk:getSkillRender()._universalskills) { if(sk.getMpConsume()>=_actor.getCurrentMp() || _actor.isSkillDisabled(sk.getId()) ||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <=
			 * dist2 && !canAura(sk)) ||(sk.isMagic()&&_actor.isMuted()) ||(!sk.isMagic()&&_actor.isPhysicalMuted())) { continue; } if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget())) continue; clientStopMoving(null); L2Object target = getAttackTarget(); //_actor.setTarget(_actor);
			 * _actor.doCast(sk); //_actor.setTarget(target); return; } }
			 */
		}
		// timepass = timepass + 1;
		if (_actor.isRooted())
		{
			// setTimePass(0);
			targetReconsider();
			
			return;
		}
		// else if (timepass >= 5)
		// {
		// setTimePass(0);
		// aggroReconsider();
		// return;
		// }
		
		if (dist > range || !GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
		{
			if (getAttackTarget().isMoving())
				range -= 100;
			if (range < 5)
				range = 5;
			moveToPawn(getAttackTarget(), range);
			return;
			
		}
		
		melee(getActor().getPrimaryAttack());
		return;
	}
	
	private L2Character effectTargetReconsider(L2Skill sk, boolean positive)
	{
		if (sk == null)
			return null;
		if (sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if (!positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				L2Attackable actor = getActor();
				
				if (actor.getAttackByList() != null)
					for (L2Character obj : actor.getAttackByList())
					{
						if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(_actor, obj)
								|| obj == getAttackTarget())
							continue;
						try
						{
							_actor.setTarget(getAttackTarget());
							dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
							dist2 = dist;
							range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
									+ obj.getTemplate().getCollisionRadius();
							if (obj.isMoving())
								dist2 = dist2 - 70;
						}
						catch (NullPointerException e)
						{
							continue;
						}
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
								return obj;
						}
					}
				
				// ----------------------------------------------------------------------
				// If there is nearby Target with aggro, start going on random target that is attackable
				for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(_actor, obj))
						continue;
					try
					{
						_actor.setTarget(getAttackTarget());
						dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
								+ obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
							dist2 = dist2 - 70;
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (obj instanceof L2Attackable)
					{
						if (actor.getEnemyClan() != null
								&& (getActor().getEnemyClan().equals(((L2Attackable) obj).getClan()))
								&& !"none".equals(getActor().getEnemyClan()))
						{
							if (dist2 <= range)
							{
								if (getAttackTarget().getFirstEffect(sk) == null)
									return obj;
							}
						}
					}
					if (obj instanceof L2PcInstance || obj instanceof L2Summon)
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
								return obj;
						}
					}
				}
			}
			else if (positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (!(obj instanceof L2Attackable) || obj.isDead()
							|| !GeoData.getInstance().canSeeTarget(_actor, obj))
						continue;
					
					L2Attackable targets = (L2Attackable) obj;
					if (getActor().getFactionId() != targets.getFactionId()
							&& getActor().getFactionId() != null)
						continue;
					
					try
					{
						_actor.setTarget(getAttackTarget());
						dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
								+ obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
							dist2 = dist2 - 70;
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (obj.getFirstEffect(sk) == null)
							return obj;
					}
				}
			}
			return null;
		}
		else
		{
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
					+ getAttackTarget().getTemplate().getCollisionRadius();
			for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
			{
				if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(_actor, obj))
					continue;
				try
				{
					_actor.setTarget(getAttackTarget());
					dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist;
					range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
							+ obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
						dist2 = dist2 - 70;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (obj instanceof L2Attackable)
				{
					if (getActor().getEnemyClan() != null
							&& (getActor().getEnemyClan().equals(((L2Attackable) obj).getClan()))
							&& !"none".equals(getActor().getEnemyClan()))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
								return obj;
						}
					}
				}
				if (obj instanceof L2PcInstance || obj instanceof L2Summon)
				{
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
							return obj;
					}
				}
			}
			return null;
		}
	}
	
	private L2Character skillTargetReconsider(L2Skill sk)
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActor();
		if (actor.getHateList() != null)
			for (L2Character obj : actor.getHateList())
			{
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || obj.isDead())
					continue;
				try
				{
					_actor.setTarget(getAttackTarget());
					dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist;
					range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
							+ getAttackTarget().getTemplate().getCollisionRadius();
					// if(obj.isMoving())
					// dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (dist2 <= range)
				{
					return obj;
				}
			}
		
		if (!(_actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = _actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				try
				{
					_actor.setTarget(getAttackTarget());
					dist = Math.sqrt(_actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + _actor.getTemplate().getCollisionRadius()
							+ getAttackTarget().getTemplate().getCollisionRadius();
					// if(obj.isMoving())
					// dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				L2Character obj = null;
				if (target instanceof L2Character)
					obj = (L2Character)target;
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || dist2 > range)
					continue;
				if (obj instanceof L2PcInstance)
				{
					return obj;
				}
				if (obj instanceof L2Attackable)
				{
					L2Attackable trg = (L2Attackable) obj;
					if (actor.getEnemyClan() != null
							&& (actor.getEnemyClan().equals(trg.getClan()))
							&& !"none".equals(actor.getEnemyClan()))
					{
						return obj;
					}
					if (actor.getIsChaos() != 0)
					{
						if (trg.getFactionId() != null
								&& trg.getFactionId().equals(actor.getFactionId()))
							continue;
						else
							return obj;
					}
				}
				if (obj instanceof L2Summon)
				{
					return obj;
				}
			}
		}
		return null;
	}
	
	private void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActor();
		L2Character MostHate = actor.getMostHated();
		if (actor.getHateList() != null)
		{
			for (L2Character obj : actor.getHateList())
			{
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || obj.isDead() || obj != MostHate
						|| obj == _actor)
					continue;
				try
				{
					dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist;
					range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius()
							+ obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
						dist2 = dist2 - 70;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				
				if (dist2 <= range)
				{
					if (MostHate != null)
						actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
					else
						actor.addDamageHate(obj, 2000, 2000);
					_actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if (!(_actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = _actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
					obj = (L2Character) target;
				
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || obj.isDead() || obj != MostHate
						|| obj == _actor || obj == getAttackTarget())
					continue;
				if (obj instanceof L2PcInstance)
				{
					if (MostHate != null)
						actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
					else
						actor.addDamageHate(obj, 2000, 2000);
					_actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					L2Attackable trg = (L2Attackable) obj;
					if (actor.getEnemyClan() != null
							&& (actor.getEnemyClan().equals(trg.getClan()))
							&& !"none".equals(actor.getEnemyClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						_actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if (trg.getFactionId() != null
								&& trg.getFactionId().equals(actor.getFactionId()))
							continue;
						else
						{
							if (MostHate != null)
								actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
							else
								actor.addDamageHate(obj, 2000, 2000);
							_actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
						actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
					else
						actor.addDamageHate(obj, 2000, 2000);
					_actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	@SuppressWarnings("null")
	private void aggroReconsider()
	{
		L2Attackable actor = getActor();
		L2Character MostHate = actor.getMostHated();
		
		if (actor.getHateList() != null)
		{
			int rand = Rnd.get(actor.getHateList().size());
			int count = 0;
			for (L2Character obj : actor.getHateList())
			{
				if (count < rand)
				{
					count++;
					continue;
				}
				
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || obj.isDead()
						|| obj == getAttackTarget() || obj == actor)
					continue;
				
				try
				{
					_actor.setTarget(getAttackTarget());
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (MostHate != null)
					actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
				else
					actor.addDamageHate(obj, 2000, 2000);
				_actor.setTarget(obj);
				setAttackTarget(obj);
				return;
				
			}
		}
		
		if (!(_actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = _actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
					obj = (L2Character)target;
				else
					continue;
				if (obj == null || !GeoData.getInstance().canSeeTarget(_actor, obj) || obj.isDead() || obj != MostHate
						|| obj == _actor)
					continue;
				if (obj instanceof L2PcInstance)
				{
					if (MostHate != null && !MostHate.isDead())
						actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
					else
						actor.addDamageHate(obj, 2000, 2000);
					_actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					L2Attackable trg = (L2Attackable) obj;
					if (actor.getEnemyClan() != null
							&& (actor.getEnemyClan().equals(trg.getClan()))
							&& !"none".equals(actor.getEnemyClan()))
					{
						if (MostHate != null)
							actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
						else
							actor.addDamageHate(obj, 2000, 2000);
						_actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if (trg.getFactionId() != null
								&& trg.getFactionId().equals(actor.getFactionId()))
							continue;
						else
						{
							if (MostHate != null)
								actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
							else
								actor.addDamageHate(obj, 2000, 2000);
							_actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
						actor.addDamageHate(obj, actor.getHating(MostHate), actor.getHating(MostHate));
					else
						actor.addDamageHate(obj, 2000, 2000);
					_actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}

	private void lSkillRender()
	{
		if (getSkillRender()._Lrangeskills == null)
			getSkillRender()._Lrangeskills = getActor().getLrangeSkill();
	}
	
	private void sSkillRender()
	{
		if (getSkillRender()._Srangeskills == null)
			getSkillRender()._Srangeskills = getActor().getSrangeSkill();
	}

	/**
	 * Manage AI thinking actions of a L2Attackable.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking || _actor.isAllSkillsDisabled())
			return;

		// Start thinking action
		_thinking = true;

		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
				thinkActive();
			else if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 * 
	 * @param attacker The L2Character that attacks the actor
	 * 
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		/*if (_actor instanceof L2ChestInstance && !((L2ChestInstance)_actor).isOpenFailed())
		{
			((L2ChestInstance)_actor).deleteMe();
			((L2ChestInstance)_actor).getSpawn().startRespawn();
		}*/

		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
			_globalAggro = 0;

		// Add the attacker to the _aggroList of the actor
		getActor().addDamageHate(attacker, 0, 1);

		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
			_actor.setRunning();

		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (getActor().getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 * 
	 * @param target The L2Character that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 * 
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = getActor();

		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!_actor.isRunning())
					_actor.setRunning();

				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}

	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	/**
	 * @param clan The clan to set.
	 */
	public void setClan(String clan)
	{
		_clan = clan;
	}
	
	/**
	 * @return Returns the clan.
	 */
	public String getClan()
	{
		return _clan;
	}
	
	/**
	 * @param enemyClan
	 *            The enemyClan to set.
	 */
	public void setEnemyClan(String enemyClan)
	{
		_enemyClan = enemyClan;
	}
	
	/**
	 * @return Returns the enemyClan.
	 */
	public String getEnemyClan()
	{
		return _enemyClan;
	}
	
	/**
	 * @param timepass
	 *            The timepass to set.
	 */
	public void setTimePass(int timePass)
	{
		_timePass = timePass;
	}
	
	/**
	 * @return Returns the timepass.
	 */
	public int getTimePass()
	{
		return _timePass;
	}
	
	/**
	 * @return the NPC template
	 */
	public L2NpcTemplate getSkillRender()
	{
		return _skillRender;
	}

	@Override
	public void removeReferencesOf(L2Playable playable)
	{
		super.removeReferencesOf(playable);
		/*
		if (_mostHatedAnalysis.character == playable)
			_mostHatedAnalysis.update(getActor(), null);
		
		if (_secondMostHatedAnalysis.character == playable)
			_secondMostHatedAnalysis.update(getActor(), null);
		*/
	}
}

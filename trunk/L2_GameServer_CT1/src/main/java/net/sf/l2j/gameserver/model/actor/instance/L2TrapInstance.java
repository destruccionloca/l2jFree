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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import net.sf.l2j.tools.random.Rnd;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.effects.EffectTrap;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all Traps.
 * @author Darki699
 */
public final class L2TrapInstance extends L2NpcInstance
{
	/**
	 * Does absolutely nothing, just sends to init <b>this</b> L2NpcInstance
	 * @param objectId
	 * @param template
	 */
	public L2TrapInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private long 		_activityTime;				// Total life time of this trap
	private int			_radius;					// Trip radius of this trap
	private int 		_trapLevel		= -1;		// Caster's level when it summoned this trap
	private boolean		_deleteMe 		= false;	// If set to true, trap will be deleted after 1 second
	private boolean 	_detected 		= false;	// Trap will show a mark, marking the trap

	private L2Character _caster;					// The caster summoning this trap
	private L2Skill		_shotSkill;					// The trap's skill when it explodes
	private L2Spawn		_trapSpawn;					// The L2Spawn of this trap
	private Future 		_trapTask;					// The trap's task

	/**
	 * Since we don't support all trap levels, just returns the caster's level = trap's level
	 * @return caster's level
	 */
	public int getTrapLevel()
	{
		return _trapLevel;
	}

	/**
	 * 	 If trap is set to detected, it will show a mark where it is.
	 */
	public void setIsDetected()
	{
		_detected = true;
	}

	/**
	 * returns the last L2NpcInstance trap object
	 * @return
	 */
	public L2Character getTrapObject()
	{
		return ((_trapSpawn == null) ? null : _trapSpawn.getLastSpawn());
	}

	/**
	 * @Override
	 */
	public void eraseMe()
	{
		_caster 	= null;
		_shotSkill 	= null;

		if (_trapTask != null)
		{
			_trapTask.cancel(true);
			_trapTask = null;
		}

		EffectTrap.getInstance().delete(this);
	}

	/**
	 * @Override Trap Instance needs a caster and a skill to explode with
	 * @param caster
	 * @param skill
	 */
	public void onSpawn(L2Character caster , L2Skill skill)
	{
		if (caster == null || skill == null)
			return;

		_caster 		= caster;
		_shotSkill		= SkillTable.getInstance().getInfo(skill.getTriggeredSkill().getId(), skill.getLevel());
		_radius 		= skill.getSkillRadius();
		_activityTime	= System.currentTimeMillis() + ((skill.getDuration() > 0) ? skill.getDuration() : 60000 );
		_trapLevel		= _caster.getLevel();

		if (!onSpawn(caster.getX() , caster.getY() , caster.getZ()))
		{
			_log.error("L2TrapInstance Error: Could not spawn trap.");
		}
	}

	/**
	 * @Override Spawns the trap at x,y,z location
	 * @param x
	 * @param y
	 * @param z
	 * @return true if spawn is successful
	 */
	private boolean onSpawn(int x, int y, int z)
	{
		try
		{
			_trapSpawn = new L2Spawn(getTemplate());
			_trapSpawn.setLocx(x);
			_trapSpawn.setLocy(y);
			_trapSpawn.setLocz(z);
			_trapSpawn.setAmount(1);
			_trapSpawn.setRespawnDelay(1);
			setSpawn(_trapSpawn);

			SpawnTable.getInstance().addNewSpawn(_trapSpawn, false);
			_trapSpawn.init();
			_trapSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);

			_trapSpawn.getLastSpawn().decayMe();
			_trapSpawn.getLastSpawn().spawnMe();

			_trapSpawn.getLastSpawn().setIsImobilised(true);
			_trapSpawn.getLastSpawn().setUnTargetable(true);

			TrapTask trapTask = new TrapTask();
			_trapTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(trapTask, 3000, 1000);
			return true;
		}

		catch(Throwable t)
		{
			return false;
		}
	}

	/**
	 * Trap's task. Manages if the trap is detected or not, and when to explode or just kill itself...
	 * @author Darki699
	 */
	private class TrapTask implements Runnable
	{
		public void run()
		{
			if (_trapSpawn == null || _trapTask == null || _caster == null || System.currentTimeMillis() > _activityTime || _deleteMe)
			{
				eraseMe();
				return;
			}

			else if (_trapSpawn.getLastSpawn().isVisible() && !_detected)
			{
				_trapSpawn.getLastSpawn().setIsVisible(false);
				_trapSpawn.getLastSpawn().decayMe();
				return;
			}

			// Turns a trap invisible for 3 seconds...
			else if (!_detected)
			{
				SocialAction sa = new SocialAction(_trapSpawn.getLastSpawn().getObjectId(),2);
				_trapSpawn.getLastSpawn().broadcastPacket(sa);
			}

			L2Character myTarget = getClosestTarget(_trapSpawn.getLastSpawn().getX() , _trapSpawn.getLastSpawn().getY() , _trapSpawn.getLastSpawn().getZ());
			if (myTarget != null)
			{
				_trapSpawn.getLastSpawn().setIsVisible(true);
				_trapSpawn.getLastSpawn().spawnMe();
				explode(myTarget);
				_deleteMe = true;
			}
		}
	}

	/**
	 * Receives the Traps X,Y,Z position and returns the closest target to explode upon.
	 * @param x
	 * @param y
	 * @param z
	 * @return L2Character - The target that tripped on this trap, or <b>null</b> if no such target exists
	 */
	private L2Character getClosestTarget(int x , int y , int z)
	{
		L2Character myTarget = null;
		int closest = -1;

		for (L2Character maybeTarget : _trapSpawn.getLastSpawn().getKnownList().getKnownCharacters())
		{
			if (maybeTarget == _caster)
				continue;
			else if (maybeTarget != null)
			{
				if (maybeTarget.isInsideRadius(x, y, z, _radius, true, false) && !maybeTarget.isAlikeDead() && maybeTarget.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE)
				{
					/* 	Calculate the (x,y) vector length between the target and the trap sqrt(x^2 + y^2) */
					int close = (int)Math.sqrt(Math.pow(x-maybeTarget.getX(), 2) + Math.pow(y-maybeTarget.getY(), 2));
					if (close < closest || closest < 0)
					{
						myTarget 	= maybeTarget;
						closest 	= close;
					}
				}
			}
		}
		return myTarget;
	}

	/**
	 * @Override Trap needs to set a target first and send the caster a message that the trap was tripped.
	 * @param target The trap's target.
	 */
	private void doCast(L2Character target)
	{
		_trapSpawn.getLastSpawn().setTarget(target);
		target.sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_TRIPPED));
		((L2TrapInstance)_trapSpawn.getLastSpawn()).setCaster(_caster);
		_trapSpawn.getLastSpawn().doCast(_shotSkill);
	}

	public L2PcInstance getCaster()
	{
		return ((_caster instanceof L2PcInstance) ? ((L2PcInstance)_caster) : null);
	}

	/**
	 * Set the trap summoner
	 * @param caster
	 */
	public void setCaster(L2Character caster)
	{
		_caster = caster;
	}

	/**
	 * if skill landing percent is set, trap may fail.
	 * @return boolean true if trap has failed to function.
	 */
	private boolean trapFailed()
	{
		return (!(Rnd.get(100) < ((_shotSkill.getLandingPercent()>0) ? _shotSkill.getLandingPercent() : 100)));
	}

	/**
	 * Trap Explodes
	 * @param target - L2Character the main damage target of the skill
	 */
	private void explode(L2Character target)
	{
		if (trapFailed())
		{
			_caster.sendPacket(new SystemMessage(SystemMessageId.TRAP_FAILED));
			return;
		}

		_trapSpawn.getLastSpawn().setIsImobilised(false);
		doCast(target);

		// Add hate damage toward the caster from all the trap's targets
		for (L2Object injured : _shotSkill.getTargetList(_trapSpawn.getLastSpawn()))
		{
			if (injured == (L2Object)_caster)
				continue;

			else if (injured != null)
			{
				if (injured instanceof L2Attackable)
				{
					((L2Attackable)injured).addDamage(_caster, 1);
				}
			}

		}

		// Add hate damage to the main target of this explosion
		if (target instanceof L2Attackable)
		{
			((L2Attackable)target).addDamage(_caster, 100);
		}

		if (_caster != null)
		{
			for (L2Effect effect : _caster.getAllEffects())
			{
				if (effect != null && effect.getSkill().getId() == _shotSkill.getId())
				{
					effect.exit();
				}
			}
		}
	}

	/**
	 * Sends out a "trap damage message" to the L2PcInstance that received trap's damage...
	 * @param target receives the system message.
	 */
	public static void sendDamageMessage(L2PcInstance target , L2Skill skill)
	{
		SystemMessage sm = null;

		switch (skill.getSkillType())
		{

		case POISON:
				sm = new SystemMessage(SystemMessageId.POISONED_BY_TRAP);
				break;

		case DEBUFF:
				sm = new SystemMessage(SystemMessageId.SLOWED_BY_TRAP);
				break;

		case MDAM:
				sm = new SystemMessage(SystemMessageId.TRAP_DID_S1_DAMAGE);
				sm.addString("fire");
				break;

		case STUN:
				target.sendMessage("You have been stunned by a Secret Trap."); // Until NCSoft make a system message...
				break;

		case ROOT:
				target.sendMessage("You have been rooted by a Secret Trap."); // Until NCSoft make a system message...
				break;
		}

		if (sm != null)
		{
			target.sendPacket(sm);
		}

	}

	/**
	 * I'm not sure if a trap can damage it's caster....
	 * @param trap
	 * @param targets
	 * @return new targets without the trap summoner
	 */
	public static L2Object[] doNotIncludeCaster(L2TrapInstance trap , L2Object[] targets)
	{
		if (trap == null || targets == null)
			return null;

		for (int x = 0 ; x < targets.length ; x++)
		{
			if (targets[x] == (L2Object)(trap.getCaster()))
				targets[x] = trap;
		}

		return targets;
	}
}
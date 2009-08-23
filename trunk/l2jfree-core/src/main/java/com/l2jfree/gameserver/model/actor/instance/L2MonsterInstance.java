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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.knownlist.MonsterKnownList;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.MinionList;
import com.l2jfree.tools.random.Rnd;

/**
 * This class manages all Monsters.
 * 
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	protected final MinionList		_minionList;

	protected ScheduledFuture<?>	_minionMaintainTask				= null;

	private static final int		MONSTER_MAINTENANCE_INTERVAL	= 1000;

	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_minionList = new MinionList(this);

		// [L2J_JP ADD]
		if (getNpcId() == 29002) // Queen Ant Larva is invulnerable.
		{
			setIsInvul(true);
		}
	}

	@Override
	public final MonsterKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new MonsterKnownList(this);

		return (MonsterKnownList) _knownList;
	}

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;

		return !isEventMob;
	}

	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	@Override
	public boolean isAggressive()
	{
		return (getTemplate().getAggroRange() > 0) && !isEventMob;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if (getTemplate().getMinionData() != null)
		{
			if (getSpawnedMinions() != null)
			{
				for (L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null)
						continue;
					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();

				manageMinions();
			}
		}
	}
	
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval
	 *
	 */
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval());
	}

	public void callMinions()
	{
		if (_minionList.hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				if (minion == null || minion.isDead() || minion.isInCombat() || minion.isMovementDisabled())
					continue;
				
				// Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = getX() + Rnd.get(-200, 200);
					int minionY = getY() + Rnd.get(-200, 200);
					int minionZ = getZ();
					
					// Move the minion to the new coords
					minion.moveToLocation(minionX, minionY, minionZ, 0);
				}
			}
		}
	}

	public void callMinionsToAssist(L2Character attacker)
	{
		if (_minionList.hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				if (minion == null || minion.isDead())
					continue;
				
				// Trigger the aggro condition of the minion
				if (isRaidBoss() && !isRaidMinion())
					minion.addDamage(attacker, 100, null);
				else
					minion.addDamage(attacker, 1, null);
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!isKillable())
			return false;

		if (!super.doDie(killer))
			return false;

		if (_minionMaintainTask != null)
			_minionMaintainTask.cancel(true); // doesn't do it?

		if (isRaidBoss())
			deleteSpawnedMinions();
		return true;
	}

	public List<L2MinionInstance> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}

	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}

	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}

	public void notifyMinionDied(L2MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}

	public void notifyMinionSpawned(L2MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}

	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!(attacker instanceof L2MonsterInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (_minionMaintainTask != null)
				_minionMaintainTask.cancel(true);

			deleteSpawnedMinions();
		}
		super.deleteMe();
	}

	public void deleteSpawnedMinions()
	{
		for (L2MinionInstance minion : getSpawnedMinions())
		{
			if (minion == null)
				continue;
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			getSpawnedMinions().remove(minion);
		}
		_minionList.clearRespawnList();
	}
}

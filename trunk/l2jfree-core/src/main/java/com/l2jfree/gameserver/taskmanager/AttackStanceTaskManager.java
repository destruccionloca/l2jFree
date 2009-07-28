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
package com.l2jfree.gameserver.taskmanager;

import java.util.Map;

import javolution.util.FastMap;

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.AutoAttackStop;

public final class AttackStanceTaskManager extends AbstractPeriodicTaskManager
{
	public static final long COMBAT_TIME = 15000;
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<L2Character, Long> _attackStanceTasks = new FastMap<L2Character, Long>();
	
	private AttackStanceTaskManager()
	{
		super(1000);
	}
	
	public boolean getAttackStanceTask(L2Character actor)
	{
		readLock();
		try
		{
			if (actor instanceof L2Summon)
				actor = ((L2Summon)actor).getOwner();
			
			return _attackStanceTasks.containsKey(actor);
		}
		finally
		{
			readUnlock();
		}
	}
	
	public void addAttackStanceTask(L2Character actor)
	{
		writeLock();
		try
		{
			if (actor instanceof L2Summon)
				actor = ((L2Summon)actor).getOwner();
			
			if (actor instanceof L2PcInstance)
				for (L2CubicInstance cubic : ((L2PcInstance)actor).getCubics().values())
					if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
						cubic.doAction();
			
			_attackStanceTasks.put(actor, System.currentTimeMillis() + COMBAT_TIME);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public void removeAttackStanceTask(L2Character actor)
	{
		writeLock();
		try
		{
			if (actor instanceof L2Summon)
				actor = ((L2Summon)actor).getOwner();
			
			_attackStanceTasks.remove(actor);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	@Override
	public void run()
	{
		writeLock();
		try
		{
			for (Map.Entry<L2Character, Long> entry : _attackStanceTasks.entrySet())
			{
				if (System.currentTimeMillis() > entry.getValue())
				{
					final L2Character actor = entry.getKey();
					
					actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
					
					if (actor instanceof L2PcInstance)
					{
						final L2Summon pet = ((L2PcInstance)actor).getPet();
						if (pet != null)
							pet.broadcastPacket(new AutoAttackStop(pet.getObjectId()));
					}
					
					actor.getAI().setAutoAttacking(false);
					
					_attackStanceTasks.remove(actor);
				}
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}

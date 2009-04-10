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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfree.tools.random.Rnd;

public final class AttackStanceTaskManager implements Runnable
{
	private final static Log _log = LogFactory.getLog(AttackStanceTaskManager.class);
	
	public static final long COMBAT_TIME = 15000;
	
	private static AttackStanceTaskManager _instance;
	
	public static AttackStanceTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new AttackStanceTaskManager();
		
		return _instance;
	}
	
	private final Map<L2Character, Long> _attackStanceTasks = new FastMap<L2Character, Long>();
	
	private AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(1000), Rnd.get(990, 1010));
		
		_log.info("AttackStanceTaskManager: Initialized.");
	}
	
	public synchronized boolean getAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
			actor = ((L2Summon)actor).getOwner();
		
		return _attackStanceTasks.containsKey(actor);
	}
	
	public synchronized void addAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
			actor = ((L2Summon)actor).getOwner();
		
		if (actor instanceof L2PcInstance)
			for (L2CubicInstance cubic : ((L2PcInstance)actor).getCubics().values())
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction();
		
		_attackStanceTasks.put(actor, System.currentTimeMillis() + COMBAT_TIME);
	}
	
	public synchronized void removeAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
			actor = ((L2Summon)actor).getOwner();
		
		_attackStanceTasks.remove(actor);
	}
	
	public synchronized void run()
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
}

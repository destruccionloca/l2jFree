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
package com.l2jfree.gameserver.instancemanager;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Instance;

/**
 * @author evill33t
 * 
 */
public class InstanceManager
{
	private final static Log			_log			= LogFactory.getLog(InstanceManager.class);

	private final FastMap<Integer, Instance>	_instanceList	= new FastMap<Integer, Instance>();
	private final FastMap<Integer, InstanceWorld> _instanceWorlds = new FastMap<Integer, InstanceWorld>();

	private int							_dynamic		= 300000;

	public class InstanceWorld
	{
		public int instanceId;
		public FastList<L2PcInstance> allowed = new FastList<L2PcInstance>();
		public int status;
	}

	public void addWorld(InstanceWorld world)
	{
		_instanceWorlds.put(world.instanceId, world);
	}

	public InstanceWorld getWorld(int instanceId)
	{
		return _instanceWorlds.get(instanceId);
	}

	public InstanceWorld getPlayerWorld(L2PcInstance player)
	{
		for (InstanceWorld temp : _instanceWorlds.values())
		{
			// check if the player have a World Instance where he/she is allowed to enter
			if (temp.allowed.contains(player))
				return temp;
		}
		return null;
	}

	public static final InstanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private InstanceManager()
	{
		_log.info("Initializing InstanceManager");
		createWorld();
	}

	private void createWorld()
	{
		Instance themultiverse = new Instance(-1);
		themultiverse.setName("multiverse");
		_instanceList.put(-1, themultiverse);
		_log.info("Multiverse Instance created");

		Instance universe = new Instance(0);
		universe.setName("universe");
		_instanceList.put(0, universe);
		_log.info("Universe Instance created");
	}

	public void destroyInstance(int instanceid)
	{
		if (instanceid == 0)
			return;
		Instance temp = _instanceList.get(instanceid);
		if (temp != null)
		{
			temp.removeNpcs();
			temp.removePlayers();
			temp.removeDoors();
			temp.cancelTimer();
			_instanceList.remove(instanceid);
			_instanceWorlds.remove(instanceid);
		}
	}

	public Instance getInstance(int instanceid)
	{
		return _instanceList.get(instanceid);
	}
	
	public FastMap<Integer,Instance> getInstances()
	{
		return _instanceList;
	}

	public int getPlayerInstance(int objectId)
	{
		for (Instance temp : _instanceList.values())
		{
			// check if the player is in any active instance
			if (temp.containsPlayer(objectId))
				return temp.getId();
		}
		// 0 is default instance aka the world
		return 0;
	}

	public boolean createInstance(int id)
	{
		if (getInstance(id) != null)
			return false;

		Instance instance = new Instance(id);
		_instanceList.put(id, instance);
		return true;
	}

	public boolean createInstanceFromTemplate(int id, String template)
	{
		if (getInstance(id) != null)
			return false;

		Instance instance = new Instance(id);
		_instanceList.put(id, instance);
		instance.loadInstanceTemplate(template);
		return true;
	}

	public int createDynamicInstance(String template)
	{
		while (getInstance(_dynamic) != null)
		{
			_dynamic++;
			if (_dynamic == Integer.MAX_VALUE)
			{
				_log.warn("InstanceManager: More then " + (Integer.MAX_VALUE - 300000) + " instances created");
				_dynamic = 300000;
			}
		}
		Instance instance = new Instance(_dynamic);
		_instanceList.put(_dynamic, instance);
		if (template != null)
		{
			try
			{
				instance.loadInstanceTemplate(template);
			}
			catch (Exception e)
			{
				_log.warn("InstanceManager: Failed creating instance from template " + template + ", " + e.getMessage(), e);
			}
		}
		return _dynamic;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final InstanceManager _instance = new InstanceManager();
	}
}

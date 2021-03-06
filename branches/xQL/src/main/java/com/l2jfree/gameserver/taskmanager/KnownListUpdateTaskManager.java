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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;


public class KnownListUpdateTaskManager
{
	protected static final Log					_log	= LogFactory.getLog(KnownListUpdateTaskManager.class.getName());

	private static KnownListUpdateTaskManager	_instance;

	public KnownListUpdateTaskManager()
	{
		if (Config.MOVE_BASED_KNOWNLIST)
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, 2500);
		else
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, 750);
	}

	public static KnownListUpdateTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new KnownListUpdateTaskManager();

		return _instance;
	}

	private class KnownListUpdate implements Runnable
	{
		boolean	forgetObjects;
		boolean	fullUpdate	= true;

		protected KnownListUpdate()
		{
			if (Config.MOVE_BASED_KNOWNLIST)
				forgetObjects = true;
			else
				forgetObjects = false;
		}

		public void run()
		{
			try
			{
				for (L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
				{
					for (L2WorldRegion r : regions) // go through all world regions
					{
						if (r.isActive()) // and check only if the region is active
						{
							updateRegion(r, fullUpdate, forgetObjects);
						}
					}
				}
				if (forgetObjects && !Config.MOVE_BASED_KNOWNLIST)
					forgetObjects = false;
				else
					forgetObjects = true;
				if (fullUpdate)
					fullUpdate = false;
			}
			catch (Throwable e)
			{
				_log.warn(e.toString());
			}
		}
	}

	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		for (L2Object object : region.getVisibleObjects()) // and for all members in region
		{
			if (!object.isVisible())
				continue; // skip dying objects
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects(
						(object instanceof L2PlayableInstance || (Config.ALLOW_GUARDS && object instanceof L2GuardInstance) || fullUpdate));
				continue;
			}
			if (object instanceof L2PlayableInstance || (Config.ALLOW_GUARDS && object instanceof L2GuardInstance) || fullUpdate)
			{
				for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					for (L2Object _object : regi.getVisibleObjects())
					{
						if (_object != object)
						{
							object.getKnownList().addKnownObject(_object);
						}
					}
				}
			}
			else if (object instanceof L2Character)
			{
				for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					if (regi.isActive())
						for (L2Object _object : regi.getVisiblePlayable())
						{
							if (_object != object)
							{
								object.getKnownList().addKnownObject(_object);
							}
						}
				}
			}
		}
	}
}

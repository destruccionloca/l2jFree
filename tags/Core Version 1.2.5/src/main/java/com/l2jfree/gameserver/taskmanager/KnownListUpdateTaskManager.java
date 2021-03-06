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

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;

/**
 * It removes accidently leaked objects from knownlists :)
 */
public final class KnownListUpdateTaskManager implements Runnable
{
	private static KnownListUpdateTaskManager _instance;
	
	public static KnownListUpdateTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new KnownListUpdateTaskManager();
		
		return _instance;
	}
	
	public KnownListUpdateTaskManager()
	{
		long delay = 10*60*1000; // 10 min
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, delay, delay);
	}
	
	@Override
	public void run()
	{
		for (L2Object obj : L2World.getInstance().getAllVisibleObjects())
			obj.getKnownList().tryRemoveObjects();
	}
}
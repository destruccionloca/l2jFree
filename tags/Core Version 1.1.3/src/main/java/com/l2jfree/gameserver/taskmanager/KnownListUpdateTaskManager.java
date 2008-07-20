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

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2WorldRegion;

public final class KnownListUpdateTaskManager implements Runnable
{
	private static KnownListUpdateTaskManager _instance;
	
	public static KnownListUpdateTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new KnownListUpdateTaskManager();
		
		return _instance;
	}
	
	private boolean _forgetObjects = Config.MOVE_BASED_KNOWNLIST;
	
	public KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, Config.MOVE_BASED_KNOWNLIST ? 3000 : 1000);
		
		update(true);
	}
	
	@Override
	public void run()
	{
		update(false);
	}
	
	private void update(boolean fullUpdate)
	{
		for (L2WorldRegion[] regions : L2World.getInstance().getAllWorldRegions())
			for (L2WorldRegion r : regions)
				if (r.isActive())
					r.updateRegion(fullUpdate, _forgetObjects);
		
		if (Config.MOVE_BASED_KNOWNLIST)
			_forgetObjects = true;
		else
			_forgetObjects = !_forgetObjects;
	}
}
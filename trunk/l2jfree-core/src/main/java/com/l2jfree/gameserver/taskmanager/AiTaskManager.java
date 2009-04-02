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

import java.util.Set;

import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.concurrent.ExecuteWrapper;

/**
 * @author NB4L1
 */
public final class AiTaskManager implements Runnable
{
	private static final Log _log = LogFactory.getLog(AiTaskManager.class);
	
	private static AiTaskManager _instance;
	
	public static AiTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new AiTaskManager();
		
		return _instance;
	}
	
	private final Set<Runnable> _startList = new FastSet<Runnable>();
	private final Set<Runnable> _stopList = new FastSet<Runnable>();
	
	private final Set<Runnable> _aiTasks = new FastSet<Runnable>();
	
	private AiTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(1000), Rnd.get(990, 1010));
		
		_log.info("AiTaskManager: Initialized.");
	}
	
	public synchronized void startAiTask(Runnable runnable)
	{
		_startList.add(runnable);
		
		_stopList.remove(runnable);
	}
	
	public synchronized void stopAiTask(Runnable runnable)
	{
		_stopList.add(runnable);
		
		_startList.remove(runnable);
	}
	
	public void run()
	{
		synchronized (this)
		{
			_aiTasks.addAll(_startList);
			_aiTasks.removeAll(_stopList);
			
			_startList.clear();
			_stopList.clear();
		}
		
		for (Runnable runnable : _aiTasks)
			ExecuteWrapper.execute(runnable);
	}
}

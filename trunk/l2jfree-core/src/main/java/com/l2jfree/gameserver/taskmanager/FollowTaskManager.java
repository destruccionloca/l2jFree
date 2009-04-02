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
import com.l2jfree.gameserver.ai.AbstractAI;
import com.l2jfree.tools.random.Rnd;

/**
 * @author NB4L1
 */
public final class FollowTaskManager implements Runnable
{
	private static final Log _log = LogFactory.getLog(FollowTaskManager.class);
	
	private static FollowTaskManager _instance;
	
	public static FollowTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new FollowTaskManager();
		
		return _instance;
	}
	
	private final Set<AbstractAI> _startList = new FastSet<AbstractAI>();
	private final Set<AbstractAI> _stopList = new FastSet<AbstractAI>();
	
	private final Set<AbstractAI> _followTasks = new FastSet<AbstractAI>();
	
	private FollowTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, Rnd.get(500), Rnd.get(490, 510));
		
		_log.info("FollowTaskManager: Initialized.");
	}
	
	public synchronized void startFollowTask(AbstractAI followTask)
	{
		_startList.add(followTask);
		
		_stopList.remove(followTask);
	}
	
	public synchronized void stopFollowTask(AbstractAI followTask)
	{
		_stopList.add(followTask);
		
		_startList.remove(followTask);
	}
	
	public void run()
	{
		synchronized (this)
		{
			_followTasks.addAll(_startList);
			_followTasks.removeAll(_stopList);
			
			_startList.clear();
			_stopList.clear();
		}
		
		for (AbstractAI followTask : _followTasks)
			followTask.followTarget();
	}
}

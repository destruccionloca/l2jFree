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

import com.l2jfree.util.concurrent.RunnableStatsManager;

/**
 * @author NB4L1
 */
public abstract class AbstractIterativePeriodicTaskManager<T> extends AbstractPeriodicTaskManager
{
	private final Set<T> _startList = new FastSet<T>();
	private final Set<T> _stopList = new FastSet<T>();
	
	private final Set<T> _activeTasks = new FastSet<T>();
	
	protected AbstractIterativePeriodicTaskManager(int period)
	{
		super(period);
	}
	
	public synchronized boolean hasTask(T task)
	{
		return _activeTasks.contains(task) || _startList.contains(task);
	}
	
	public final synchronized void startTask(T task)
	{
		_startList.add(task);
		
		_stopList.remove(task);
	}
	
	public final synchronized void stopTask(T task)
	{
		_stopList.add(task);
		
		_startList.remove(task);
	}
	
	@Override
	public final void run()
	{
		synchronized (this)
		{
			_activeTasks.addAll(_startList);
			_activeTasks.removeAll(_stopList);
			
			_startList.clear();
			_stopList.clear();
		}
		
		for (T task : _activeTasks)
		{
			final long begin = System.nanoTime();
			
			try
			{
				callTask(task);
			}
			catch (RuntimeException e)
			{
				_log.warn("", e);
			}
			finally
			{
				RunnableStatsManager.getInstance().handleStats(task.getClass(), getCalledMethodName(),
					System.nanoTime() - begin);
			}
		}
	}
	
	protected abstract void callTask(T task);
	
	protected abstract String getCalledMethodName();
}

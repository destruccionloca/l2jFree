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
package com.l2jfree.gameserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.util.concurrent.ExecuteWrapper;
import com.l2jfree.util.concurrent.L2RejectedExecutionHandler;
import com.l2jfree.util.concurrent.RunnableStatsManager;
import com.l2jfree.util.concurrent.ScheduledFutureWrapper;

/**
 * @author -Wooden-, NB4L1
 */
public final class ThreadPoolManager
{
	private static final Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	public static final long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING = 5000;
	
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	private static ThreadPoolManager _instance;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _pool;
	private final ThreadPoolExecutor _longRunningPool;
	
	private ThreadPoolManager()
	{
		_pool = new ScheduledThreadPoolExecutor(Config.THREAD_POOL_SIZE);
		_pool.setRejectedExecutionHandler(new L2RejectedExecutionHandler());
		_pool.prestartAllCoreThreads();
		
		_longRunningPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());
		_longRunningPool.setRejectedExecutionHandler(new L2RejectedExecutionHandler());
		_longRunningPool.prestartAllCoreThreads();
		
		scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				purge();
			}
		}, 600000, 600000);
		
		_log.info("ThreadPoolManager: Initialized with " + Config.THREAD_POOL_SIZE + " threads.");
	}
	
	private final long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	private static final class ThreadPoolExecuteWrapper extends ExecuteWrapper
	{
		private ThreadPoolExecuteWrapper(Runnable runnable)
		{
			super(runnable);
		}
		
		@Override
		protected long getMaximumRuntimeInMillisecWithoutWarning()
		{
			return MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING;
		}
	}
	
	// ===========================================================================================
	
	public final ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return new ScheduledFutureWrapper(_pool.schedule(
			new ThreadPoolExecuteWrapper(r), validate(delay), TimeUnit.MILLISECONDS));
	}
	
	public final ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	public final ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	public final ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	// ===========================================================================================
	
	public final ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		return new ScheduledFutureWrapper(_pool.scheduleAtFixedRate(
			new ThreadPoolExecuteWrapper(r), validate(delay), validate(period), TimeUnit.MILLISECONDS));
	}
	
	public final ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	public final ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	public final ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	// ===========================================================================================
	
	public final void execute(Runnable r)
	{
		_pool.execute(new ThreadPoolExecuteWrapper(r));
	}
	
	public final void executeTask(Runnable r)
	{
		execute(r);
	}
	
	public final void executeLongRunning(Runnable r)
	{
		_longRunningPool.execute(new ExecuteWrapper(r));
	}
	
	// ===========================================================================================
	
	public final Future<?> submit(Runnable r)
	{
		return _pool.submit(new ThreadPoolExecuteWrapper(r));
	}
	
	public final Future<?> submitLongRunning(Runnable r)
	{
		return _longRunningPool.submit(new ExecuteWrapper(r));
	}
	
	// ===========================================================================================
	
	public List<String> getStats()
	{
		List<String> list = new ArrayList<String>(25);
		
		list.add("");
		list.add("Normal scheduler:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + _pool.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + _pool.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + _pool.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + _pool.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + _pool.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + _pool.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + _pool.getQueue().size());
		list.add("\tgetTaskCount: ........ " + _pool.getTaskCount());
		list.add("");
		list.add("Long-running executor:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + _longRunningPool.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + _longRunningPool.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + _longRunningPool.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + _longRunningPool.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + _longRunningPool.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + _longRunningPool.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + _longRunningPool.getQueue().size());
		list.add("\tgetTaskCount: ........ " + _longRunningPool.getTaskCount());
		list.add("");
		
		return list;
	}
	
	private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException
	{
		final long begin = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - begin < timeoutInMillisec)
		{
			if (!_pool.awaitTermination(1, TimeUnit.MILLISECONDS))
				continue;
			
			if (!_longRunningPool.awaitTermination(1, TimeUnit.MILLISECONDS))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	public void shutdown()
	{
		final long begin = System.currentTimeMillis();
		
		System.out.println("ThreadPoolManager: Shutting down.");
		System.out.println("\t... executing "
			+ (_pool.getQueue().size() + _pool.getActiveCount()) + " tasks.");
		System.out.println("\t... executing "
			+ (_longRunningPool.getQueue().size() + _longRunningPool.getActiveCount()) + " long running tasks.");
		
		_pool.shutdown();
		_longRunningPool.shutdown();
		
		boolean success = false;
		try
		{
			success |= awaitTermination(5000);
			
			if (!success)
			{
				_pool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
				_pool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				
				success |= awaitTermination(10000);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
		System.out.println("\t... " +
			(_pool.getQueue().size() + _pool.getActiveCount()) + " tasks left.");
		System.out.println("\t... " +
			(_longRunningPool.getQueue().size() + _longRunningPool.getActiveCount()) + " long running tasks left.");
		
		if (TimeUnit.HOURS.toMillis(12) < (System.currentTimeMillis() - GameServer.getStartedTime().getTimeInMillis()))
			RunnableStatsManager.getInstance().dumpClassStats();
	}
	
	public void purge()
	{
		_pool.purge();
		_longRunningPool.purge();
	}
}

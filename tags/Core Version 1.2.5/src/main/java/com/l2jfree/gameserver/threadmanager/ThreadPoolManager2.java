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
package com.l2jfree.gameserver.threadmanager;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * @author NB4L1
 */
public final class ThreadPoolManager2 extends ThreadPoolManager
{
	private static ThreadPoolManager2 _instance;
	
	public static ThreadPoolManager2 getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager2();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _scheduler =
		new ScheduledThreadPoolExecutor(1, new L2ThreadFactory("Scheduler", Thread.MAX_PRIORITY),
			new L2RejectedExecutionHandler());
	
	private final ThreadPoolExecutor _executor =
		new ThreadPoolExecutor(Config.THREAD_POOL_SIZE, Config.THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new L2ThreadFactory("Executor", Thread.NORM_PRIORITY),
			new L2RejectedExecutionHandler());
	
	private ThreadPoolManager2()
	{
		_log.info("ThreadPoolManager v2: Initialized with " + Config.THREAD_POOL_SIZE + " threads...");
	}
	
	// ===========================================================================================
	
	@Override
	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return new ScheduledFutureWrapper(_scheduler.schedule(new ScheduleWrapper(r), validate(delay),
			TimeUnit.MILLISECONDS));
	}
	
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		return new ScheduledFutureWrapper(_scheduler.scheduleAtFixedRate(new ScheduleWrapper(r), validate(delay),
			validate(period), TimeUnit.MILLISECONDS));
	}
	
	@Override
	public void execute(Runnable r)
	{
		_executor.execute(new ExecuteWrapper(r));
	}
	
	// ===========================================================================================
	
	@Override
	public List<String> getStats()
	{
		List<String> list = FastList.newInstance();
		
		list.add("ThreadPoolExecutor");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + _executor.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + _executor.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + _executor.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + _executor.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + _executor.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + _executor.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + _executor.getQueue().size());
		list.add("\tgetTaskCount: ........ " + _executor.getTaskCount());
		list.add("");
		list.add("ScheduledThreadPoolExecutor");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + _scheduler.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + _scheduler.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + _scheduler.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + _scheduler.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + _scheduler.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + _scheduler.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + _scheduler.getQueue().size());
		list.add("\tgetTaskCount: ........ " + _scheduler.getTaskCount());
		list.add("");
		
		return list;
	}
	
	@Override
	public void shutdown()
	{
		System.out.println("ThreadPoolManager: Shutting down!");
		
		System.out.println("\tExecuting " + _scheduler.getQueue().size() + " scheduled tasks!");
		System.out.println("\tExecuting " + _executor.getQueue().size() + " instant tasks!");
		
		_scheduler.shutdown();
		_executor.shutdown();
		
		System.out.println("ThreadPoolManager: Results!");
		
		try
		{
			Thread.sleep(5000);
			
			System.out.println("\tScheduler: " + _scheduler.awaitTermination(0, TimeUnit.SECONDS));
			System.out.println("\tExecutor:  " + _executor.awaitTermination(0, TimeUnit.SECONDS));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("\t" + _scheduler.getQueue().size() + " scheduled tasks left.");
		System.out.println("\t" + _executor.getQueue().size() + " instant tasks left.");
	}
	
	@Override
	public void purge()
	{
		_scheduler.purge();
		_executor.purge();
	}
}
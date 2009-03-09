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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * @author NB4L1
 */
public final class ThreadPoolManager1 extends ThreadPoolManager
{
	private static ThreadPoolManager1 _instance;
	
	public static ThreadPoolManager1 getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager1();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _pool =
		new ScheduledThreadPoolExecutor(Config.THREAD_POOL_SIZE,
			new L2ThreadFactory("ThreadPool", Thread.NORM_PRIORITY), new L2RejectedExecutionHandler());
	
	private ThreadPoolManager1()
	{
		_log.info("ThreadPoolManager v1: Initialized with " + Config.THREAD_POOL_SIZE + " threads...");
	}
	
	// ===========================================================================================
	
	@Override
	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return new ScheduledFutureWrapper(_pool.schedule(new ExecuteWrapper(r), validate(delay), TimeUnit.MILLISECONDS));
	}
	
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		return new ScheduledFutureWrapper(_pool.scheduleAtFixedRate(new ExecuteWrapper(r), validate(delay),
			validate(period), TimeUnit.MILLISECONDS));
	}
	
	@Override
	public void execute(Runnable r)
	{
		_pool.execute(new ExecuteWrapper(r));
	}
	
	// ===========================================================================================
	
	@Override
	public List<String> getStats()
	{
		List<String> list = FastList.newInstance();
		
		list.add("ThreadPool");
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
		
		return list;
	}
	
	@Override
	public void shutdown()
	{
		System.out.println("ThreadPoolManager: Shutting down!");
		
		System.out.println("\tExecuting " + _pool.getQueue().size() + " tasks!");
		
		_pool.shutdown();
		
		System.out.println("ThreadPoolManager: Results!");
		
		try
		{
			System.out.println("\tPool: " + _pool.awaitTermination(5, TimeUnit.SECONDS));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("\t" + _pool.getQueue().size() + " tasks left.");
	}
	
	@Override
	public void purge()
	{
		_pool.purge();
	}
}
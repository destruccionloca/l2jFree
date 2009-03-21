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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.Config;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.util.concurrent.ExecuteWrapper;
import com.l2jfree.util.concurrent.L2RejectedExecutionHandler;
import com.l2jfree.util.concurrent.ScheduledFutureWrapper;

/**
 * @author -Wooden-, NB4L1
 */
public final class ThreadPoolManager
{
	private static final Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	private static ThreadPoolManager _instance;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _pool;
	
	private ThreadPoolManager()
	{
		_pool = new ScheduledThreadPoolExecutor(Config.THREAD_POOL_SIZE);
		_pool.setRejectedExecutionHandler(new L2RejectedExecutionHandler());
		_pool.prestartAllCoreThreads();
		
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
	
	// ===========================================================================================
	
	public final ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return new ScheduledFutureWrapper(_pool.schedule(
			new ExecuteWrapper(r), validate(delay), TimeUnit.MILLISECONDS));
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
			new ExecuteWrapper(r), validate(delay), validate(period), TimeUnit.MILLISECONDS));
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
		_pool.execute(new ExecuteWrapper(r));
	}
	
	public final void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		execute(pkt);
	}
	
	public final void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
	{
		execute(pkt);
	}
	
	public final void executeTask(Runnable r)
	{
		execute(r);
	}
	
	public final void executeAi(Runnable r)
	{
		execute(r);
	}
	
	// ===========================================================================================
	
	public List<String> getStats()
	{
		List<String> list = new ArrayList<String>(11);
		
		list.add("ThreadPool:");
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
	
	public void shutdown()
	{
		System.out.println("ThreadPoolManager: Shutting down.");
		System.out.println("\t... executing " + (_pool.getQueue().size() + _pool.getActiveCount()) + " tasks.");
		
		_pool.shutdown();
		
		boolean success = false;
		try
		{
			success = _pool.awaitTermination(3, TimeUnit.SECONDS);
			
			if (!success)
			{
				_pool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
				_pool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				
				success = _pool.awaitTermination(3, TimeUnit.SECONDS);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("ThreadPoolManager: Done.");
		System.out.println("\t... success: " + success);
		System.out.println("\t... " + (_pool.getQueue().size() + _pool.getActiveCount()) + " tasks left.");
	}
	
	public void purge()
	{
		_pool.purge();
	}
}

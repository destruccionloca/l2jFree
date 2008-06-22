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

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.Config;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.threadmanager.L2RejectedExecutionHandler;
import com.l2jfree.gameserver.threadmanager.L2ThreadFactory;

/**
 * @author -Wooden-, NB4L1
 */
public final class ThreadPoolManager
{
	private final static Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	private static ThreadPoolManager _instance;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _scheduler =
		new ScheduledThreadPoolExecutor(1, new L2ThreadFactory("Scheduler", Thread.MAX_PRIORITY),
			new L2RejectedExecutionHandler());
	
	private final ThreadPoolExecutor _executor =
		new ThreadPoolExecutor(Config.THREAD_POOL_SIZE, Config.THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new L2ThreadFactory("Executor", Thread.NORM_PRIORITY),
			new L2RejectedExecutionHandler());
	
	private ThreadPoolManager()
	{
		_log.info("ThreadPoolManager: Initialized with " + Config.THREAD_POOL_SIZE + " threads...");
		
		scheduleGeneralAtFixedRate(new Runnable() {
			public void run()
			{
				purge();
			}
		}, 3600000, 3600000);
	}
	
	private long validateDelay(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		return _scheduler.schedule(new ScheduleWrapper(r), validateDelay(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long delay, long period)
	{
		return _scheduler.scheduleAtFixedRate(new ScheduleWrapper(r), validateDelay(delay), validateDelay(period),
			TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		return _scheduler.schedule(new ScheduleWrapper(r), validateDelay(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long delay, long period)
	{
		return _scheduler.scheduleAtFixedRate(new ScheduleWrapper(r), validateDelay(delay), validateDelay(period),
			TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		return _scheduler.schedule(new ScheduleWrapper(r), validateDelay(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long delay, long period)
	{
		return _scheduler.scheduleAtFixedRate(new ScheduleWrapper(r), validateDelay(delay), validateDelay(period),
			TimeUnit.MILLISECONDS);
	}
	
	public void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		_executor.execute(new ExecuteWrapper(pkt));
	}
	
	public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
	{
		_executor.execute(new ExecuteWrapper(pkt));
	}
	
	public void executeTask(Runnable r)
	{
		_executor.execute(new ExecuteWrapper(r));
	}
	
	public void executeAi(Runnable r)
	{
		_executor.execute(new ExecuteWrapper(r));
	}
	
	private class ExecuteWrapper implements Runnable
	{
		private final Runnable _runnable;
		
		private ExecuteWrapper(Runnable runnable)
		{
			_runnable = runnable;
		}
		
		public void run()
		{
			try
			{
				_runnable.run();
			}
			catch (Exception e)
			{
				_log.warn("Exception in a Runnable execution:", e);
			}
		}
	}
	
	private class ScheduleWrapper implements Runnable
	{
		private final Runnable _runnable;
		
		private ScheduleWrapper(Runnable runnable)
		{
			_runnable = runnable;
		}
		
		public void run()
		{
			_executor.execute(_runnable);
		}
	}
	
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
		list.add("\t");
		
		return list;
	}
	
	public void shutdown()
	{
		System.out.println("ThreadPoolManager: Shutting down!");
		
		System.out.println("\tExecuting " + _scheduler.getQueue().size() + " scheduled tasks!");
		System.out.println("\tExecuting " + _executor.getQueue().size() + " instant tasks!");
		
		_scheduler.shutdown();
		_executor.shutdown();
		
		try
		{
			Thread.sleep(5000);
			
			System.out.println("ThreadPoolManager: Results!");
			
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
	
	public void purge()
	{
		_scheduler.purge();
		_executor.purge();
	}
}
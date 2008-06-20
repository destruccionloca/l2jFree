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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javolution.text.TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.threadmanager.L2RejectedExecutionHandler;
import com.l2jfree.gameserver.threadmanager.L2ThreadFactory;
import com.l2jfree.gameserver.threadmanager.RunnableStatsManager;

/**
 * <p>
 * This class is made to handle all the ThreadPools used in L2j.
 * </p>
 * <p>
 * Scheduled Tasks can either be sent to a
 * {@link #_generalScheduledThreadPool "general"} or
 * {@link #_effectsScheduledThreadPool "effects"}
 * {@link ScheduledThreadPoolExecutor ScheduledThreadPool}: The "effects" one
 * is used for every effects (skills, hp/mp regen ...) while the "general" one
 * is used for everything else that needs to be scheduled.<br>
 * There also is an {@link #_aiScheduledThreadPool "ai"}
 * {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.
 * </p>
 * <p>
 * Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool}
 * either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs
 * to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that
 * needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be
 * executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long)(Runnable, long)} :
 * for effects Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)(Runnable, long)} :
 * for scheduled Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long)(Runnable, long)} :
 * for AI Tasks that needs to be executed periodicaly</li>
 * </ul>
 * </p>
 * <p>
 * For all Tasks that should be executed with no delay asynchronously in a
 * ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools} that
 * can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #_generalPacketsThreadPool GeneralPackets} where most packets
 * handler are executed.</li>
 * <li>{@link #_ioPacketsThreadPool I/O Packets} where all the i/o packets are
 * executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run
 * asynchronously with no delay should be executed ({@link com.l2jfree.gameserver.model.actor.knownlist KnownList}
 * updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p>
 * 
 * @author -Wooden-
 */
public class ThreadPoolManager
{
	private final static Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	private static ThreadPoolManager _instance;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
			_instance = new ThreadPoolManager();
		
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _scheduler =
		new ScheduledThreadPoolExecutor(50, new L2ThreadFactory("Scheduler", Thread.NORM_PRIORITY),
			new L2RejectedExecutionHandler());
	
	private final ThreadPoolExecutor _executor =
		new ThreadPoolExecutor(10, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(100), new L2ThreadFactory("Executor",
				Thread.NORM_PRIORITY), new L2RejectedExecutionHandler());
	
	/** temp workaround for VM issue */
	private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;
	
	private boolean _shutdown;
	
	private ThreadPoolManager()
	{
		scheduleAtFixedRate(new Runnable() {
			public void run()
			{
				ThreadPoolManager.this.purge();
			}
		}, 3600000, 3600000);
	}
	
	private long validateDelay(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	private ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return _scheduler.schedule(new Runner(r), validateDelay(delay), TimeUnit.MILLISECONDS);
	}
	
	private ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		return _scheduler.scheduleAtFixedRate(new Runner(r), validateDelay(delay),
			validateDelay(period), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		return schedule(r, delay);
	}
	
	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(r, delay, period);
	}
	
	private void execute(Runnable r)
	{
		_executor.execute(r);
	}
	
	public void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		execute(pkt);
	}
	
	public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
	{
		execute(pkt);
	}
	
	public void executeTask(Runnable r)
	{
		execute(r);
	}
	
	public void executeAi(Runnable r)
	{
		execute(r);
	}
	
	private class Runner implements Runnable
	{
		private final Runnable _r;
		
		private Runner(Runnable r)
		{
			_r = r;
		}
		
		public void run()
		{
			long begin = System.nanoTime();
			
			try
			{
				_r.run();
			}
			catch (Exception e)
			{
				_log.warn("Exception at a Future<?> task:", e);
			}
			finally
			{
				RunnableStatsManager.getInstance().handleStats(_r.getClass(),
					System.nanoTime() - begin);
			}
		}
	}
	
	public String[] getStats()
	{
		return new String[] {
			" + Scheduler:",
			" |- ActiveThreads:   " + _scheduler.getActiveCount(),
			" |- getCorePoolSize: " + _scheduler.getCorePoolSize(),
			" |- PoolSize:        " + _scheduler.getPoolSize(),
			" |- MaximumPoolSize: " + _scheduler.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _scheduler.getCompletedTaskCount(),
			" |- ScheduledTasks:  "
				+ (_scheduler.getTaskCount() - _scheduler.getCompletedTaskCount()), " | -------",
			" + Executor:", " |- ActiveThreads:   " + _executor.getActiveCount(),
			" |- getCorePoolSize: " + _executor.getCorePoolSize(),
			" |- MaximumPoolSize: " + _executor.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _executor.getLargestPoolSize(),
			" |- PoolSize:        " + _executor.getPoolSize(),
			" |- CompletedTasks:  " + _executor.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _executor.getQueue().size(), " | -------", };
	}
	
	public void shutdown()
	{
		_shutdown = true;
		try
		{
			System.out.println("ThreadPoolManager: Shutdown...");
			
			_scheduler.awaitTermination(1, TimeUnit.SECONDS);
			_executor.awaitTermination(1, TimeUnit.SECONDS);
			
			RunnableStatsManager.getInstance().dumpClassStats();
			
			_scheduler.shutdown();
			_executor.shutdown();
			
			System.out.println("ThreadPoolManager: ThreadPools stopped!");
		}
		catch (InterruptedException e)
		{
			_log.fatal("", e);
		}
	}
	
	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	public void purge()
	{
		_scheduler.purge();
		
		_executor.purge();
	}
	
	public String getExecutorStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _executor.getThreadFactory();
		if (tf instanceof L2ThreadFactory)
		{
			tb.append("General Thread Pool:\r\n");
			tb.append("Tasks in the queue: " + _executor.getQueue().size() + "\r\n");
			tb.append("Showing threads stack trace:\r\n");
			L2ThreadFactory l2tf = (L2ThreadFactory)tf;
			int count = l2tf.getThreadGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			l2tf.getThreadGroup().enumerate(threads);
			tb.append("There should be " + count + " Threads\r\n");
			for (Thread t : threads)
			{
				if (t == null)
					continue;
				tb.append(t.getName() + "\r\n");
				for (StackTraceElement ste : t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
}
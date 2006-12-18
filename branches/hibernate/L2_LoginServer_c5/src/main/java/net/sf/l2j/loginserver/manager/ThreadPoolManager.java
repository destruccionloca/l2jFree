/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.loginserver.manager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2j.Config;

/**
 * <p>This class is made to handle all the ThreadPools used in L2j.</p>
 * <p>Scheduled Tasks can either be sent to a {@link #_generalScheduledThreadPool "general"} or {@link #_effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}:
 * The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used for
 * everything else that needs to be scheduled.<br>
 * There also is an {@link #_aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.</p>
 * <p>Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)(Runnable, long)} : for scheduled Tasks that needs to be executed periodicaly.</li>
 * </ul></p>
 * 
 * <p>For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools}
 * that can grow/shrink according to their load.:
 * <ul>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link net.sf.l2j.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p> 
 * @author -Wooden-
 *
 */
public class ThreadPoolManager
{
	private static ThreadPoolManager _instance;
	
	private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
	
	private ThreadPoolExecutor _generalThreadPool;
	
	public static ThreadPoolManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new ThreadPoolManager();
		}
		return _instance;
	}
	
	private ThreadPoolManager()
	{
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GerenalSTPool", Thread.NORM_PRIORITY));
		
        _generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE+2,
		                                                   5L, TimeUnit.SECONDS,
		                                                   new LinkedBlockingQueue<Runnable>(),
		                                                   new PriorityThreadFactory("General Pool",Thread.NORM_PRIORITY));
		
		// will be really used in the next AI implementation.
		/*_aiThreadPool = new ThreadPoolExecutor(1, Config.AI_MAX_THREAD,
			                                      10L, TimeUnit.SECONDS,
			                                      new LinkedBlockingQueue<Runnable>());*/
	}
	
	public ScheduledFuture scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public void executeTask(Runnable r)
	{
		_generalThreadPool.execute(r);
	}
	
	public String[] getStats()
	{
		return new String[] {
		                     "STP:",
		                     " + General:",
		                     " |- ActiveThreads:   "+_generalScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_generalScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_generalScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_generalScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()),
		                     "TP:",
		                     " + General Tasks:",
		                     " |- ActiveThreads:   "+_generalThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_generalThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_generalThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_generalThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_generalThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_generalThreadPool.getTaskCount() - _generalThreadPool.getCompletedTaskCount()),
		};
	}
	
    private class PriorityThreadFactory implements ThreadFactory
    {
    	private int _prio;
		private String _name;
		private AtomicInteger _threadNumber = new AtomicInteger(1);
    	
		public PriorityThreadFactory(String name, int prio)
    	{
    		_prio = prio;
    		_name = name;
    	}
		/* (non-Javadoc)
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(r);
			t.setName(_name+"-"+_threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
    	
    }

	/**
	 * 
	 */
	public void shutdown()
	{
		try
		{
			_generalScheduledThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalScheduledThreadPool.shutdown();
			_generalThreadPool.shutdown();
			System.out.println("All ThreadPools are now stoped");
			
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void purge()
	{
		_generalScheduledThreadPool.purge();
		_generalThreadPool.purge();
	}
}

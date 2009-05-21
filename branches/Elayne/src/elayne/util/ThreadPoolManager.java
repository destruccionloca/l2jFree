/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package elayne.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.text.TextBuilder;

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
 * asynchronously with no delay should be executed ({@link net.sf.l2j.gameserver.model.actor.knownlist KnownList}
 * updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p>
 * @author -Wooden-
 */
public class ThreadPoolManager
{
	private class PriorityThreadFactory implements ThreadFactory
	{
		private ThreadGroup _group;
		private String _name;
		private int _prio;
		private AtomicInteger threadNumber = new AtomicInteger(1);

		public PriorityThreadFactory(String name, int prio)
		{
			_prio = prio;
			_name = name;
			_group = new ThreadGroup(_name);
		}

		public ThreadGroup getGroup()
		{
			return _group;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(_group, r);
			t.setName(_name + "-" + threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
	}

	private static ThreadPoolManager _instance;

	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ThreadPoolManager();
		}
		return _instance;
	}

	private ScheduledThreadPoolExecutor _generalScheduledThreadPool;

	private ThreadPoolExecutor _generalThreadPool;

	private boolean _shutdown;

	private ThreadPoolManager()
	{
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(15, new PriorityThreadFactory("GerenalSTPool", Thread.NORM_PRIORITY));
		_generalThreadPool = new ThreadPoolExecutor(4, 6, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("General Pool", Thread.NORM_PRIORITY));
	}

	public void executeTask(Runnable r)
	{
		_generalThreadPool.execute(r);
	}

	public String getGeneralStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _generalThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("General Thread Pool:\r\n");
			tb.append("Tasks in the queue: " + _generalThreadPool.getQueue().size() + "\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
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

	public boolean isShutdown()
	{
		return _shutdown;
	}

	/**
	 * 
	 */
	public void purge()
	{
		_generalScheduledThreadPool.purge();
		_generalThreadPool.purge();
	}

	@SuppressWarnings("unchecked")
	public ScheduledFuture scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			if (delay < 0)
				delay = 0;
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	@SuppressWarnings("unchecked")
	public ScheduledFuture scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0)
				delay = 0;
			if (initial < 0)
				initial = 0;
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	/**
	 * 
	 */
	public void shutdown()
	{
		_shutdown = true;
		try
		{
			_generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalScheduledThreadPool.shutdown();
			_generalThreadPool.shutdown();
			System.out.println("All ThreadPools are now stoped");

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

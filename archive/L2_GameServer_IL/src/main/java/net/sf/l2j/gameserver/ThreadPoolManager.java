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
package net.sf.l2j.gameserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jserver.mmocore.network.ReceivablePacket;

/**
 * <p>This class is made to handle all the ThreadPools used in L2j.</p>
 * <p>Scheduled Tasks can either be sent to a {@link #_generalScheduledThreadPool "general"} or {@link #_effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}:
 * The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used for
 * everything else that needs to be scheduled.<br>
 * There also is an {@link #_aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.</p>
 * <p>Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long)(Runnable, long)} : for effects Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)(Runnable, long)} : for scheduled Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long)(Runnable, long)} : for AI Tasks that needs to be executed periodicaly</li>
 * </ul></p>
 * 
 * <p>For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools}
 * that can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #_generalPacketsThreadPool GeneralPackets} where most packets handler are executed.</li>
 * <li>{@link #_ioPacketsThreadPool I/O Packets} where all the i/o packets are executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link net.sf.l2j.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p> 
 * @author -Wooden-
 *
 */
public class ThreadPoolManager implements ThreadPoolManagerMBean
{
	private static ThreadPoolManager _instance;
    
    private final static Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	private ScheduledThreadPoolExecutor _effectsScheduledThreadPool;
	private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
	
	private ThreadPoolExecutor _generalPacketsThreadPool;
	private ThreadPoolExecutor _ioPacketsThreadPool;
	// will be really used in the next AI implementation.
	private ThreadPoolExecutor _aiThreadPool;
	private ThreadPoolExecutor _generalThreadPool;
	
	// temp
	private ScheduledThreadPoolExecutor _aiScheduledThreadPool;
	
	private boolean _shutdown;
	
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
		_log.info("ThreadPoolManager: io:"+Config.IO_PACKET_THREAD_CORE_SIZE+" generalPackets:"+Config.GENERAL_PACKET_THREAD_CORE_SIZE+" general:"+Config.GENERAL_THREAD_CORE_SIZE+" ai:"+Config.AI_MAX_THREAD);
		
		_effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", Thread.NORM_PRIORITY));
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GerenalSTPool", Thread.NORM_PRIORITY));
		
		_ioPacketsThreadPool = new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE,
		                                                  5L, TimeUnit.SECONDS,
		                                                  new LinkedBlockingQueue<Runnable>(),
		                                                  new PriorityThreadFactory("I/O Packet Pool",Thread.NORM_PRIORITY+1));
		
        _generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE+2,
		                                                   15L, TimeUnit.SECONDS,
		                                                   new LinkedBlockingQueue<Runnable>(),
		                                                   new PriorityThreadFactory("Normal Packet Pool",Thread.NORM_PRIORITY+1));
		
        _generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE+2,
		                                                   5L, TimeUnit.SECONDS,
		                                                   new LinkedBlockingQueue<Runnable>(),
		                                                   new PriorityThreadFactory("General Pool",Thread.NORM_PRIORITY));
		
		// will be really used in the next AI implementation.
		_aiThreadPool = new ThreadPoolExecutor(1, Config.AI_MAX_THREAD,
			                                      10L, TimeUnit.SECONDS,
			                                      new LinkedBlockingQueue<Runnable>());
		
		_aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD, new PriorityThreadFactory("AISTPool", Thread.NORM_PRIORITY));
	}
	
	public ScheduledFuture scheduleEffect(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _effectsScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleEffectAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _effectsScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
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
	
	public ScheduledFuture scheduleAi(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _aiScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleAiAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _aiScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		_generalPacketsThreadPool.execute(pkt);
	}
	
    public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
    {
    	_ioPacketsThreadPool.execute(pkt);
    }
    
	public void executeTask(Runnable r)
	{
		_generalThreadPool.execute(r);
	}
    
    public void executeAi(Runnable r)
    {
        _aiThreadPool.execute(r);
    }    
	
	public String[] getStats()
	{
		return new String[] {
		                     "STP:",
		                     " + Effects:",
		                     " |- ActiveThreads:   "+_effectsScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_effectsScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_effectsScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_effectsScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_effectsScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_effectsScheduledThreadPool.getTaskCount() - _effectsScheduledThreadPool.getCompletedTaskCount()),
		                     " | -------",
		                     " + General:",
		                     " |- ActiveThreads:   "+_generalScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_generalScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_generalScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_generalScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()),
		                     " | -------",
		                     " + AI:",
		                     " |- ActiveThreads:   "+_aiScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_aiScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_aiScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_aiScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_aiScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_aiScheduledThreadPool.getTaskCount() - _aiScheduledThreadPool.getCompletedTaskCount()),
		                     "TP:",
		                     " + Packets:",
		                     " |- ActiveThreads:   "+_generalPacketsThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalPacketsThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_generalPacketsThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_generalPacketsThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_generalPacketsThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_generalPacketsThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_generalPacketsThreadPool.getQueue().size(),
		                     " | -------",
		                     " + I/O Packets:",
		                     " |- ActiveThreads:   "+_ioPacketsThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_ioPacketsThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_ioPacketsThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_ioPacketsThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_ioPacketsThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_ioPacketsThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_ioPacketsThreadPool.getQueue().size(),
		                     " | -------",
		                     " + General Tasks:",
		                     " |- ActiveThreads:   "+_generalThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_generalThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_generalThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_generalThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_generalThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_generalThreadPool.getQueue().size(),
		                     " | -------",
		                     " + AI:",
		                     " |- Not Done"
		};
	}
	
    private class PriorityThreadFactory implements ThreadFactory
    {
    	private int _prio;
		private String _name;
		private AtomicInteger _threadNumber = new AtomicInteger(1);
		private ThreadGroup _group;
    	
		public PriorityThreadFactory(String name, int prio)
    	{
    		_prio = prio;
    		_name = name;
    		_group = new ThreadGroup(_name);    		
    	}
		/* (non-Javadoc)
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(_group,r);
			t.setName(_name+"-"+_threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
    	
		public ThreadGroup getGroup()
		{
			return _group;
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
			_effectsScheduledThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalScheduledThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalPacketsThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_ioPacketsThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_aiThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_effectsScheduledThreadPool.shutdown();
			_generalScheduledThreadPool.shutdown();
			_generalPacketsThreadPool.shutdown();
			_ioPacketsThreadPool.shutdown();
			_generalThreadPool.shutdown();
			_aiThreadPool.shutdown();
			_log.info("All ThreadPools are now stoped");
			
		}
		catch (InterruptedException e)
		{
            _log.error(e);
		}
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
		_effectsScheduledThreadPool.purge();
		_generalScheduledThreadPool.purge();
		_aiScheduledThreadPool.purge();
		_ioPacketsThreadPool.purge();
		_generalPacketsThreadPool.purge();
		_generalThreadPool.purge();
		_aiThreadPool.purge();
	}

	/**
	 * 
	 */
	public String getPacketStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("General Packet Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_generalPacketsThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
	
	public String getIOPacketStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _ioPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("I/O Packet Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_ioPacketsThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
	
	public String getGeneralStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _generalThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("General Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_generalThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsNbActiveThreads()
     */
    public int getEffectsNbActiveThreads()
    {
        return _effectsScheduledThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsCorePoolSize()
     */
    public int getEffectsCorePoolSize()
    {
        return _effectsScheduledThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsPoolSize()
     */
    public int getEffectsPoolSize()
    {
        return _effectsScheduledThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsMaximumPoolSize()
     */
    public int getEffectsMaximumPoolSize()
    {
        return _effectsScheduledThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsCompletedTasks()
     */
    public long getEffectsCompletedTasks()
    {
        return _effectsScheduledThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getEffectsScheduledTasks()
     */
    public long getEffectsScheduledTasks()
    {
        return (_effectsScheduledThreadPool.getTaskCount() - _effectsScheduledThreadPool.getCompletedTaskCount());
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledActiveThreads()
     */
    public int getGeneralScheduledActiveThreads()
    {
        return _generalScheduledThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledCorePoolSize()
     */
    public int getGeneralScheduledCorePoolSize()
    {
        return _generalScheduledThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledPoolSize()
     */
    public int getGeneralScheduledPoolSize()
    {
        return _generalScheduledThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledMaximumPoolSize()
     */
    public int getGeneralScheduledMaximumPoolSize()
    {
        return _generalScheduledThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledCompletedTasks()
     */
    public long getGeneralScheduledCompletedTasks()
    {
        return _generalScheduledThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralScheduledScheduledTasks()
     */
    public long getGeneralScheduledScheduledTasks()
    {
        return (_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount());
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAIActiveThreads()
     */
    public int getAIActiveThreads()
    {
        return _aiScheduledThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAICorePoolSize()
     */
    public int getAICorePoolSize()
    {
        return _aiScheduledThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAIPoolSize()
     */
    public int getAIPoolSize()
    {
        return _aiScheduledThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAIMaximumPoolSize()
     */
    public int getAIMaximumPoolSize()
    {
        return _aiScheduledThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAICompletedTasks()
     */
    public long getAICompletedTasks()
    {
        return _aiScheduledThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getAIScheduledTasks()
     */
    public long getAIScheduledTasks()
    {
        return (_aiScheduledThreadPool.getTaskCount() - _aiScheduledThreadPool.getCompletedTaskCount());
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsActiveThreads()
     */
    public int getPacketsActiveThreads()
    {
        return _generalPacketsThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsCorePoolSize()
     */
    public int getPacketsCorePoolSize()
    {
        return _generalPacketsThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsMaximumPoolSize()
     */
    public int getPacketsMaximumPoolSize()
    {
        return _generalPacketsThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsLargestPoolSize()
     */
    public int getPacketsLargestPoolSize()
    {
        return _generalPacketsThreadPool.getLargestPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsPoolSize()
     */
    public int getPacketsPoolSize()
    {
        return _generalPacketsThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsCompletedTasks()
     */
    public long getPacketsCompletedTasks()
    {
        return _generalPacketsThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getPacketsQueuedTasks()
     */
    public long getPacketsQueuedTasks()
    {
        return _generalPacketsThreadPool.getQueue().size();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsActiveThreads()
     */
    public int getIoPacketsActiveThreads()
    {
        return _ioPacketsThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsCorePoolSize()
     */
    public int getIoPacketsCorePoolSize()
    {
        return _ioPacketsThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsMaximumPoolSize()
     */
    public int getIoPacketsMaximumPoolSize()
    {
        return _ioPacketsThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsLargestPoolSize()
     */
    public int getIoPacketsLargestPoolSize()
    {
        return _ioPacketsThreadPool.getLargestPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsPoolSize()
     */
    public int getIoPacketsPoolSize()
    {
        return _ioPacketsThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsCompletedTasks()
     */
    public long getIoPacketsCompletedTasks()
    {
        return _ioPacketsThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getIoPacketsQueuedTasks()
     */
    public long getIoPacketsQueuedTasks()
    {
        return _ioPacketsThreadPool.getQueue().size();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralActiveThreads()
     */
    public int getGeneralActiveThreads()
    {
        return _generalThreadPool.getActiveCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralCorePoolSize()
     */
    public int getGeneralCorePoolSize()
    {
        return _generalThreadPool.getCorePoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralMaximumPoolSize()
     */
    public int getGeneralMaximumPoolSize()
    {
        return _generalThreadPool.getMaximumPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralLargestPoolSize()
     */
    public int getGeneralLargestPoolSize()
    {
        return _generalThreadPool.getLargestPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralPoolSize()
     */
    public int getGeneralPoolSize()
    {
        return _generalThreadPool.getPoolSize();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralCompletedTasks()
     */
    public long getGeneralCompletedTasks()
    {
        return _generalThreadPool.getCompletedTaskCount();
    }

    /**
     * @see net.sf.l2j.gameserver.ThreadPoolManagerMBean#getGeneralQueuedTasks()
     */
    public long getGeneralQueuedTasks()
    {
        return _generalThreadPool.getQueue().size();
    }
}

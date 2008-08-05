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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.threadmanager.ThreadPoolManager1;

/**
 * @author -Wooden-, NB4L1
 */
public abstract class ThreadPoolManager
{
	protected static final Log _log = LogFactory.getLog(ThreadPoolManager.class);
	
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	public static ThreadPoolManager getInstance()
	{
		return ThreadPoolManager1.getInstance();
	}
	
	public final void startPurgeTask(long period)
	{
		scheduleAtFixedRate(new Runnable() {
			public void run()
			{
				purge();
			}
		}, period, period);
	}
	
	protected final long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	// ===========================================================================================
	
	public abstract ScheduledFuture<?> schedule(Runnable r, long delay);
	
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
	
	public abstract ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period);
	
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
	
	public abstract void execute(Runnable r);
	
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
	
	public abstract List<String> getStats();
	
	public abstract void shutdown();
	
	public abstract void purge();
}
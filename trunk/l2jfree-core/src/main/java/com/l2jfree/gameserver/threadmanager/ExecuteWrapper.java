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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public final class ExecuteWrapper implements Runnable
{
	private static final Log _log = LogFactory.getLog(ExecuteWrapper.class);
	
	private final Runnable _runnable;
	
	public ExecuteWrapper(Runnable runnable)
	{
		_runnable = runnable;
	}
	
	public void run()
	{
		ExecuteWrapper.execute(_runnable);
	}
	
	public static void execute(Runnable runnable)
	{
		long begin = System.nanoTime();
		
		try
		{
			runnable.run();
			
			RunnableStatsManager.getInstance().handleStats(runnable.getClass(), System.nanoTime() - begin);
		}
		catch (Exception e)
		{
			_log.warn("Exception in a Runnable execution:", e);
		}
	}
}

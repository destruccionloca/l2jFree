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
package com.l2jfree.util.concurrent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public final class RunnableStatsManager
{
	private static final Log _log = LogFactory.getLog(RunnableStatsManager.class);
	
	private static RunnableStatsManager _instance;
	
	public static RunnableStatsManager getInstance()
	{
		if (_instance == null)
			_instance = new RunnableStatsManager();
		
		return _instance;
	}
	
	private final Map<Class<?>, ClassStat> _classStats = new FastMap<Class<?>, ClassStat>().setShared(true);
	
	private class ClassStat
	{
		private final Class<?> _class;
		private long _runCount;
		private long _runTime;
		
		private ClassStat(Class<?> cl)
		{
			_class = cl;
			_classStats.put(cl, this);
		}
	}
	
	public synchronized void handleStats(Class<?> cl, long runTime)
	{
		ClassStat stat = _classStats.get(cl);
		
		if (stat == null)
			stat = new ClassStat(cl);
		
		stat._runCount++;
		stat._runTime += runTime;
	}
	
	public synchronized void dumpClassStats()
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("Class-" + System.currentTimeMillis() + ".log"));
			
			int i = 1;
			
			for (ClassStat stat : _classStats.values())
			{
				String className = stat._class.getName().replace("com.l2jfree.gameserver.", "");
				long runCount = stat._runCount;
				long runTime = stat._runTime;
				long average = runTime / runCount;
				
				String s = i++ + ".";
				s += " " + className + " - ";
				s += "Count: " + runCount + " - ";
				s += "Time: " + runTime + " - ";
				s += "Average: " + average;
				
				out.write(s);
				out.newLine();
			}
			
			out.close();
		}
		catch (IOException e)
		{
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}
}
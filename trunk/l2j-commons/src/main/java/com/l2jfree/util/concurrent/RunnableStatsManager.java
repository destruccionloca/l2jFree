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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
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
	
	private final Map<Class<?>, ClassStat> _classStats = new HashMap<Class<?>, ClassStat>();
	
	private final class ClassStat
	{
		private final String _name;
		
		private String[] _methodNames = new String[0];
		private MethodStat[] _methodStats = new MethodStat[0];
		
		private ClassStat(Class<?> clazz)
		{
			_classStats.put(clazz, this);
			
			_name = clazz.getName().replace("com.l2jfree.gameserver.", "");
		}
		
		private MethodStat getMethodStat(String methodName)
		{
			for (int i = 0; i < _methodNames.length; i++)
				if (_methodNames[i].equals(methodName))
					return _methodStats[i];
			
			methodName = methodName.intern();
			
			final MethodStat methodStat = new MethodStat(_name, methodName);
			
			_methodNames = (String[])ArrayUtils.add(_methodNames, methodName);
			_methodStats = (MethodStat[])ArrayUtils.add(_methodStats, methodStat);
			
			return methodStat;
		}
	}
	
	private final class MethodStat
	{
		private final String _name;
		
		private long _count;
		private long _total;
		private long _min = Long.MAX_VALUE;
		private long _max = Long.MIN_VALUE;
		
		private MethodStat(String className, String methodName)
		{
			_name = className + "." + methodName;
		}
		
		private void handleStats(long runTime)
		{
			_count++;
			_total += runTime;
			_min = Math.min(_min, runTime);
			_max = Math.max(_max, runTime);
		}
	}
	
	private ClassStat getClassStat(Class<?> clazz)
	{
		ClassStat stat = _classStats.get(clazz);
		
		if (stat == null)
			stat = new ClassStat(clazz);
		
		return stat;
	}
	
	public synchronized void handleStats(Class<? extends Runnable> clazz, long runTime)
	{
		handleStats(clazz, "run()", runTime);
	}
	
	public synchronized void handleStats(Class<?> clazz, String methodName, long runTime)
	{
		getClassStat(clazz).getMethodStat(methodName).handleStats(runTime);
	}
	
	@SuppressWarnings("unchecked")
	public static enum SortBy
	{
		AVG("average"),
		COUNT("count"),
		TOTAL("total"),
		NAME("name"),
		MIN("min"),
		MAX("max"), ;
		
		private final String _xmlName;
		
		private SortBy(String xmlName)
		{
			_xmlName = xmlName;
		}
		
		private final Comparator<MethodStat> _comparator = new Comparator<MethodStat>() {
			public int compare(MethodStat o1, MethodStat o2)
			{
				final Comparable c1 = getComparableValueOf(o1);
				final Comparable c2 = getComparableValueOf(o2);
				
				if (c1 instanceof Number)
					return c2.compareTo(c1);
				
				return c1.compareTo(c2);
			}
		};
		
		private Comparable getComparableValueOf(MethodStat stat)
		{
			switch (SortBy.this)
			{
				case NAME:
					return stat._name;
				case COUNT:
					return stat._count;
				case TOTAL:
					return stat._total;
				case MIN:
					return stat._min;
				case MAX:
					return stat._max;
				case AVG:
					return stat._total / stat._count;
				default:
					throw new InternalError();
			}
		}
		
		private static final SortBy[] VALUES = SortBy.values();
	}
	
	public void dumpClassStats()
	{
		dumpClassStats(null);
	}
	
	@SuppressWarnings("unchecked")
	public void dumpClassStats(final SortBy sortBy)
	{
		final List<MethodStat> methodStats = new ArrayList<MethodStat>();
		
		synchronized (this)
		{
			for (ClassStat classStat : _classStats.values())
				for (MethodStat methodStat : classStat._methodStats)
					methodStats.add(methodStat);
		}
		
		if (sortBy != null)
			Collections.sort(methodStats, sortBy._comparator);
		
		final List<String> lines = new ArrayList<String>();
		
		lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		lines.add("<methods>");
		lines.add("\t<!-- This XML contains statistics about execution times. -->");
		lines.add("\t<!-- Submitted results will help the developers to optimize the server. -->");
		
		final String[][] values = new String[SortBy.VALUES.length][methodStats.size()];
		final int[] maxLength = new int[SortBy.VALUES.length];
		
		for (int i = 0; i < SortBy.VALUES.length; i++)
		{
			final SortBy sort = SortBy.VALUES[i];
			
			for (int k = 0; k < methodStats.size(); k++)
			{
				final String value = String.valueOf(sort.getComparableValueOf(methodStats.get(k)));
				
				values[i][k] = value;
				
				maxLength[i] = Math.max(maxLength[i], value.length());
			}
		}
		
		for (int k = 0; k < methodStats.size(); k++)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\t<method ");
			
			if (sortBy != null)
				appendAttribute(sb, sortBy, values[sortBy.ordinal()][k], maxLength[sortBy.ordinal()]);
			
			for (SortBy sort : SortBy.VALUES)
				if (sort != sortBy)
					appendAttribute(sb, sort, values[sort.ordinal()][k], maxLength[sort.ordinal()]);
			
			sb.append("/>");
			
			lines.add(sb.toString());
		}
		
		lines.add("</methods>");
		
		PrintStream ps = null;
		try
		{
			ps = new PrintStream("MethodStats-" + System.currentTimeMillis() + ".log");
			
			for (String line : lines)
				ps.println(line);
			
			ps.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			IOUtils.closeQuietly(ps);
		}
	}
	
	private void appendAttribute(StringBuilder sb, SortBy sortBy, String value, int fillTo)
	{
		sb.append(sortBy._xmlName);
		sb.append("=\"");
		sb.append(value);
		sb.append("\" ");
		
		for (int i = value.length(); i < fillTo; i++)
			sb.append(" ");
	}
}

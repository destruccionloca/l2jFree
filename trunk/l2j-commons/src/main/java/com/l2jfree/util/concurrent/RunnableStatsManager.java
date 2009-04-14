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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	
	private final class ClassStat
	{
		private final String _name;
		private long _count;
		private long _total;
		private long _min = Long.MAX_VALUE;
		private long _max = Long.MIN_VALUE;
		
		private ClassStat(Class<?> cl)
		{
			_classStats.put(cl, this);
			
			_name = cl.getName().replace("com.l2jfree.gameserver.", "");
		}
		
		private void handleStats(long runTime)
		{
			_count++;
			_total += runTime;
			_min = Math.min(_min, runTime);
			_max = Math.max(_max, runTime);
		}
		
		private Element fill(Element cl)
		{
			cl.setAttribute("name", String.valueOf(_name));
			cl.setAttribute("count", String.valueOf(_count));
			cl.setAttribute("total", String.valueOf(_total));
			cl.setAttribute("min", String.valueOf(_min));
			cl.setAttribute("max", String.valueOf(_max));
			cl.setAttribute("average", String.valueOf(_total / _count));
			
			return cl;
		}
	}
	
	public synchronized void handleStats(Class<?> cl, long runTime)
	{
		ClassStat stat = _classStats.get(cl);
		
		if (stat == null)
			stat = new ClassStat(cl);
		
		stat.handleStats(runTime);
	}
	
	public static enum SortBy
	{
		NAME,
		COUNT,
		TOTAL,
		MIN,
		MAX,
		AVG;
		
		@SuppressWarnings("unchecked")
		private final Comparator<ClassStat> _comparator = new Comparator<ClassStat>() {
			private Comparable getComparableValueOf(ClassStat stat)
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
			
			public int compare(ClassStat o1, ClassStat o2)
			{
				return getComparableValueOf(o1).compareTo(getComparableValueOf(o2));
			}
		};
		
		private Comparator<ClassStat> getComparator()
		{
			return _comparator;
		}
	}
	
	public void dumpClassStats()
	{
		dumpClassStats(null);
	}
	
	public synchronized void dumpClassStats(SortBy sortBy)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			Node classes = doc.createElement("classes");
			doc.appendChild(classes);
			
			classes.appendChild(doc.createComment("This XML contains statistics about execution times."));
			classes.appendChild(doc.createComment("Submitted results will help the developers to optimize the server."));
			
			List<ClassStat> list = new ArrayList<ClassStat>(_classStats.values());
			if (sortBy != null)
				Collections.sort(list, sortBy.getComparator());
			
			for (ClassStat stat : list)
				classes.appendChild(stat.fill(doc.createElement("class")));
			
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty("indent", "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
			serializer.transform(new DOMSource(doc), new StreamResult("Class-" + System.currentTimeMillis() + ".log"));
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}
}

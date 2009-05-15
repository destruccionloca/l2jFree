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
package com.l2jfree.gameserver.geodata.pathfinding.utils;

import javolution.util.FastMap;

import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.util.L2FastSet;
import com.l2jfree.util.ThreadLocalObjectPool;

/**
 * @author Sami
 */
public final class CellNodeMap
{
	private final FastMap<Integer, L2FastSet<Node>> _cellIndex = new FastMap<Integer, L2FastSet<Node>>();
	
	private CellNodeMap()
	{
	}
	
	public void add(Node n)
	{
		L2FastSet<Node> set = _cellIndex.get(n.getLoc().getY());
		
		if (set == null)
			_cellIndex.put(n.getLoc().getY(), set = THREAD_LOCAL_SET_POOL.get());
		
		set.add(n);
	}
	
	public boolean contains(Node n)
	{
		L2FastSet<Node> set = _cellIndex.get(n.getLoc().getY());
		if (set == null)
			return false;
		
		return set.contains(n);
	}
	
	public static CellNodeMap newInstance()
	{
		return THREAD_LOCAL_POOL.get();
	}
	
	public static void recycle(CellNodeMap map)
	{
		THREAD_LOCAL_POOL.store(map);
	}
	
	private static final ThreadLocalObjectPool<CellNodeMap> THREAD_LOCAL_POOL = new ThreadLocalObjectPool<CellNodeMap>() {
		@Override
		protected void reset(CellNodeMap map)
		{
			for (FastMap.Entry<Integer, L2FastSet<Node>> e = map._cellIndex.head(), end = map._cellIndex.tail(); (e = e.getNext()) != end;)
			{
				THREAD_LOCAL_SET_POOL.store(e.getValue());
			}
			
			map._cellIndex.clear();
		}
		
		@Override
		protected CellNodeMap create()
		{
			return new CellNodeMap();
		}
	};
	
	private static final ThreadLocalObjectPool<L2FastSet<Node>> THREAD_LOCAL_SET_POOL = new ThreadLocalObjectPool<L2FastSet<Node>>() {
		@Override
		protected void reset(L2FastSet<Node> set)
		{
			set.clear();
		}
		
		@Override
		protected L2FastSet<Node> create()
		{
			return new L2FastSet<Node>();
		}
	};
}

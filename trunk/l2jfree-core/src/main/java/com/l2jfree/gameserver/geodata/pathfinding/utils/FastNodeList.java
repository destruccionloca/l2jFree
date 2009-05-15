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

import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.util.L2FastSet;
import com.l2jfree.util.ThreadLocalObjectPool;

/**
 * @author -Nemesiss-
 */
public final class FastNodeList
{
	private final L2FastSet<Node> _nodes = new L2FastSet<Node>();
	
	private FastNodeList()
	{
	}
	
	public void add(Node n)
	{
		_nodes.add(n);
	}
	
	public boolean contains(Node n)
	{
		return _nodes.contains(n);
	}
	
	public boolean containsRev(Node n)
	{
		return _nodes.contains(n);
	}
	
	public static FastNodeList newInstance()
	{
		return THREAD_LOCAL_POOL.get();
	}
	
	public static void recycle(FastNodeList list)
	{
		THREAD_LOCAL_POOL.store(list);
	}
	
	private static final ThreadLocalObjectPool<FastNodeList> THREAD_LOCAL_POOL = new ThreadLocalObjectPool<FastNodeList>() {
		@Override
		protected void reset(FastNodeList list)
		{
			list._nodes.clear();
		}
		
		@Override
		protected FastNodeList create()
		{
			return new FastNodeList();
		}
	};
}

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
package com.l2jfree.gameserver.pathfinding.worldnodes;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import com.l2jfree.gameserver.pathfinding.AbstractNodeLoc;
import com.l2jfree.gameserver.pathfinding.Node;
import com.l2jfree.gameserver.pathfinding.PathFinding;

import javolution.util.FastMap;

/**
 *
 * @author -Nemesiss-
 */
public class WorldPathFinding extends PathFinding
{
	//private static Logger _log = Logger.getLogger(WorldPathFinding.class.getName());
	private static WorldPathFinding			_instance;
	@SuppressWarnings("unused")
	private static Map<Short, ByteBuffer>	_pathNodes		= new FastMap<Short, ByteBuffer>();
	private static Map<Short, IntBuffer>	_pathNodesIndex	= new FastMap<Short, IntBuffer>();

	public static WorldPathFinding getInstance()
	{
		if (_instance == null)
			_instance = new WorldPathFinding();
		return _instance;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return _pathNodesIndex.containsKey(regionoffset);
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
	@Override
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz)
	{
		return null;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.PathFinding#ReadNeighbors(short, short)
	 */
	@Override
	public Node[] readNeighbors(short node_x, short node_y, int idx)
	{
		return null;
	}

	private WorldPathFinding()
	{
	}
}

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
package com.l2jfree.gameserver.geodata;

import java.util.ArrayList;

import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.Location;

/**
 * @Author: Diamond
 * @Date: 20/5/2007
 * @Time: 9:57:48
 */
public class PathFind
{
	private static final byte	EAST			= 1, WEST = 2, SOUTH = 4, NORTH = 8;

	private int					mapSize;

	private GeoNode[][]			map;
	private GeoNode[]			mapFast;

	private int					mapFastIndex	= 0;
	private int					mapFastSize		= 0;

	private int					offsetX;
	private int					offsetY;

	private Location			startpoint;
	private Location			endpoint;

	private ArrayList<GeoNode>	path;

	private final GeoEngine		engine;

	long						time;

	public PathFind(int x, int y, int z, int destX, int destY, int destZ, GeoEngine engine)
	{
		this.engine = engine;
		time = System.currentTimeMillis();

		startpoint = new Location(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
		endpoint = new Location(destX - L2World.MAP_MIN_X >> 4, destY - L2World.MAP_MIN_Y >> 4, destZ);

		startpoint = engine.MoveCheckForPathFind(startpoint.getX(), startpoint.getY(), startpoint.getZ(), endpoint.getX(), endpoint.getY());
		endpoint = engine.MoveCheckForPathFind(endpoint.getX(), endpoint.getY(), endpoint.getZ(), startpoint.getX(), startpoint.getY());

		startpoint.setZ(engine.NgetHeight(startpoint.getX(), startpoint.getY(), startpoint.getZ()));
		endpoint.setZ(engine.NgetHeight(endpoint.getX(), endpoint.getY(), endpoint.getZ()));

		int xdiff = Math.abs(endpoint.getX() - startpoint.getX());
		int ydiff = Math.abs(endpoint.getY() - startpoint.getY());

		if (xdiff == 0 && ydiff == 0 || xdiff + ydiff == 1)
		{
			if (Math.abs(endpoint.getZ() - startpoint.getZ()) < 64)
			{
				path = new ArrayList<GeoNode>();
				path.add(0, new GeoNode(startpoint, 0, null));
			}
			return;
		}

		mapSize = 64 + Math.max(xdiff, ydiff);

		if (mapSize > 500)
			return;

		mapFastSize = mapSize * mapSize;

		map = new GeoNode[mapSize][mapSize];
		mapFast = new GeoNode[mapFastSize];

		offsetX = startpoint.getX() - mapSize / 2;
		offsetY = startpoint.getY() - mapSize / 2;

		path = findPath();
	}

	private GeoNode getBestOpenNode()
	{
		GeoNode bestNode = null;
		int bestNodeIndex = 0;

		for (int i = 0; i < mapFastIndex; i++)
		{
			GeoNode n = mapFast[i];
			if (n.closed)
				continue;
			if (bestNode == null || n.score < bestNode.score)
			{
				bestNode = n;
				bestNodeIndex = i;
			}
		}

		if (bestNode != null)
		{
			bestNode.closed = true;
			mapFastIndex--;
			mapFast[bestNodeIndex] = mapFast[mapFastIndex];
		}

		return bestNode;
	}

	private ArrayList<GeoNode> tracePath(GeoNode f)
	{
		ArrayList<GeoNode> nodes = new ArrayList<GeoNode>();
		GeoNode parent = f.parent;
		nodes.add(0, f);

		while (true)
		{
			if (parent.parent == null)
				break;
			nodes.add(0, parent);
			parent = parent.parent;
		}
		return nodes;
	}

	public ArrayList<GeoNode> findPath()
	{
		GeoNode n = new GeoNode(startpoint, 0, null);
		map[startpoint.getX() - offsetX][startpoint.getY() - offsetY] = n;
		mapFast[mapFastIndex] = n;
		mapFastIndex++;

		GeoNode nextNode;
		nextNode = getBestOpenNode();
		GeoNode finish;

		while (nextNode != null)
		{
			if (mapFastIndex >= mapFastSize)
				return null;
			finish = handleNode(nextNode);
			if (finish != null)
				return tracePath(finish);
			if (System.currentTimeMillis() - time > 50)
				return null;
			nextNode = getBestOpenNode();
		}
		return null;
	}

	public GeoNode handleNode(GeoNode node)
	{
		Location cl = node.location;
		GeoNode result = null;

		int clX = cl.getX();
		int clY = cl.getY();
		int clZ = cl.getZ();
		short NSWE;

		NSWE = engine.NgetNSWE(clX, clY, clZ);

		if ((NSWE & EAST) == EAST)
			result = getNeighbour(clX + 1, clY, node);

		if (result != null)
			return result;

		if ((NSWE & WEST) == WEST)
			result = getNeighbour(clX - 1, clY, node);

		if (result != null)
			return result;

		if ((NSWE & SOUTH) == SOUTH)
			result = getNeighbour(clX, clY + 1, node);

		if (result != null)
			return result;

		if ((NSWE & NORTH) == NORTH)
			result = getNeighbour(clX, clY - 1, node);

		return result;
	}

	public GeoNode getNeighbour(int x, int y, GeoNode from)
	{
		if (mapFastIndex >= mapFastSize)
			return null;

		if (x - offsetX > mapSize - 1 || x < offsetX || y - offsetY > mapSize - 1 || y < offsetY)
			return null;

		if (map[x - offsetX][y - offsetY] != null && map[x - offsetX][y - offsetY].closed)
			return null;

		int z = engine.NgetHeight(x, y, from.location.getZ());

		int height = Math.abs(z - from.location.getZ());

		if (height >= 64)
			return null;

		int weight = 0;

		if (engine.NgetNSWE(x, y, z) != 15 || height > 8)
			weight = 8;
		else if (engine.NgetNSWE(x + 1, y, z) != 15 || Math.abs(z - engine.NgetHeight(x + 1, y, z)) > 8)
			weight = 4;
		else if (engine.NgetNSWE(x - 1, y, z) != 15 || Math.abs(z - engine.NgetHeight(x - 1, y, z)) > 8)
			weight = 4;
		else if (engine.NgetNSWE(x, y + 1, z) != 15 || Math.abs(z - engine.NgetHeight(x, y + 1, z)) > 8)
			weight = 4;
		else if (engine.NgetNSWE(x, y - 1, z) != 15 || Math.abs(z - engine.NgetHeight(x, y - 1, z)) > 8)
			weight = 4;

		int dx = Math.abs(endpoint.getX() - x);
		int dy = Math.abs(endpoint.getY() - y);
		int dz = Math.abs(endpoint.getZ() - z) / 16;

		GeoNode n = new GeoNode(new Location(x, y, z), 1, from);

		n.moveCost += from.moveCost + weight;
		n.score = n.moveCost + (int) Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (x == endpoint.getX() && y == endpoint.getY() && Math.abs(z - endpoint.getZ()) < 64)
			return n;

		if (n.score < mapSize * 3)
		{
			GeoNode on = map[x - offsetX][y - offsetY];
			if (on == null || n.moveCost < on.moveCost)
			{
				map[x - offsetX][y - offsetY] = n;
				mapFast[mapFastIndex] = n;
				mapFastIndex++;
			}
		}

		return null;
	}

	public class GeoNode
	{
		public GeoNode	parent		= null;
		public Location	location	= null;
		public int		moveCost	= 0;
		public int		score		= 0;
		public boolean	closed		= false;

		public GeoNode(Location loc, int mCost, GeoNode pNode)
		{
			location = loc;
			moveCost = mCost;
			parent = pNode;
		}
	}

	public ArrayList<GeoNode> getPath()
	{
		return path;
	}
}

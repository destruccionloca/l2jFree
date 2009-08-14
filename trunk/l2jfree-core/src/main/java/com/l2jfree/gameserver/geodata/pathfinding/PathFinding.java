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
package com.l2jfree.gameserver.geodata.pathfinding;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.Config;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.geodata.pathfinding.cellnodes.CellPathFinding;
import com.l2jfree.gameserver.geodata.pathfinding.geonodes.GeoPathFinding;
import com.l2jfree.gameserver.geodata.pathfinding.utils.BinaryNodeHeap;
import com.l2jfree.gameserver.geodata.pathfinding.utils.CellNodeMap;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.util.L2Arrays;
import com.l2jfree.util.L2Collections;
import com.l2jfree.util.L2FastSet;

/**
 * @author -Nemesiss-
 */
public abstract class PathFinding
{
	public static PathFinding getInstance()
	{
		if (!Config.GEODATA_CELLFINDING)
			return GeoPathFinding.getInstance(); //Higher Memory Usage, Smaller Cpu Usage
		else
			return CellPathFinding.getInstance(); // Cell pathfinding, calculated directly from geodata files
	}
	
	public abstract boolean pathNodesExist(short regionoffset);
	
	public abstract AbstractNodeLoc[] findPath(int x, int y, int z, int tx, int ty, int tz);
	
	public abstract Node[] readNeighbors(Node n, int idx);
	
	public final AbstractNodeLoc[] search(Node start, Node end)
	{
		// The simplest grid-based pathfinding.
		// Drawback is not having higher cost for diagonal movement (means funny routes)
		// Could be optimized e.g. not to calculate backwards as far as forwards.
		
		// List of Visited Nodes
		L2FastSet<Node> visited = L2Collections.newL2FastSet();
		
		// List of Nodes to Visit
		L2FastSet<Node> to_visit = L2Collections.newL2FastSet();
		to_visit.add(start);
		try
		{
			int i = 0;
			while (i < 800)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				Node node = to_visit.removeFirst();
				
				if (node.equals(end)) //path found!
					return constructPath(node);
				else
				{
					i++;
					visited.add(node);
					node.attachNeighbors();
					Node[] neighbors = node.getNeighbors();
					if (neighbors == null)
						continue;
					for (Node n : neighbors)
					{
						if (!visited.contains(n) && !to_visit.contains(n))
						{
							n.setParent(node);
							to_visit.add(n);
						}
					}
				}
			}
			//No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final AbstractNodeLoc[] searchByClosest(Node start, Node end)
	{
		// Note: This is the version for cell-based calculation, harder
		// on cpu than from block-based pathnode files. However produces better routes.
		
		// Always continues checking from the closest to target non-blocked
		// node from to_visit list. There's extra length in path if needed
		// to go backwards/sideways but when moving generally forwards, this is extra fast
		// and accurate. And can reach insane distances (try it with 8000 nodes..).
		// Minimum required node count would be around 300-400.
		// Generally returns a bit (only a bit) more intelligent looking routes than
		// the basic version. Not a true distance image (which would increase CPU
		// load) level of intelligence though.
		
		// List of Visited Nodes
		CellNodeMap known = CellNodeMap.newInstance();
		// List of Nodes to Visit
		ArrayList<Node> to_visit = L2Collections.newArrayList();
		to_visit.add(start);
		known.add(start);
		try
		{
			int targetx = end.getLoc().getNodeX();
			int targety = end.getLoc().getNodeY();
			int targetz = end.getLoc().getZ();
			
			int dx, dy, dz;
			boolean added;
			int i = 0;
			while (i < 3500)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				Node node = to_visit.remove(0);
				
				i++;
				
				node.attachNeighbors();
				if (node.equals(end))
				{
					//path found! note that node z coordinate is updated only in attach
					//to improve performance (alternative: much more checks)
					//System.out.println("path found, i:"+i);
					return constructPath(node);
				}
				
				Node[] neighbors = node.getNeighbors();
				if (neighbors == null)
					continue;
				for (Node n : neighbors)
				{
					if (!known.contains(n))
					{
						
						added = false;
						n.setParent(node);
						dx = targetx - n.getLoc().getNodeX();
						dy = targety - n.getLoc().getNodeY();
						dz = targetz - n.getLoc().getZ();
						n.setCost(dx * dx + dy * dy + dz / 2 * dz/*+n.getCost()*/);
						for (int index = 0; index < to_visit.size(); index++)
						{
							// supposed to find it quite early..
							if (to_visit.get(index).getCost() > n.getCost())
							{
								to_visit.add(index, n);
								added = true;
								break;
							}
						}
						if (!added)
							to_visit.add(n);
						known.add(n);
					}
				}
			}
			//No Path found
			//System.out.println("no path found");
			return null;
		}
		finally
		{
			CellNodeMap.recycle(known);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final AbstractNodeLoc[] searchByClosest2(Node start, Node end)
	{
		// Always continues checking from the closest to target non-blocked
		// node from to_visit list. There's extra length in path if needed
		// to go backwards/sideways but when moving generally forwards, this is extra fast
		// and accurate. And can reach insane distances (try it with 800 nodes..).
		// Minimum required node count would be around 300-400.
		// Generally returns a bit (only a bit) more intelligent looking routes than
		// the basic version. Not a true distance image (which would increase CPU
		// load) level of intelligence though.
		
		// List of Visited Nodes
		L2FastSet<Node> visited = L2Collections.newL2FastSet();
		// List of Nodes to Visit
		ArrayList<Node> to_visit = L2Collections.newArrayList();
		to_visit.add(start);
		try
		{
			int targetx = end.getLoc().getNodeX();
			int targety = end.getLoc().getNodeY();
			int dx, dy;
			boolean added;
			int i = 0;
			while (i < 550)
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				Node node = to_visit.remove(0);
				
				if (node.equals(end)) //path found!
					return constructPath2(node);
				else
				{
					i++;
					visited.add(node);
					node.attachNeighbors();
					Node[] neighbors = node.getNeighbors();
					if (neighbors == null)
						continue;
					for (Node n : neighbors)
					{
						if (!visited.contains(n) && !to_visit.contains(n))
						{
							added = false;
							n.setParent(node);
							dx = targetx - n.getLoc().getNodeX();
							dy = targety - n.getLoc().getNodeY();
							n.setCost(dx * dx + dy * dy);
							for (int index = 0; index < to_visit.size(); index++)
							{
								// supposed to find it quite early..
								if (to_visit.get(index).getCost() > n.getCost())
								{
									to_visit.add(index, n);
									added = true;
									break;
								}
							}
							if (!added)
								to_visit.add(n);
						}
					}
				}
			}
			//No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			L2Collections.recycle(to_visit);
		}
	}
	
	public final AbstractNodeLoc[] searchAStar(Node start, Node end)
	{
		// Not operational yet?
		int start_x = start.getLoc().getX();
		int start_y = start.getLoc().getY();
		int end_x = end.getLoc().getX();
		int end_y = end.getLoc().getY();
		//List of Visited Nodes
		L2FastSet<Node> visited = L2Collections.newL2FastSet();//TODO! Add limit to cfg
		
		// List of Nodes to Visit
		BinaryNodeHeap to_visit = BinaryNodeHeap.newInstance();
		to_visit.add(start);
		try
		{
			int i = 0;
			while (i < 800)//TODO! Add limit to cfg
			{
				if (to_visit.isEmpty())
				{
					// No Path found
					return null;
				}
				
				Node node;
				try
				{
					node = to_visit.removeFirst();
				}
				catch (Exception e)
				{
					// No Path found
					return null;
				}
				if (node.equals(end)) //path found!
					return constructPath(node);
				else
				{
					visited.add(node);
					node.attachNeighbors();
					for (Node n : node.getNeighbors())
					{
						if (!visited.contains(n) && !to_visit.contains(n))
						{
							i++;
							n.setParent(node);
							n.setCost(Math.abs(start_x - n.getLoc().getNodeX())
								+ Math.abs(start_y - n.getLoc().getNodeY()) + Math.abs(end_x - n.getLoc().getNodeX())
								+ Math.abs(end_y - n.getLoc().getNodeY()));
							to_visit.add(n);
						}
					}
				}
			}
			//No Path found
			return null;
		}
		finally
		{
			L2Collections.recycle(visited);
			BinaryNodeHeap.recycle(to_visit);
		}
	}
	
	public final AbstractNodeLoc[] constructPath(Node node)
	{
		ArrayList<AbstractNodeLoc> tmp = L2Collections.newArrayList();
		int previousdirectionx = -1000;
		int previousdirectiony = -1000;
		int directionx;
		int directiony;
		while (node.getParent() != null)
		{
			// only add a new route point if moving direction changes
			if (node.getParent().getParent() != null // to check and clean diagonal movement
				&& Math.abs(node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX()) == 1
				&& Math.abs(node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY()) == 1)
			{
				directionx = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
				directiony = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
			}
			else
			{
				directionx = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
				directiony = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			}
			if (directionx != previousdirectionx || directiony != previousdirectiony)
			{
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				tmp.add(node.getLoc());
			}
			node = node.getParent();
		}
		
		AbstractNodeLoc[] path = tmp.toArray(new AbstractNodeLoc[tmp.size()]);
		
		L2Collections.recycle(tmp);
		
		ArrayUtils.reverse(path);
		
		// then LOS based filtering to reduce the number of route points
		if (path.length > 4)
		{
			//System.out.println("pathsize:"+path.size());
			for (int index = 0; index < path.length - 3; index = index + 3)
			{
				//System.out.println("Attempt filter");
				AbstractNodeLoc n1 = path[index];
				AbstractNodeLoc n2 = path[index + 3];
				
				if (GeoData.getInstance().canMoveFromToTarget(n1.getX(), n1.getY(), n1.getZ(), n2.getX(), n2.getY(),
					n2.getZ()))
				{
					//System.out.println("filtering i:"+(index+1));
					path[index + 1] = null;
					path[index + 2] = null;
				}
			}
			
			//System.out.println("pathsize:"+path.size());
		}
		
		return L2Arrays.compact(path);
	}
	
	public final AbstractNodeLoc[] constructPath2(Node node)
	{
		ArrayList<AbstractNodeLoc> tmp = L2Collections.newArrayList();
		int previousdirectionx = -1000;
		int previousdirectiony = -1000;
		int directionx;
		int directiony;
		while (node.getParent() != null)
		{
			// only add a new route point if moving direction changes
			directionx = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
			directiony = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			if (directionx != previousdirectionx || directiony != previousdirectiony)
			{
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				tmp.add(node.getLoc());
			}
			node = node.getParent();
		}
		
		AbstractNodeLoc[] path = tmp.toArray(new AbstractNodeLoc[tmp.size()]);
		
		L2Collections.recycle(tmp);
		
		ArrayUtils.reverse(path);
		
		return path;
	}
	
	/**
	 * Convert geodata position to pathnode position
	 * 
	 * @param geo_pos
	 * @return pathnode position
	 */
	public final short getNodePos(int geo_pos)
	{
		return (short)(geo_pos >> 3); //OK?
	}
	
	/**
	 * Convert node position to pathnode block position
	 * 
	 * @param geo_pos
	 * @return pathnode block position (0...255)
	 */
	public final short getNodeBlock(int node_pos)
	{
		return (short)(node_pos % 256);
	}
	
	public final byte getRegionX(int node_pos)
	{
		return (byte)((node_pos >> 8) + 15);
	}
	
	public final byte getRegionY(int node_pos)
	{
		return (byte)((node_pos >> 8) + 10);
	}
	
	public final short getRegionOffset(byte rx, byte ry)
	{
		return (short)((rx << 5) + ry);
	}
	
	/**
	 * Convert pathnode x to World x position
	 * 
	 * @param node_x, rx
	 * @return
	 */
	public final int calculateWorldX(short node_x)
	{
		return L2World.MAP_MIN_X + node_x * 128 + 48;
	}
	
	/**
	 * Convert pathnode y to World y position
	 * 
	 * @param node_y
	 * @return
	 */
	public final int calculateWorldY(short node_y)
	{
		return L2World.MAP_MIN_Y + node_y * 128 + 48;
	}
}

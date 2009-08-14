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

/**
 * @author -Nemesiss-
 */
public final class Node
{
	private final AbstractNodeLoc _loc;
	private final int _neighborsIdx;
	private Node[] _neighbors;
	private Node _parent;
	private short _cost;
	
	public Node(AbstractNodeLoc loc, int neighborsIdx)
	{
		_loc = loc;
		_neighborsIdx = neighborsIdx;
	}
	
	public void setParent(Node p)
	{
		_parent = p;
	}
	
	public void setCost(int cost)
	{
		_cost = (short)cost;
	}
	
	public void attachNeighbors()
	{
		if (_loc == null)
			_neighbors = null;
		else
			_neighbors = PathFinding.getInstance().readNeighbors(this, _neighborsIdx);
	}
	
	public Node[] getNeighbors()
	{
		return _neighbors;
	}
	
	public Node getParent()
	{
		return _parent;
	}
	
	public AbstractNodeLoc getLoc()
	{
		return _loc;
	}
	
	public short getCost()
	{
		return _cost;
	}
	
	@Override
	public int hashCode()
	{
		return getLoc().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Node))
			return false;
		
		Node n = (Node)obj;
		
		return getLoc().equals(n.getLoc());
	}
}

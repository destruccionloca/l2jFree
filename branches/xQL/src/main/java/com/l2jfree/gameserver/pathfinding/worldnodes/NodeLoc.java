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

import com.l2jfree.gameserver.pathfinding.AbstractNodeLoc;

/**
 *
 * @author -Nemesiss-
 */
public class NodeLoc extends AbstractNodeLoc
{
	private final int	_x;
	private final int	_y;
	private final short	_z;

	public NodeLoc(int x, int y, short z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.AbstractNodeLoc#getX()
	 */
	@Override
	public int getX()
	{
		return _x;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.AbstractNodeLoc#getY()
	 */
	@Override
	public int getY()
	{
		return _y;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.AbstractNodeLoc#getZ()
	 */
	@Override
	public short getZ()
	{
		return _z;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.AbstractNodeLoc#getNodeX()
	 */
	@Override
	public short getNodeX()
	{
		return 0;
	}

	/**
	 * @see com.l2jfree.gameserver.pathfinding.AbstractNodeLoc#getNodeY()
	 */
	@Override
	public short getNodeY()
	{
		return 0;
	}

}

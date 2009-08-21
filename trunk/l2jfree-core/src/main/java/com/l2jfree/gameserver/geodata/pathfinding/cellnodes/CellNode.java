package com.l2jfree.gameserver.geodata.pathfinding.cellnodes;

import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.gameserver.model.L2World;

/**
 * @author NB4L1
 */
public final class CellNode extends Node
{
	private final int _x;
	private final int _y;
	private short _z;
	
	public CellNode(int x, int y, short z, int neighborsIdx)
	{
		super(neighborsIdx);
		
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public int getX()
	{
		return (_x << 4) + L2World.MAP_MIN_X;
	}
	
	@Override
	public int getY()
	{
		return (_y << 4) + L2World.MAP_MIN_Y;
	}
	
	@Override
	public short getZ()
	{
		return _z;
	}
	
	@Override
	public void setZ(short z)
	{
		_z = z;
	}
	
	@Override
	public int getNodeX()
	{
		return _x;
	}
	
	@Override
	public int getNodeY()
	{
		return _y;
	}
}

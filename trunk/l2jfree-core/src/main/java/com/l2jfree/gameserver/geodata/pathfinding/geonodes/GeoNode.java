package com.l2jfree.gameserver.geodata.pathfinding.geonodes;

import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.gameserver.model.L2World;

/**
 * @author NB4L1
 */
public final class GeoNode extends Node
{
	private final short _x;
	private final short _y;
	private final short _z;
	
	public GeoNode(short x, short y, short z, int neighborsIdx)
	{
		super(neighborsIdx);
		
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public int getX()
	{
		return L2World.MAP_MIN_X + _x * 128 + 48;
	}
	
	@Override
	public int getY()
	{
		return L2World.MAP_MIN_Y + _y * 128 + 48;
	}
	
	@Override
	public short getZ()
	{
		return _z;
	}
	
	@Override
	public void setZ(short z)
	{
		//
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

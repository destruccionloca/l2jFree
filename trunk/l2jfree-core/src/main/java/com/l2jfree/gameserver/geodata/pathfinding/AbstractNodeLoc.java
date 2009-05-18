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

import com.l2jfree.lang.L2System;

/**
 * @author -Nemesiss-
 */
public abstract class AbstractNodeLoc
{
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract short getZ();
	
	public abstract void setZ(short z);
	
	public abstract int getNodeX();
	
	public abstract int getNodeY();
	
	@Override
	public int hashCode()
	{
		return L2System.hash(getX() ^ getY() ^ getZ());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AbstractNodeLoc))
			return false;
		
		AbstractNodeLoc loc = (AbstractNodeLoc)obj;
		
		return getX() == loc.getX() && getY() == loc.getY() && getZ() == loc.getZ();
	}
}

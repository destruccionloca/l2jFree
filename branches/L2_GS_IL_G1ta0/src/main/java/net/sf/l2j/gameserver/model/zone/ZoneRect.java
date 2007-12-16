/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;

/**
 * @author G1ta0 
 * This class implements Rectangle Zone
 */

public class ZoneRect extends ZoneBase
{

	public ZoneRect()
	{}

	public ZoneRect(int id, String zoneName, ZoneType zoneType, StatsSet zoneSet)
	{
		super(id, zoneName, zoneType, zoneSet);
	}

	@Override
	public boolean checkIfInZone(L2Object obj)
	{
		return checkIfInZone(obj.getPosition().getX(), obj.getPosition().getY(), obj.getPosition().getZ());
	}

	@Override
	public boolean checkIfInZone(int x, int y)
	{
		if(x >= getMin().getX() && x <= getMax().getX() && y >= getMin().getY() && y <= getMax().getY())
			return true;

		return false;
	}

	@Override
	public boolean checkIfInZone(int x, int y, int z)
	{
		if(checkIfInZone(x, y))
		{
			if(getMin().getZ() == 0 && getMax().getZ() == 0)
				return true;
			else if(z >= getMin().getZ() && z <= getMax().getZ())
				return true;
		}
		return false;

	}

	@Override
	public Location getRandomLocation()
	{
		int z = getMin().getZ();

		int x = getMin().getX() + Rnd.nextInt(getMax().getX() - getMin().getX());
		int y = getMin().getY() + Rnd.nextInt(getMax().getY() - getMin().getY());
		return new Location(x, y, z);
	}

	@Override
	public double getZoneDistance(int x, int y)
	{
		int x2 = getMin().getX() + Math.abs(getMax().getX() - getMin().getX()) / 2;
		int y2 = getMin().getY() + Math.abs(getMax().getY() - getMin().getY()) / 2;

		return Util.calculateDistance(x, y, 0, x2, y2);
	}

	@Override
	public double getZoneDistance(int x, int y, int z)
	{
		int x2 = getMin().getX() + Math.abs(getMax().getX() - getMin().getX()) / 2;
		int y2 = getMin().getY() + Math.abs(getMax().getY() - getMin().getY()) / 2;
		int z2 = getMin().getZ();

		return Util.calculateDistance(x, y, z, x2, y2, z2, true);
	}
	
	@Override
	public boolean intersectsRectangle(int cx, int cy, int dx, int dy)
	{
	
		if (checkIfInZone(cx, cy)) return true;

		int rx = 0;
		int ry = 0;
		
		int ax = getMin().getX();
		int ay = getMin().getY();
		int bx = getMax().getX();
		int by = getMax().getY();

		if (Util.checkIfLineSegementsIntersects(ax, ay, bx, by, cx, cy, cx, dy, rx, ry)) return true;
		if (Util.checkIfLineSegementsIntersects(ax, ay, bx, by, cx, dy, dx, dy, rx, ry)) return true;
		if (Util.checkIfLineSegementsIntersects(ax, ay, bx, by, dx, dy, dx, cy, rx, ry)) return true;
		if (Util.checkIfLineSegementsIntersects(ax, ay, bx, by, cx, cy, dx, cy, rx, ry)) return true;
		
		return false;
	}
}

/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.util.Util;
/**
 * @author G1ta0
 * This class implements Polygon Zone
 */

public class ZonePoly extends ZoneBase
{

    public ZonePoly()
    {
    }
    
	public ZonePoly(int id, String zoneName, ZoneType zoneType)
	{
		setId(id);
		setCastleId(0);
		setTownId(0);
		setZoneType(zoneType);
		setZoneName(zoneName);
	}

	public ZonePoly(int id, int castleId, int townId, String zoneName, ZoneType zoneType)
	{
		setId(id);
		setCastleId(castleId);
		setTownId(townId);
		setZoneType(zoneType);
		setZoneName(zoneName);
	}

	public boolean checkIfInZone(L2Object obj)
	{
		return checkIfInZone(obj.getPosition().getX(), obj.getPosition().getY(), obj.getPosition().getZ());
	}

	public boolean checkIfInZone(int x, int y)
	{
		return cn_PnPoly(x, y);
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		if (checkIfInZone(x, y))
		{
			if (getMin().getZ() == 0 && getMax().getZ() == 0)
				return true;
			else if (z >= getMin().getZ() && z <= getMax().getZ())
				return true;
		}
		return false;

	}

	public Location getRandomLocation()
	{
		int z = getMin().getZ();

		int x = getMin().getX() + Rnd.nextInt(getMax().getX() - getMin().getX());
		int y = getMin().getY() + Rnd.nextInt(getMax().getY() - getMin().getY());
		return new Location(x, y, z);
	}

	public double getZoneDistance(int x, int y)
	{
		int x2 = getMin().getX() + Math.abs(getMax().getX() - getMin().getX()) / 2;
		int y2 = getMin().getY() + Math.abs(getMax().getY() - getMin().getY()) / 2;
		
		return Util.calculateDistance(x, y, 0, x2, y2);
	}

	public double getZoneDistance(int x, int y, int z)
	{
		int x2 = getMin().getX() + Math.abs(getMax().getX() - getMin().getX()) / 2;
		int y2 = getMin().getY() + Math.abs(getMax().getY() - getMin().getY()) / 2;
		int z2 = getMin().getZ();
		
		return Util.calculateDistance(x, y, z, x2, y2, z2, true);
	}

	/**
	 * cn_PnPoly(): crossing number test for a point in a polygon
	 * Return:  false = outside, true = inside 
	 * http://www.geometryalgorithms.com/Archive/algorithm_0103/algorithm_0103.htm
	 */
	private final boolean cn_PnPoly( int x, int y)
	{
	    int    cn = 0;    // the crossing number counter

	    // loop through all edges of the polygon
	    for (int i=0; i<getPoints().size() - 1; i++) {
	       if (((getPoints().get(i).getY() <= y) && (getPoints().get(i+1).getY() > y))    // an upward crossing
	        || ((getPoints().get(i).getY() > y) && (getPoints().get(i+1).getY() <= y))) { // a downward crossing
	            // compute the actual edge-ray intersect x-coordinate
	            float vt = (float)(y - getPoints().get(i).getY()) / (getPoints().get(i+1).getY() - getPoints().get(i).getY());
	            if (x < getPoints().get(i).getX() + vt * (getPoints().get(i+1).getX() - getPoints().get(i).getX())) // x < intersect
	                ++cn;
	        }
	    }
	    return (cn & 1) == 1;    // 0 if even (out), and 1 if odd (in)
	}
}


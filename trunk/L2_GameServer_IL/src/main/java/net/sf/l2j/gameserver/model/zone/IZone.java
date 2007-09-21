/*
 * This program is free software; you can redistribute it and/or modify
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

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * @author G1ta0
 * Interface for Zone classes
 */

public interface IZone
{
	
	public void setId(int id);
	
	public void setCastleId(int castleId);
	
	public void setTownId(int townId);
	
	public void setZoneType(ZoneType zoneType);
	
	public void setZoneName(String zoneName);
	
	public void addPoint(Point3D point);
	
	public void addRestartPoint(RestartType restartType,Point3D point);
	
	public boolean checkIfInZone(L2Object obj);
	
	public boolean checkIfInZone(int x, int y);
	
	public boolean checkIfInZone(int x, int y, int z);
	
	public double getZoneDistance(int x, int y);
	
	public double getZoneDistance(int x, int y, int z);
	
	public int getId();
	
	public int getCastleId();
	
	public int getTownId();
	
	public String getZoneName();
	
	public ZoneType getZoneType();
	
	public Location getRestartPoint(RestartType restartType);
	
	public FastList<Point3D> getPoints();
	
	public Point3D getMin();
	
	public Point3D getMax();
}

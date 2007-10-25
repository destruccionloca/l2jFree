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
	/** Set Zone internal Id **/
	public void setId(int id);
	
	/** Set Castle Id, that zone belongs to **/
	public void setCastleId(int castleId);
	
	/** Set Town Id, that zone belongs to **/
	public void setTownId(int townId);
	
	/** Set zone type **/
	public void setZoneType(ZoneType zoneType);
	
	/** Set zone name **/
	public void setZoneName(String zoneName);
	
	/** Add zone point for calculation **/
	public void addPoint(Point3D point);
	
	/** Add restart point for player spawn **/
	public void addRestartPoint(RestartType restartType,Point3D point);
	
	/** Check if L2Object is in zone **/
	public boolean checkIfInZone(L2Object obj);
	
	/** Check if x,y is in planar zone **/
	public boolean checkIfInZone(int x, int y);
	
	/** Check if point is in zone **/
	public boolean checkIfInZone(int x, int y, int z);
	
	/** Calculate distance from x,y point to center of planar zone rectangle **/
	public double getZoneDistance(int x, int y);
	
	/** Calculate distance from x,y,z point to center of zone rectangle **/
	public double getZoneDistance(int x, int y, int z);
	
	/** Get Zone internal Id **/
	public int getId();
	
	/** Get Castle Id, that zone belongs to **/
	public int getCastleId();
	
	/** Get Town Id, that zone belongs to **/
	public int getTownId();
	
	/** Get zone name **/
	public String getZoneName();
	
	/** Get zone type **/
	public ZoneType getZoneType();
	
	/** Get restart point for player spawn **/
	public Location getRestartPoint(RestartType restartType);
	
	/** Get zone points **/
	public FastList<Point3D> getPoints();
	
	/** Get left bottom point of zone rectangle **/
	public Point3D getMin();
	
	/** Get right top point of zone rectangle **/
	public Point3D getMax();
}

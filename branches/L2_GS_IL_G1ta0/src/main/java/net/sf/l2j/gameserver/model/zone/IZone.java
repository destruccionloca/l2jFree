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

import java.util.Set;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * @author G1ta0
 * Interface for Zone classes
 */

public interface IZone
{
	/** Set Zone internal Id **/
	public void setId(int id);
	
	/** Set zone settings **/
	public void setSettings(StatsSet zoneSet);
	
	/** Set zone type **/
	public void setZoneType(ZoneType zoneType);
	
	/** Set zone name **/
	public void setZoneName(String zoneName);
	
	/** Add zone point for calculation **/
	public void addPoint(int x, int y);
	
	/** Set zone max, min z coord **/
	public void setZ(int zMin, int zMax);
	
	/** Check if L2Object is in zone **/
	public boolean checkIfInZone(L2Object obj);

	/** Check if L2Character is in zone **/
	public boolean checkIfCharacterInZone(L2Character character);
	
	/** Check if x,y is in planar zone **/
	public boolean checkIfInZone(int x, int y);
	
	/** Check if point is in zone **/
	public boolean checkIfInZone(int x, int y, int z);
	
	/** Check if zone intersects rectangle or rectangle is in zone **/
	public boolean intersectsRectangle(int cx, int cy, int dx, int dy);
	
	/** Get random point in zone **/
	public Location getRandomLocation();
	
	/** Calculate distance from x,y point to center of planar zone rectangle **/
	public double getZoneDistance(int x, int y);
	
	/** Calculate distance from x,y,z point to center of zone rectangle **/
	public double getZoneDistance(int x, int y, int z);
	
	/** Get Zone internal Id **/
	public int getId();
	
	/** Get zone settings **/
	public StatsSet getSettings();
	
	/** Get zone name **/
	public String getZoneName();
	
	/** Get zone type **/
	public ZoneType getZoneType();
	
	/** Get zone points **/
	public FastList<Point3D> getPoints();
	
	/** Get left bottom point of zone rectangle **/
	public Point3D getMin();
	
	/** Get right top point of zone rectangle **/
	public Point3D getMax();
	
	/** Get in-zone character list **/
	public Set<L2Character> getCharacters();
	
	/** Revalidate in-zone character **/
	public void revalidateInZone(L2Character character);
	
	/** Remove character from zone' character list **/
	public void removeFromZone(L2Character character);
}

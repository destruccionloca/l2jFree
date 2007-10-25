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

import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.tools.geometry.Point3D;
/**
 * @author G1ta0
 * This class is base class for ingame Zone
 */

public abstract class ZoneBase implements IZone
{

	private Point3D _min;
	private Point3D _max;
	FastList<Point3D> _points2D;
	private Map<RestartType, FastList<Point3D> > _restarts;
	private int _id;
	private int _castleId;
	private int _townId;
	private ZoneType _zoneType;
	private String _zoneName;

	public ZoneBase()
	{

	}

	public ZoneBase(int id, String zoneName, ZoneType zoneType)
	{
		setId(id);
		setZoneType(zoneType);
		setZoneName(zoneName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#addPoint(net.sf.l2j.gameserver.model.Location)
	 */
	public void addPoint(Point3D point)
	{
		if (getMin() == null)
			setMin(point);
		if (getMax() == null)
			setMax(point);

		setMax(new Point3D(Math.max(point.getX(), getMax().getX()), Math.max(point.getY(), getMax().getY()), Math.max(point
				.getZ(), getMax().getZ())));
		setMin(new Point3D(Math.min(point.getX(), getMin().getX()), Math.min(point.getY(), getMin().getY()), Math.min(point
				.getZ(), getMin().getZ())));

		if (_points2D == null) _points2D = new FastList<Point3D>();
		getPoints().add(point);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#addRestartPoint(net.sf.l2j.gameserver.model.zone.IZone.RestartType,
	 *      net.sf.l2j.gameserver.model.Location)
	 */
	public void addRestartPoint(RestartType restartType, Point3D point)
	{
		if (_restarts == null)
			_restarts = new FastMap<RestartType, FastList<Point3D>>();
		
		if (_restarts.get(restartType) == null)
			_restarts.put(restartType, new FastList<Point3D>());
		
		_restarts.get(restartType).add(point);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#checkIfInZone(net.sf.l2j.gameserver.model.L2Object)
	 */
	public abstract boolean checkIfInZone(L2Object obj);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#checkIfInZone(int, int)
	 */
	public abstract boolean checkIfInZone(int x, int y);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#checkIfInZone(int, int, int)
	 */
	public abstract boolean checkIfInZone(int x, int y, int z);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getId()
	 */
	public int getId()
	{
		return _id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getMax()
	 */
	public Point3D getMax()
	{
		return _max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getMin()
	 */
	public Point3D getMin()
	{
		return _min;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getPoints()
	 */
	public FastList<Point3D> getPoints()
	{
		return _points2D;
	}
	
	@SuppressWarnings("unused")
	protected void setMax(Point3D point)
	{
		_max = point;
	}

	@SuppressWarnings("unused")
	protected void setMin(Point3D point)
	{
		_min = point;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getRestartPoint(net.sf.l2j.gameserver.model.zone.IZone.RestartType)
	 */
	public Location getRestartPoint(RestartType restartType)
	{
		if (restartType == RestartType.RestartRandom)
			return getRandomLocation();
		else if (_restarts != null)
		{
			if (_restarts.get(restartType) != null)
			{
				Point3D point = _restarts.get(restartType).get(Rnd.nextInt(_restarts.get(restartType).size()));
				return new Location(point.getX(), point.getY(), point.getZ());
			}
		}
		return null;
	}

	public abstract Location getRandomLocation();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneCastle()
	 */
	public int getCastleId()
	{
		return _castleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneTown()
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneDistance(int, int)
	 */
	public abstract double getZoneDistance(int x, int y);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneDistance(int, int,
	 *      int)
	 */
	public abstract double getZoneDistance(int x, int y, int z);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneName()
	 */
	public String getZoneName()
	{
		return _zoneName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getZoneType()
	 */
	public ZoneType getZoneType()
	{
		return _zoneType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setId(int)
	 */
	public void setId(int id)
	{
		_id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setZoneCastle(int)
	 */
	public void setCastleId(int castleId)
	{
		_castleId = castleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setZoneCastle(int)
	 */
	public void setTownId(int townId)
	{
		_townId = townId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setZoneName(java.lang.String)
	 */
	public void setZoneName(String zoneName)
	{
		_zoneName = zoneName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setZoneType(net.sf.l2j.gameserver.model.zone.ZoneType)
	 */
	public void setZoneType(ZoneType zoneType)
	{
		_zoneType = zoneType;
	}

}

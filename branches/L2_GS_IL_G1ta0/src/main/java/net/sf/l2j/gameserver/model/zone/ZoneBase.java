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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javolution.util.FastList;
import net.sf.l2j.gameserver.handler.IZoneHandler;
import net.sf.l2j.gameserver.handler.ZoneHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * @author G1ta0
 * This class is base class for ingame Zone
 */

public abstract class ZoneBase implements IZone
{

	private Point3D _min;
	private Point3D _max;
	private int _zMin, _zMax;
	private FastList<Point3D> _points2D;
	private Map<L2Character, IZone> _characters = Collections.synchronizedMap(new WeakHashMap<L2Character, IZone>());
	private int _id;
	private ZoneType _zoneType;
	private String _zoneName;
	private StatsSet _zoneSet;
	private IZoneHandler _zoneHandler;

	public ZoneBase()
	{

	}

	public ZoneBase(int id, String zoneName, ZoneType zoneType, StatsSet zoneSet)
	{
		setId(id);
		setZoneType(zoneType);
		setZoneName(zoneName);
		setSettings(zoneSet);
		_zoneHandler = ZoneHandler.getInstance().getZoneHandler(this);
		_points2D = new FastList<Point3D>();
	}

	public void addPoint(int x, int y)
	{
		getPoints().add(new Point3D(x, y, 0));
	}

	public void setZ(int zMin, int zMax)
	{
		_zMin = Math.min(zMin, zMax);
		_zMax = Math.max(zMin, zMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#checkIfCharacterInZone(net.sf.l2j.gameserver.model.L2Character)
	 */
	public boolean checkIfCharacterInZone(L2Character character)
	{
		return getCharacters().contains(character);
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
		if(_max == null)
		{
			_max = new Point3D(getPoints().get(0).getX(), getPoints().get(0).getY(), Math.max(_zMax, _zMin));

			for(Point3D point : getPoints())
				_max.setXYZ(Math.max(point.getX(), _max.getX()), Math.max(point.getY(), _max.getY()), Math.max(_zMax, _zMin));
		}

		return _max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getMin()
	 */
	public Point3D getMin()
	{
		if(_min == null)
		{
			_min = new Point3D(getPoints().get(0).getX(), getPoints().get(0).getY(), Math.min(_zMax, _zMin));

			for(Point3D point : getPoints())
				_min.setXYZ(Math.min(point.getX(), _min.getX()), Math.min(point.getY(), _min.getY()), Math.min(_zMax, _zMin));
		}

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

	public abstract Location getRandomLocation();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.model.zone.IZone#getSettings()
	 */
	public StatsSet getSettings()
	{
		return _zoneSet;
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
	 * @see net.sf.l2j.gameserver.model.zone.IZone#intersectsRectangle(int , int , int , int );
	 */
	public abstract boolean intersectsRectangle(int cx, int cy, int dx, int dy);
	
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

	public Set<L2Character> getCharacters()
	{
		return _characters.keySet();
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
	 * @see net.sf.l2j.gameserver.model.zone.IZone#setSettings(net.sf.l2j.gameserver.templates.StatsSet)
	 */
	public void setSettings(StatsSet zoneSet)
	{
		_zoneSet = zoneSet;
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

	public void revalidateInZone(L2Character character)
	{
		// If the object is inside the zone...
		if(checkIfInZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if(!_characters.containsKey(character))
			{
				_characters.put(character, this);
				if(_zoneHandler != null)
					_zoneHandler.onEnter(character);
			} else
				if(_zoneHandler != null)
					_zoneHandler.onMove(character);
		}
		else
			removeFromZone(character);
	}

	/**
	 * Force fully removes a character from the zone
	 * Should use during teleport / logoff
	 * @param character
	 */
	public void removeFromZone(L2Character character)
	{
		if(_characters.containsKey(character))
		{
			_characters.remove(character);
			if(_zoneHandler != null)
				_zoneHandler.onExit(character);
		}
	}

	/**
	 * Will scan the zones char list for the character
	 * @param character
	 * @return
	 */
	public boolean checkIfInZone(L2Character character)
	{
		return _characters.containsKey(character);
	}

}

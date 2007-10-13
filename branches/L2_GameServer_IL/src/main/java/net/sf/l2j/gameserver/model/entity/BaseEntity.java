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
package net.sf.l2j.gameserver.model.entity;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneSettings;
import net.sf.l2j.tools.geometry.Point3D;

public abstract class BaseEntity
{
	//protected ZoneSettings _set;
	protected FastList<L2Zone> _zones;

	private FastMap<RestartType, FastList<Point3D> > _restarts;

	protected BaseEntity()
	{
		//_set = set;
		_zones = new FastList<L2Zone>();
	}

	/**
	 * Set the settings for this zone
	 * @param settings
	 */
	/*public void setSettings(ZoneSettings settings)
	{
		_set = settings;
	}*/
	
	/**
	 * Returns the settings for this zone
	 * @param zone
	 * @return
	 */
	/*public ZoneSettings getSettings()
	{
		return _set;
	}*/
	
	public FastList<L2Zone> getZones()
	{
		return _zones;
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		for(L2Zone zone : _zones)
		{
			if(zone.isInsideZone(x, y, z))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if the given obejct is inside the zone.
	 * 
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public void addRestartPoint(RestartType restartType, Point3D point)
	{
		if (_restarts == null)
			_restarts = new FastMap<RestartType, FastList<Point3D>>();
		
		if (_restarts.get(restartType) == null)
			_restarts.put(restartType, new FastList<Point3D>());
		
		_restarts.get(restartType).add(point);
	}

	public Location getRestartPoint(RestartType restartType)
	{
		if (restartType == RestartType.RestartRandom)
		{
			return getRandomLocation();
		}
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

	public Location getSpawnLoc()
	{
		return getRestartPoint(RestartType.RestartNormal);
	}

	protected abstract void onEnter(L2Character character);
	protected abstract void onExit(L2Character character);
}

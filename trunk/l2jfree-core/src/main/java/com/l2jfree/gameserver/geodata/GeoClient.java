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
package com.l2jfree.gameserver.geodata;

import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Territory;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.tools.geometry.Point3D;
import com.l2jfree.tools.random.Rnd;

/**
 * @Author: Death
 * @Date: 23/11/2007
 * @Time: 12:13:11
 */
public class GeoClient
{
	final static Log			_log	= LogFactory.getLog(GeoClient.class.getName());

	private static GeoClient	instance;

	private transient GeoInterface	geoEngine;

	public GeoInterface getEngine()
	{
		return geoEngine;
	}

	public static GeoClient getInstance()
	{
		if (instance == null)
			try
			{
				new GeoClient();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Can't init geoclient.");
				System.exit(0);
			}
		return instance;
	}

	public GeoClient() throws RemoteException
	{
		instance = this;
		if (Config.GEODATA)
			initLocal();
		else
			initFake();

	}

	public void initFake()
	{
		_log.info("GeoData: Disabled");
	}

	public void initLocal()
	{
		_log.info("GeoData: GeoEngine Started");
		GeoEngine.loadGeo();
		geoEngine = new GeoEngine();

		for (L2DoorInstance door : DoorTable.getInstance().getDoors())
		{
			if (door.getOpen() == 1)
			{
				closeDoor(door.getPos());
				door.setGeoOpen(false);
			}
		}
	}

	public Vector<Location> checkMovement(int x, int y, int z, Location pos)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getGeoMove().checkMovement(x, y, z, pos);
		}
		return new Vector<Location>();
	}

	public boolean canSeeTarget(L2Object actor, L2Object target)
	{
		if (actor == null || target == null)
			return true;

		return canSeeTarget(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ());
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canSeeTarget(x, y, z, tx, ty, tz);
		}
		return true;
	}

	public int getHeight(int x, int y, int z)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getHeight(x, y, z);
		}
		return z;
	}

	public int getHeight(Location loc)
	{
		return getHeight(loc.getX(), loc.getY(), loc.getZ());
	}

	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveCheck(x, y, z, tx, ty);
		}
		return new Location(tx, ty, tz);
	}

	public boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canMoveToCoord(x, y, z, tx, ty, tz);
		}
		return true;
	}

	public Location moveCheckForAI(L2Object cha, L2Object target)
	{
		return moveCheckForAI(new Location(cha.getX(), cha.getY(), cha.getZ()), new Location(target.getX(), target.getY(), target.getZ()));
	}

	public Location moveCheckForAI(Location loc1, Location loc2)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveCheckForAI(loc1, loc2);
		}
		return loc2;
	}

	public short getNSWE(int x, int y, int z)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getNSWE(x, y, z);
		}
		return 15;
	}

	public boolean canMoveToCoordWithCollision(int x, int y, int z, int tx, int ty, int tz)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canMoveToCoordWithCollision(x, y, z, tx, ty, tz);
		}
		return true;
	}

	/**
	 * @param x
	 * @param y
	 *
	 * @return Geo Block Type
	 */
	public short getType(int x, int y)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getType(x, y);
		}
		return 0;
	}

	public void openDoor(L2Territory pos)
	{
		if (!Config.GEODATA && !Config.GEO_DOORS)
			return;

		geoEngine.openDoor(pos);
	}

	public void closeDoor(L2Territory pos)
	{
		if (!Config.GEODATA && !Config.GEO_DOORS)
			return;

		geoEngine.closeDoor(pos);
	}

	public static Location coordsRandomize(L2Object obj, int min, int max)
	{
		return coordsRandomize(obj.getLoc(), min, max);
	}

	public static Location coordsRandomize(L2Object obj, int radius)
	{
		return coordsRandomize(obj, 0, radius);
	}

	public static Location coordsRandomize(int x, int y, int z, int heading, int radius_min, int radius_max)
	{
		if (radius_max == 0 || radius_max < radius_min)
			return new Location(x, y, z, heading);
		int radius = Rnd.get(radius_min, radius_max);
		double angle = Rnd.nextDouble() * 2 * Math.PI;
		return new Location((int) (x + radius * Math.cos(angle)), (int) (y + radius * Math.sin(angle)), z, heading);
	}

	public static Location coordsRandomize(Location pos, int radius)
	{
		return coordsRandomize(pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), 0, radius);
	}

	public static Location coordsRandomize(Location pos, int radius_min, int radius_max)
	{
		return coordsRandomize(pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), radius_min, radius_max);
	}

	public Location findPointToStay(int x, int y, int z, int j, int k)
	{
		Location pos = new Location(x, y, z);
		for (int i = 0; i < 100; i++)
		{
			pos = coordsRandomize(x, y, z, 0, j, k);
			if (canMoveToCoord(x, y, z, pos.getX(), pos.getY(), pos.getZ()) && canMoveToCoord(pos.getX(), pos.getY(), pos.getZ(), x, y, z))
				break;
		}
		return pos;
	}

	public boolean canSeeTarget(L2Object cha, Point3D target)
	{
		//if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
			//return false;
		if (cha.getZ() >= target.getZ())
			return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
		else
			return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
	}
	
	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
	{
		return geoEngine.getSpawnHeight(x, y, zmin, zmax, spawnid);
	}
}
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
package com.l2jfree.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.instancemanager.MapRegionManager;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.mapregion.L2MapRegion;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;

public final class DoorTable
{
	private static final Log _log = LogFactory.getLog(DoorTable.class);

	public static DoorTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, L2DoorInstance> _doors = new FastMap<Integer, L2DoorInstance>();

	private DoorTable()
	{
		reloadAll();
	}

	public void reloadAll()
	{
		_doorArray = null;
		_doors.clear();

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(new File(Config.DATAPACK_ROOT, "data/door.csv")));

			for (String line; (line = br.readLine()) != null;)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				final L2DoorInstance door = parseLine(line);
				if (door == null)
					continue;

				putDoor(door);

				door.spawnMe(door.getX(), door.getY(), door.getZ());

				// Garden of Eva (every 7 minutes)
				if (door.getDoorName().startsWith("goe"))
					door.setAutoActionDelay(420000);

				// Tower of Insolence (every 5 minutes)
				else if (door.getDoorName().startsWith("aden_tower"))
					door.setAutoActionDelay(300000);

				/* TODO: check which are automatic
				// devils (every 5 minutes)
				else if (door.getDoorName().startsWith("pirate_isle"))
					door.setAutoActionDelay(300000);
				// Cruma Tower (every 20 minutes)
				else if (door.getDoorName().startsWith("cruma"))
					door.setAutoActionDelay(1200000);
				// Coral Garden Gate (every 15 minutes)
				else if (door.getDoorName().startsWith("Coral_garden"))
					door.setAutoActionDelay(900000);
				// Normil's cave (every 5 minutes)
				else if (door.getDoorName().startsWith("Normils_cave"))
					door.setAutoActionDelay(300000);
				// Normil's Garden (every 15 minutes)
				else if (door.getDoorName().startsWith("Normils_garden"))
					door.setAutoActionDelay(900000);
				*/
			}

			_log.info("DoorTable: Loaded " + _doors.size() + " Door Templates.");
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			IOUtils.closeQuietly(br);
		}
	}

	public void registerToClanHalls()
	{
		for (L2DoorInstance door : getDoors())
		{
			ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 700);
			if (clanhall != null)
			{
				clanhall.getDoors().add(door);
				door.setClanHall(clanhall);
			}
		}
	}

	public void setCommanderDoors()
	{
		for (L2DoorInstance door : getDoors())
		{
			if (door.getFort() != null && door.getOpen())
			{
				door.setOpen(false);
				door.setIsCommanderDoor(true);
			}
		}
	}

	public static L2DoorInstance parseLine(String line)
	{
		L2DoorInstance door = null;
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");

			String name = st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());
			int y = Integer.parseInt(st.nextToken());
			int z = Integer.parseInt(st.nextToken());
			int rangeXMin = Integer.parseInt(st.nextToken());
			int rangeYMin = Integer.parseInt(st.nextToken());
			int rangeZMin = Integer.parseInt(st.nextToken());
			int rangeXMax = Integer.parseInt(st.nextToken());
			int rangeYMax = Integer.parseInt(st.nextToken());
			int rangeZMax = Integer.parseInt(st.nextToken());
			int hp = Integer.parseInt(st.nextToken());
			int pdef = Integer.parseInt(st.nextToken());
			int mdef = Integer.parseInt(st.nextToken());
			boolean unlockable = false;
			if (st.hasMoreTokens())
				unlockable = Boolean.parseBoolean(st.nextToken());
			boolean startOpen = false;
			if (st.hasMoreTokens())
				startOpen = Boolean.parseBoolean(st.nextToken());

			if (rangeXMin > rangeXMax)
				_log.fatal("Error in door data, XMin > XMax, ID:" + id);
			if (rangeYMin > rangeYMax)
				_log.fatal("Error in door data, YMin > YMax, ID:" + id);
			if (rangeZMin > rangeZMax)
				_log.fatal("Error in door data, ZMin > ZMax, ID:" + id);

			int collisionRadius = 0; // (max) radius for movement checks
			if (rangeXMax - rangeXMin > rangeYMax - rangeYMin)
				collisionRadius = rangeYMax - rangeYMin;

			StatsSet npcDat = new StatsSet();
			npcDat.set("npcId", id);
			npcDat.set("level", 0);
			npcDat.set("jClass", "door");

			npcDat.set("baseSTR", 0);
			npcDat.set("baseCON", 0);
			npcDat.set("baseDEX", 0);
			npcDat.set("baseINT", 0);
			npcDat.set("baseWIT", 0);
			npcDat.set("baseMEN", 0);

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseAccCombat", 38);
			npcDat.set("baseEvasRate", 38);
			npcDat.set("baseCritRate", 38);

			//npcDat.set("name", "");
			npcDat.set("collision_radius", collisionRadius);
			npcDat.set("collision_height", rangeZMax - rangeZMin & 0xfff0);
			npcDat.set("fcollision_radius", collisionRadius);
			npcDat.set("fcollision_height", rangeZMax - rangeZMin & 0xfff0);
			npcDat.set("sex", "male");
			npcDat.set("type", "");
			npcDat.set("baseAtkRange", 0);
			npcDat.set("baseMpMax", 0);
			npcDat.set("baseCpMax", 0);
			npcDat.set("rewardExp", 0);
			npcDat.set("rewardSp", 0);
			npcDat.set("basePAtk", 0);
			npcDat.set("baseMAtk", 0);
			npcDat.set("basePAtkSpd", 0);
			npcDat.set("aggroRange", 0);
			npcDat.set("baseMAtkSpd", 0);
			npcDat.set("rhand", 0);
			npcDat.set("lhand", 0);
			npcDat.set("armor", 0);
			npcDat.set("baseWalkSpd", 0);
			npcDat.set("baseRunSpd", 0);
			npcDat.set("name", name);
			npcDat.set("baseHpMax", hp);
			npcDat.set("baseHpReg", 3.e-3f);
			npcDat.set("baseMpReg", 3.e-3f);
			npcDat.set("basePDef", pdef);
			npcDat.set("baseMDef", mdef);

			L2CharTemplate template = new L2CharTemplate(npcDat);
			door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
			door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
			door.setMapRegion(MapRegionManager.getInstance().getRegion(x, y, z));
			template.setCollisionRadius(Math.min(x - rangeXMin, y - rangeYMin));
			door.getStatus().setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
			door.setOpen(startOpen);
			door.getPosition().setXYZInvisible(x, y, z);

			door.setMapRegion(MapRegionManager.getInstance().getRegion(x, y));
		}
		catch (Exception e)
		{
			_log.error("Error in door data at line: " + line, e);
		}

		return door;
	}

	public L2DoorInstance getDoor(Integer id)
	{
		return _doors.get(id);
	}

	public void putDoor(L2DoorInstance door)
	{
		_doorArray = null;
		_doors.put(door.getDoorId(), door);
	}

	private L2DoorInstance[] _doorArray;

	public L2DoorInstance[] getDoors()
	{
		if (_doorArray == null)
			_doorArray = _doors.values().toArray(new L2DoorInstance[_doors.size()]);

		return _doorArray;
	}

	public boolean checkIfDoorsBetween(Node start, Node end, int instanceId)
	{
		return checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), instanceId);
	}

	public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		L2MapRegion region = MapRegionManager.getInstance().getRegion(x, y, z);

		final L2DoorInstance[] allDoors;
		if (instanceId > 0)
			allDoors = InstanceManager.getInstance().getInstance(instanceId).getDoors();
		else
			allDoors = getDoors();

		// there are quite many doors, maybe they should be splitted
		for (L2DoorInstance doorInst : allDoors)
		{
			if (doorInst.getMapRegion() != region)
				continue;

			if (doorInst.getXMax() == 0)
				continue;

			// line segment goes through box
			// first basic checks to stop most calculations short
			// phase 1, x
			if ((x <= doorInst.getXMax() && tx >= doorInst.getXMin()) || (tx <= doorInst.getXMax() && x >= doorInst.getXMin()))
			{
				//phase 2, y
				if ((y <= doorInst.getYMax() && ty >= doorInst.getYMin()) || (ty <= doorInst.getYMax() && y >= doorInst.getYMin()))
				{
					// phase 3, basically only z remains but now we calculate it with another formula (by rage)
					// in some cases the direct line check (only) in the beginning isn't sufficient,
					// when char z changes a lot along the path
					if (doorInst.getStatus().getCurrentHp() > 0 && !doorInst.getOpen())
					{
						int px1 = doorInst.getXMin();
						int py1 = doorInst.getYMin();
						int pz1 = doorInst.getZMin();
						int px2 = doorInst.getXMax();
						int py2 = doorInst.getYMax();
						int pz2 = doorInst.getZMax();

						int l = tx - x;
						int m = ty - y;
						int n = tz - z;

						int dk;

						if ((dk = (doorInst.getA() * l + doorInst.getB() * m + doorInst.getC() * n)) == 0) continue; // Parallel

						float p = (float)(doorInst.getA() * x + doorInst.getB() * y + doorInst.getC() * z + doorInst.getD()) / (float)dk;

						int fx = (int)(x - l * p);
						int fy = (int)(y - m * p);
						int fz = (int)(z - n * p);

						if ((Math.min(x, tx) <= fx && fx <= Math.max(x, tx))
							&& (Math.min(y, ty) <= fy && fy <= Math.max(y, ty))
							&& (Math.min(z, tz) <= fz && fz <= Math.max(z, tz)))
						{

							if (((fx >= px1 && fx <= px2) || (fx >= px2 && fx <= px1))
								&& ((fy >= py1 && fy <= py2) || (fy >= py2 && fy <= py1))
								&& ((fz >= pz1 && fz <= pz2) || (fz >= pz2 && fz <= pz1)))
								return true; // Door between
						}
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DoorTable _instance = new DoorTable();
	}
}

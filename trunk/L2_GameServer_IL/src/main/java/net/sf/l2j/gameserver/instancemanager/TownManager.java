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
package net.sf.l2j.gameserver.instancemanager;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TownManager
{
	protected static Log _log = LogFactory.getLog(TownManager.class.getName());

	private static TownManager _instance;

	public static final TownManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing TownManager");
			_instance = new TownManager();
			_instance.load();
		}
		return _instance;
	}

	private FastMap<Integer, Town> _towns;

	public TownManager()
	{
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(L2Object obj)
	{
		return (getTown(obj) != null);
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(int x, int y, int z)
	{
		return (getTown(x, y, z) != null);
	}

	public void reload()
	{
		getTowns().clear();
		load();
	}

	private final void load()
	{
		for (IZone zone : ZoneManager.getInstance().getZones(ZoneType.Peace))
		{
			if (zone.getTownId() > 0)
			{
				if (getTowns().get(zone.getTownId()) == null)
				{
					Town town = new Town(zone.getTownId());
					getTowns().put(zone.getTownId(), town);
				}
				getTowns().get(zone.getTownId()).addTerritory(zone);
			}
		}
		_log.info("TownManager: Loaded "+getTowns().size()+" towns.");
	}

	public String getTownName(int townId)
	{
		String nearestTown;

		switch (townId)
		{
		case 2:
			nearestTown = "Talking Island Village";
			break;
		case 3:
			nearestTown = "Elven Village";
			break;
		case 1:
			nearestTown = "Dark Elven Village";
			break;
		case 4:
			nearestTown = "Orc Village";
			break;
		case 6:
			nearestTown = "Dwarven Village";
			break;
		case 7:
			nearestTown = "Town of Gludio";
			break;
		case 5:
			nearestTown = "Gludin Village";
			break;
		case 8:
			nearestTown = "Town of Dion";
			break;
		case 9:
			nearestTown = "Town of Giran";
			break;
		case 10:
			nearestTown = "Town of Oren";
			break;
		case 12:
			nearestTown = "Town of Aden";
			break;
		case 11:
			nearestTown = "Hunters Village";
			break;
		/*case 20:
			nearestTown = "Giran Harbor";
			break;*/
		case 15:
			nearestTown = "Heine";
			break;
		case 14:
			nearestTown = "Rune Township";
			break;
		case 13:
			nearestTown = "Town of Goddard";
			break;
		case 17:
			nearestTown = "Town of Shuttgart";
			break; // //TODO@ (Check mapregion table)[Luno]
		case 18:
			nearestTown = "Ivory Tower";
			break;
		case 19:
			nearestTown = "Primeval Isle Wharf";
			break;
		default:
			nearestTown = "";
			break;
		}

		return nearestTown;
	}
	
	public int getRedirectTownNumber(int townId)
	{
		int redirectTownId = 9;

		switch (townId)
		{

		case 7:
			redirectTownId = 5;
			break; // Gludio => Gludin
		case 8:
			redirectTownId = 7;
			break; // Dion => Gludio
		case 9:
			redirectTownId = 11;
			break; // Giran => HV (should be Giran Harbor, but its not a zone
		// town "yet")
		case 10:
			redirectTownId = 11;
			break; // Oren => HV
		case 12:
			redirectTownId = 10;
			break; // Aden => Oren
		case 13:
			redirectTownId = 14;
			break; // Goddard => Rune
		case 14:
			redirectTownId = 13;
			break; // Rune => Goddard
		case 15:
			redirectTownId = 16;
			break; // Heine => Floran (should be Giran Harbor, but its not a
		// zone town "yet")
		case 17:
			redirectTownId = 14;
			break; // Schuttgart => Rune
		/*
		 * case 18: redirectTownId = 10; break; // Ivory Tower => Oren
		 */
		case 19:
			redirectTownId = 14;
			break; // Primeval Isle Wharf => Rune
		}

		return redirectTownId;
	}
	
	public final Town getClosestTown(L2Object activeObject)
	{
		switch (MapRegionTable.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
		case 0:
			return getTown(2); // TI
		case 1:
			return getTown(3); // Elven
		case 2:
			return getTown(1); // DE
		case 3:
			return getTown(4); // Orc
		case 4:
			return getTown(6); // Dwarven
		case 5:
			return getTown(7); // Gludio
		case 6:
			return getTown(5); // Gludin
		case 7:
			return getTown(8); // Dion
		case 8:
			return getTown(9); // Giran
		case 9:
			return getTown(10); // Oren
		case 10:
			return getTown(12); // Aden
		case 11:
			return getTown(11); // HV
		case 12:
			return getTown(16); // Floran
		case 13:
			return getTown(15); // Heine
		case 14:
			return getTown(14); // Rune
		case 15:
			return getTown(13); // Goddard
		case 16:
			return getTown(17); // Schuttgart
			/*
			 * case 17: return getTown(18); // Ivory Tower
			 */
		case 18:
			return getTown(19); // Prime Isle Wharf
		}

		return getTown(16); // Default to floran
	}

	public final boolean townHasCastleInSeige(int townId)
	{
		int castleIndex = getTown(townId).getCastleId();
		if (castleIndex > 0)
		{
			Castle castle = CastleManager.getInstance().getCastles().get(castleIndex);
			if (castle != null)
				return castle.getSiege().getIsInProgress();
		}
		return false;
	}

	public final boolean townHasCastleInSeige(int x, int y)
	{
		return townHasCastleInSeige(getTown(MapRegionTable.getInstance().getMapRegion(x, y)).getTownId());		
	}

	public final Town getTown(int townId)
	{
		return getTowns().get(townId);
	}

	public final Town getTown(L2Object activeObject)
	{
		return getTown(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Town getTown(int x, int y, int z)
	{
		for (Town town : getTowns().values())
		{
			if (town != null && town.checkIfInZone(x, y, z))
				return town;
		}
		return null;
	}

	public final FastMap<Integer, Town> getTowns()
	{
		if (_towns == null)
			_towns = new FastMap<Integer, Town>();
		return _towns;
	}

}

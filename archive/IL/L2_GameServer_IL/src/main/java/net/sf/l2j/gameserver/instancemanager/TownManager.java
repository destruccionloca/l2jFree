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

import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegion;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegionRestart;
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
            _instance = new TownManager();
            _instance.load();
        }
        return _instance;
    }

    private FastMap<Integer, Town> _towns;

    public TownManager() {}

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
        // go through all world zones and search for town zones
        for (short region : ZoneManager.getInstance().getZoneMap().keySet())
            for (Map.Entry<ZoneType, FastList<IZone>> zt : ZoneManager.getInstance().getZoneMap().get(region).entrySet())
                for (IZone zone : zt.getValue())
                {
                    if (zone.getTownId() > -1)
                    {
                        if (getTowns().get(zone.getTownId()) == null)
                        {
                            Town town = new Town(zone.getTownId(), zone);
                            getTowns().put(zone.getTownId(), town);
                            
                            if (town.getMapRegion() != null)
                            	_log.info("TownManager: Town "+town.getName()+" was assigned to RestartId "+town.getMapRegion().getRestartId());
                        }
                        getTowns().get(zone.getTownId()).addTerritory(zone);
                    }
                }
        _log.info("TownManager: Loaded " + getTowns().size() + " towns.");
    }

    public String getTownName(int townId)
    {
        String nearestTown;

        switch (townId)
        {
	        case 0:
	            nearestTown = "Talking Island Village";
	            break;
	        case 1:
	            nearestTown = "Elven Village";
	            break;
	        case 2:
	            nearestTown = "Dark Elven Village";
	            break;
	        case 3:
	            nearestTown = "Orc Village";
	            break;
	        case 4:
	            nearestTown = "Dwarven Village";
	            break;
	        case 5:
	            nearestTown = "Town of Gludio";
	            break;
	        case 6:
	            nearestTown = "Gludin Village";
	            break;
	        case 7:
	            nearestTown = "Town of Dion";
	            break;
	        case 8:
	            nearestTown = "Town of Giran";
	            break;
	        case 9:
	            nearestTown = "Town of Oren";
	            break;
	        case 10:
	            nearestTown = "Town of Aden";
	            break;
	        case 11:
	            nearestTown = "Hunters Village";
	            break;
	        case 12: 
	        	nearestTown = "Giran Harbor"; 
	        	break;
	        case 13:
	            nearestTown = "Heine";
	            break;
	        case 14:
	            nearestTown = "Rune Township";
	            break;
	        case 15:
	            nearestTown = "Town of Goddard";
	            break;
	        case 16:
	            nearestTown = "Town of Shuttgart";
	            break;
	        case 17:
	            nearestTown = "Dimensional Gap";
	            break;
	        case 18:
	            nearestTown = "Primeval Isle Wharf";
	            break;
	        case 19:
	        	nearestTown = "Floran Village";
	        	break;
	        default:
	            nearestTown = "";
	            break;
        }

        return nearestTown;
    }

    public int getRedirectTownNumber(int townId)
    {
        int redirectTownId = 8;

        switch (townId)
        {
	        case 5:
	            redirectTownId = 6;
	            break; // Gludio => Gludin
	        case 7:
	            redirectTownId = 5;
	            break; // Dion => Gludio
	        case 8:
	            redirectTownId = 12;
	            break; // Giran => Giran Harbor
	        case 9:
	            redirectTownId = 11;
	            break; // Oren => HV
	        case 10:
	            redirectTownId = 9;
	            break; // Aden => Oren
	        case 15:
	            redirectTownId = 14;
	            break; // Goddard => Rune
	        case 14:
	            redirectTownId = 15;
	            break; // Rune => Goddard
	        case 13:
	            redirectTownId = 12;
	            break; // Heine => Giran Harbor
	        case 16:
	            redirectTownId = 14;
	            break; // Schuttgart => Rune
	        case 17: 
	        	redirectTownId = 9; 
	        	break; // Ivory Tower => Oren
	        case 18:
	            redirectTownId = 14;
	            break; // Primeval Isle Wharf => Rune
        }

        return redirectTownId;
    }

    public final Town getClosestTown(L2Object activeObject)
    {
        return getClosestTown(activeObject.getPosition().getX(), activeObject.getPosition().getY(), activeObject.getPosition().getZ());
    }

    public final Town getClosestTown(int x, int y, int z)
    {
    	L2MapRegion region = MapRegionManager.getInstance().getRegion(x, y, z);
    	Town town = getTown(Config.ALT_DEFAULT_RESTARTTOWN);
    	
    	if (region != null)
    	{
    		L2MapRegionRestart restart = MapRegionManager.getInstance().getRestartLocation(region.getRestartId());
    		
    		if (restart != null)
    			switch (restart.getBbsId())
    			{
    				case 1: //Talking Island
    					return getTown(0);
    				case 2: //Gludin
    					return getTown(6);
    				case 3: //Darkelfen Village
    					return getTown(2);
    				case 4: //Elfen Village
    					return getTown(1);
    				case 5: //Dion
    					return getTown(7);
    				case 6: //Giran
    					return getTown(12);
    				case 7: //Dimensional Gap
    					return getTown(17); 
    				case 8: //Orc Village
    					return getTown(3);
    				case 9: //Dwarfen Village
    					return getTown(4);
    				case 10: //Oren Villag
    					return getTown(9);
    				case 11: //Hunters Village
    					return getTown(11);
    				case 12: //Heine
    					return getTown(13);
    				case 13: //Aden
    					return getTown(10);
    				case 14: //Rune
    					return getTown(14);
    				case 15: //Goddard
    					return getTown(15);
    				case 25: //Schuttgart - FIXME
    					return getTown(16);
    			}
    	}
    	
    	return town;
    }

    public final boolean townHasCastleInSiege(int townId)
    {
    	if (getTown(townId) == null)
    		return false;
    	
        int castleIndex = getTown(townId).getCastleId();
        if (castleIndex > 0)
        {
            Castle castle = CastleManager.getInstance().getCastles().get(castleIndex);
            if (castle != null)
                return castle.getSiege().getIsInProgress();
        }
        return false;
    }

    public final boolean townHasCastleInSiege(int x, int y, int z)
    {
        return townHasCastleInSiege(getClosestTown(x, y, z).getTownId());
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

    public String getClosestTownName(L2Character activeChar)
    {
    	return getTownName(getClosestTown(activeChar).getTownId());
    }
}

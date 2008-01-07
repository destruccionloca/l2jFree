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
package net.sf.l2j.gameserver.model.entity;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegion;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.tools.geometry.Point3D;

public class Town
{
	private FastList<IZone> _territory;
	private L2MapRegion _region;
	private int _townId;
	private int _castleId;
	
	public Town(int townId, IZone zone)
	{
		_townId = townId;
		_castleId = 0;
		_territory = new FastList<IZone>();
		
		_region = findMapRegion(zone);
	}

	/** Return true if object is inside the zone */
	public boolean checkIfInZone(L2Object obj)
	{
		return checkIfInZone(obj.getX(), obj.getY(), obj.getZ());
	}

	/** Return true if object is inside the zone */
	public boolean checkIfInZone(int x, int y, int z)
	{
		for(IZone zone: getTerritory())
			if (zone.checkIfInZone(x, y, z)) return true;
		return false;
	}

	public final Castle getCastle()
	{
		return CastleManager.getInstance().getCastles().get(getCastleId());
	}
	
	public final String getName()
	{
		return TownManager.getInstance().getTownName(getTownId());
	}
	
	public final Location getSpawn()
	{
		Town town = this;
		
        // If a redirect to town id is avail, town belongs to a castle,
		// and castle is under siege then redirect		
		if (TownManager.getInstance().townHasCastleInSiege(getTownId()))
			town = TownManager.getInstance().getTown(TownManager.getInstance().getRedirectTownNumber(getTownId()));
		
		Location loc = null;
		for(IZone zone: town.getTerritory())
		{
			loc = zone.getRestartPoint(RestartType.RestartNormal);
			if (loc != null) break;
		}
		return loc;
	}

	public final Location getKarmaSpawn()
	{
		Town town = this;
		
        // If a redirect to town id is avail, town belongs to a castle,
		// and castle is under siege then redirect
		if (TownManager.getInstance().townHasCastleInSiege(getTownId()))
			town = TownManager.getInstance().getTown(TownManager.getInstance().getRedirectTownNumber(getTownId()));
		
		Location loc = null;
		for(IZone zone: town.getTerritory())
		{
			loc = zone.getRestartPoint(RestartType.RestartChaotic);
			if (loc != null) break;
		}
		return loc;
	}
	
	public final int getTownId()
	{
		return _townId;
	}

	public final int getCastleId()
	{
		return _castleId;
	}

	public final boolean isInPeace()
	{
        if (Config.ZONE_TOWN == 2) return false;
		if (Config.ZONE_TOWN == 1 && 
			TownManager.getInstance().townHasCastleInSiege(getTownId())) return false;
           
		return true;
	}
	
	public final void addTerritory(IZone zone)
	{
        if (zone.getCastleId() > 0) 
		_castleId = zone.getCastleId();
		getTerritory().add(zone);
	}
	
	public final FastList<IZone> getTerritory()
	{
		return _territory;
	}
	
	public L2MapRegion getMapRegion()
	{
		return _region;
	}
	
	private L2MapRegion findMapRegion(IZone zone)
	{
		int middleX = 0;
		int middleY = 0;
		
		for (Point3D point : zone.getPoints())
		{
			middleX += point.getX();
			middleY += point.getY();
		}
		
		middleX /= zone.getPoints().size();
		middleY /= zone.getPoints().size();

		L2MapRegion region = MapRegionManager.getInstance().getRegion(middleX, middleY);
		
		return region;
	}
}
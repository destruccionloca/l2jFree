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
package net.sf.l2j.gameserver.model.entity;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegion;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.tools.geometry.Point3D;

public class Town extends Entity
{
	private L2MapRegion _region;
	private int _townId;

	@Override
	public void registerZone(L2Zone zone)
	{
		_zone = zone;
		_region = findMapRegion();
	}

	public final Castle getCastle()
	{
		return CastleManager.getInstance().getCastles().get(getCastleId());
	}
	
	public final String getName()
	{
		return TownManager.getInstance().getTownName(getTownId());
	}

	@Override
	public boolean checkBanish(L2PcInstance player)
	{
		return false;
	}
	
	/*public final Location getSpawn()
	{
		Town town = this;
		
        // If a redirect to town id is avail, town belongs to a castle,
		// and castle is under siege then redirect		
		if (TownManager.getInstance().townHasCastleInSiege(getTownId()))
			town = TownManager.getInstance().getTown(TownManager.getInstance().getRedirectTownNumber(getTownId()));
		
		Location loc = null;
		for(L2Zone zone : town.getTerritory())
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
		for(L2Zone zone: town.getTerritory())
		{
			loc = zone.getRestartPoint(RestartType.RestartChaotic);
			if (loc != null) break;
		}
		return loc;
	}*/

	public final boolean isInPeace()
	{
        if (Config.ZONE_TOWN == 2) return false;
		if (Config.ZONE_TOWN == 1 && 
			TownManager.getInstance().townHasCastleInSiege(getTownId())) return false;
           
		return true;
	}
	
	public L2MapRegion getMapRegion()
	{
		return _region;
	}
	
	private L2MapRegion findMapRegion()
	{
		int middleX = _zone.getMiddleX();
		int middleY = _zone.getMiddleY();

		L2MapRegion region = MapRegionManager.getInstance().getRegion(middleX, middleY);
		
		return region;
	}
}
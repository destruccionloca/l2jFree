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

	public Town(L2Zone zone)
	{
		_region = findMapRegion(zone);
	}

	public final Castle getCastle()
	{
		return CastleManager.getInstance().getCastles().get(getCastleId());
	}
	
	public final String getName()
	{
		return TownManager.getInstance().getTownName(getTownId());
	}

	public L2MapRegion getMapRegion()
	{
		return _region;
	}
	
	private L2MapRegion findMapRegion(L2Zone zone)
	{
		int middleX = zone.getMiddleX();
		int middleY = zone.getMiddleY();

		L2MapRegion region = MapRegionManager.getInstance().getRegion(middleX, middleY);
		
		return region;
	}

	public boolean hasCastleInSiege()
	{
		if (getCastleId() < 1)
			return false;

		Castle castle = CastleManager.getInstance().getCastles().get(getCastleId());
		if (castle == null)
			return false;

		return castle.getSiege().getIsInProgress();
	}
}

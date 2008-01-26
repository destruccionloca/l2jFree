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
package net.sf.l2j.gameserver.instancemanager;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.L2FishingZone;
import net.sf.l2j.gameserver.model.zone.L2WaterZone;

public class FishingZoneManager
{
	// =========================================================
	private static FishingZoneManager _instance;
	public static final FishingZoneManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FishingZoneManager();
		}
		return _instance;
	}
	// =========================================================


	// =========================================================
	// Data Field
	private FastList<L2Zone> _zones;

	// =========================================================
	// Property - Public

	public void addZone(L2Zone zone)
	{
		if (_zones == null)
			_zones = new FastList<L2Zone>();

		_zones.add(zone);
	}

	public final L2FishingZone isInsideFishingZone(int x, int y)
	{
		for (L2Zone temp : _zones)
			if (temp instanceof L2FishingZone && temp.isInsideZone(x, y))
				return (L2FishingZone)temp;
		return null;
	}

	public final L2WaterZone isInsideWaterZone(int x, int y)
	{
		for (L2Zone temp : _zones)
			if (temp instanceof L2WaterZone && temp.isInsideZone(x, y))
				return (L2WaterZone)temp;
		return null;
	}
}

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
package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.zone.IZone;
import javolution.util.FastList;

import net.sf.l2j.gameserver.model.L2Character;

/**
 * This class manages all zones for a given world region
 *
 * @author  durgus
 */
public class L2ZoneManager
{
	private FastList<IZone> _zones;
	
	/**
	 * The Constructor creates an initial zone list
	 * use addZone() / removeZone() to
	 * change the zone list
	 *
	 */
	public L2ZoneManager()
	{
		_zones = new FastList<IZone>();
	}
	
	public FastList<IZone> getZones()
	{
		return _zones;
	}
	/**
	 * Register a new zone object into the manager
	 * @param zone
	 */
	public void addZone(IZone zone)
	{
		if (!getZones().contains(zone))
			getZones().add(zone);
	}
	
	/**
	 * Unregister a given zone from the manager (e.g. dynamic zones)
	 * @param zone
	 */
	public void removeZone(IZone zone)
	{
		_zones.remove(zone);
	}
	
	public void revalidateZones(L2Character character)
	{
		for (IZone zone: getZones())
		{
			zone.revalidateInZone(character);
		}
	}
	
	public void removeCharacter(L2Character character)
	{
		for (IZone zone: getZones())
		{
			zone.removeFromZone(character);
		}
	}
}

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

import javolution.util.FastMap;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Fortress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FortressManager
{
	protected static Log _log = LogFactory.getLog(FortressManager.class.getName());

	private static FortressManager _instance;
	private FastMap<Integer, Fortress> _fortresses;
	
	public static final FortressManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortressManager();
		}
		return _instance;
	}

	public final Fortress getClosestFortress(L2Object activeObject)
	{
		Fortress fort = getFortress(activeObject);
		if (fort == null)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;
			
			for (Fortress temp : getFortresses().values())
			{
				if (temp  == null)
					continue;
				distance = temp.getDistanceToZone(activeObject.getX(), activeObject.getY());
				if (closestDistance > distance)
				{
					closestDistance = distance;
					fort = temp;
				}
			}
		}
		return fort;
	}

	public final Fortress getFortressById(int fortId)
	{
		return getFortresses().get(fortId);
	}

	public final Fortress getFortress(L2Object activeObject)
	{
		return getFortress(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Fortress getFortress(int x, int y, int z)
	{
		Fortress fort;
		for (int i = 1; i <= getFortresses().size(); i++)
		{
			fort = getFortresses().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
				return fort;
		}
		return null;
	}

	public final Fortress getFortressByName(String name)
	{
		Fortress fort;
		for (int i = 1; i <= getFortresses().size(); i++)
		{
			fort = getFortresses().get(i);
			if (fort != null && fort.getName().equalsIgnoreCase(name.trim()))
				return fort;
		}
		return null;
	}

	public final Fortress getFortressByOwner(L2Clan clan)
	{
		if (clan == null)
			return null;

		Fortress fort;
		for (int i = 1; i <= getFortresses().size(); i++)
		{
			fort = getFortresses().get(i);
			if (fort != null && fort.getOwnerId() == clan.getClanId())
				return fort;
		}
		return null;
	}

	public final FastMap<Integer, Fortress> getFortresses()
	{
		if (_fortresses == null)
			_fortresses = new FastMap<Integer, Fortress>();
		return _fortresses;
	}
}
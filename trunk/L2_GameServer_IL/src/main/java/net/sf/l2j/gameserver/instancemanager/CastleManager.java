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
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CastleManager
{
	protected static Log _log = LogFactory.getLog(CastleManager.class.getName());

	private static CastleManager _instance;
	private FastMap<Integer, Castle> _castles;
	
	public static final CastleManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing CastleManager");
			_instance = new CastleManager();
			_instance.load();
		}
		return _instance;
	}

	public CastleManager()
	{
	}

	public final Castle getClosestCastle(L2Object activeObject)
	{
		Castle castle = getCastle(activeObject);
		if (castle == null)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;
			
			for (Castle castle_check : getCastles().values())
			{
				if (castle_check  == null)
					continue;
				distance = castle_check.getZone().getZoneDistance(activeObject.getX(), activeObject.getY());
				if (closestDistance > distance)
				{
					closestDistance = distance;
					castle = castle_check;
				}
			}
		}
		return castle;
	}

	public void reload()
	{
		getCastles().clear();
		load();
	}

	private final void load()
	{
		for (IZone zone : ZoneManager.getInstance().getZones(ZoneType.CastleArea))
			if (zone != null)
				getCastles().put(zone.getId(), new Castle(zone.getId()));
		_log.info("Loaded: " + getCastles().size() + " castles.");
	}

	public final Castle getCastleById(int castleId)
	{
		return getCastles().get(castleId);
	}

	public final Castle getCastle(L2Object activeObject)
	{
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Castle getCastle(int x, int y, int z)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZone(x, y, z))
				return castle;
		}
		return null;
	}

	public final Castle getCastleByName(String name)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getName().equalsIgnoreCase(name.trim()))
				return castle;
		}
		return null;
	}

	public final Castle getCastleByOwner(L2Clan clan)
	{
		if (clan == null)
			return null;

		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getOwnerId() == clan.getClanId())
				return castle;
		}
		return null;
	}
	/*
	public final Castle getCastleByTown(int x, int y, int z)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZoneTowns(x, y, z))
				return castle;
		}
		return null;
	}
	*/
	public final FastMap<Integer, Castle> getCastles()
	{
		if (_castles == null)
			_castles = new FastMap<Integer, Castle>();
		return _castles;
	}

	public final void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		switch (sealStrifeOwner)
		{
		case SevenSigns.CABAL_DUSK:
			maxTax = 5;
			break;
		case SevenSigns.CABAL_DAWN:
			maxTax = 25;
			break;
		default: // no owner
			maxTax = 15;
			break;
		}

		for (Castle castle : _castles.values())
			if (castle.getTaxPercent() > maxTax)
				castle.setTaxPercent(maxTax);
	}
}
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;

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

	public CastleManager() {}

    public final int findNearestCastleIndex(L2Object obj)
    {
        int index = getCastleIndex(obj);
        if (index < 0)
        {
            double closestDistance = 99999999;
            double distance;
            Castle castle;
            for (int i = 0; i < getCastles().size(); i++)
            {
                castle = getCastles().get(i);
                if (castle == null) continue;
                distance = castle.getDistance(obj);
                if (closestDistance > distance)
                {
                    closestDistance = distance;
                    index = i;
                }
            }
        }
        return index;
    }

	public final Castle getNearestCastle(L2Object obj)
	{
		int index = findNearestCastleIndex(obj);
		if(index <= 0) return null;
		
		return _castles.get(index);
	}

    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;
            con = L2DatabaseFactory.getInstance().getConnection(con);
            statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
            rs = statement.executeQuery();

            while (rs.next())
            {
				int id = rs.getInt("id");
                getCastles().put(id, new Castle(id));
            }

            statement.close();
            _log.info("Loaded: " + getCastles().size() + " castles");
        }
        catch (Exception e)
        {
            _log.info("Exception: CastleManager.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
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

    public final int getCastleIndex(int castleId)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getCastleId() == castleId) return i;
        }
        return -1;
    }

    public final int getCastleIndex(L2Object activeObject)
    {
    	return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
    }

    public final int getCastleIndex(int x, int y, int z)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.checkIfInZone(x, y, z)) return i;
        }
        return -1;
    }

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

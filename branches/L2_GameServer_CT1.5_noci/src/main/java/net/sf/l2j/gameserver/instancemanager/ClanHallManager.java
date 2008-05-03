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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.ClanHall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClanHallManager
{
	protected static Log _log = LogFactory.getLog(ClanHallManager.class.getName());
	
	private static ClanHallManager _instance;
	
	private Map<Integer, ClanHall> _clanHall;
	private Map<Integer, ClanHall> _freeClanHall;
	private Map<Integer, ClanHall> _allClanHalls;
	private boolean _loaded = false;

	public static ClanHallManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanHallManager();
		}
		return _instance;
	}

	public boolean loaded()
	{
		return _loaded;
	}

	private ClanHallManager()
	{
		_clanHall = new FastMap<Integer, ClanHall>();
		_freeClanHall = new FastMap<Integer, ClanHall>();
		_allClanHalls = new FastMap<Integer, ClanHall>();
		load();
	}

	/** Reload All Clan Hall */
	public final void reload()
	{
		_clanHall.clear();
		_freeClanHall.clear();
		_allClanHalls.clear();
		load();
	}

	/** Load All Clan Hall */
	private final void load()
	{
		java.sql.Connection con = null;
		try
		{
			int id, ownerId, lease, grade = 0;
			String Name, Desc, Location;
			long paidUntil = 0;
			boolean paid = false;

			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("id");
				Name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				Desc = rs.getString("desc");
				Location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");

				ClanHall ch = new ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid);
				if(ownerId == 0)
				{
					_freeClanHall.put(id, ch);
				}
				else
				{
					if(ClanTable.getInstance().getClan(rs.getInt("ownerId")) != null)
					{
						_clanHall.put(id, ch);
						ClanTable.getInstance().getClan(rs.getInt("ownerId")).setHasHideout(id);
					}
					else
					{
						_freeClanHall.put(id, ch);
						_freeClanHall.get(id).free();
						AuctionManager.getInstance().initNPC(id);
					}
				}
				_allClanHalls.put(id, ch);
			}
			statement.close();
			_log.info("ClanHallManager: loaded "+getClanHalls().size() +" clan halls");
			_log.info("ClanHallManager: loaded "+getFreeClanHalls().size() +" free clan halls");
			_loaded = true;
		}
		catch (Exception e)
		{
			_log.fatal("Exception: ClanHallManager.load(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e) {}
		}
	}

	/** Get Map with all FreeClanHalls */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}

	/** Get Map with all ClanHalls that have owner*/
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}

	/** Get Map with all ClanHalls*/
	public final Map<Integer, ClanHall> getAllClanHalls()
	{
		return _allClanHalls;
	}

	/** Check is free ClanHall */
	public final boolean isFree(int chId)
	{
		if(_freeClanHall.containsKey(chId))
			return true;
		return false;
	}

	/** Free a ClanHall */
	public final synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId,_clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}

	/** Set ClanHallOwner */
	public final synchronized void setOwner(int chId, L2Clan clan)
	{
		if(!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId,_freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
			_clanHall.get(chId).free();
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(L2Object obj)
	{
		return (getClanHall(obj) != null);
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(int x, int y)
	{
		return (getClanHall(x, y) != null);
	}

	/** Get Clan Hall by Id */
	public final ClanHall getClanHallById(int clanHallId)
	{
		return _allClanHalls.get(clanHallId);
	}

	/** Get Clan Hall by Object */
	public final ClanHall getClanHall(L2Object activeObject) 
	{ 
		return getClanHall(activeObject.getPosition().getX(), activeObject.getPosition().getY()); 
	}

	/** Get Clan Hall by region x,y,offset */
	public final ClanHall getClanHall(int x, int y)
	{
		for (Map.Entry<Integer, ClanHall> ch : _allClanHalls.entrySet())
			if (ch.getValue().checkIfInZone(x, y)) return ch.getValue();

		return null;
	}

	/** Get Clan Hall by name */
	public final ClanHall getClanHall(String name)
	{
		for (Map.Entry<Integer, ClanHall> ch : _allClanHalls.entrySet())
			if (ch.getValue().getName().equals(name)) return ch.getValue();

		return null;
	}

	/** Get Clan Hall by Owner */
	public final ClanHall getClanHallByOwner(L2Clan clan)
	{
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
			if (clan.getClanId() == ch.getValue().getOwnerId())
				return ch.getValue();
		return null;
	}

	public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{
		for (Map.Entry<Integer, ClanHall> ch : _allClanHalls.entrySet())
			if (ch.getValue().getDistanceToZone(x, y) < maxDist) return ch.getValue();

		return null;
	}
}

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
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.entity.Fort;

public class FortManager
{
	protected static final Log	_log	= LogFactory.getLog(FortManager.class.getName());
	private static FortManager	_instance;

	public static final FortManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing FortManager");
			_instance = new FortManager();
			_instance.load();
		}
		return _instance;
	}

	// =========================================================
	// Data Field
	private List<Fort>	_forts;

	// =========================================================
	// Constructor
	public FortManager()
	{
	}

	// =========================================================
	// Method - Public

	public final int findNearestFortIndex(L2Object obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;

			for (Fort fort : getForts())
			{
				if (fort == null)
					continue;
				distance = fort.getDistanceToZone(obj.getX(), obj.getY());
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = getFortIndex(fort.getFortId());
				}
			}
		}
		return index;
	}

	// =========================================================
	// Method - Private
	private final void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			statement = con.prepareStatement("SELECT id FROM fort ORDER BY id");
			rs = statement.executeQuery();

			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}

			rs.close();
			statement.close();

			_log.info("Loaded: " + getForts().size() + " fortress");
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortData(): " + e.getMessage());
		}
        finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
	}

	// =========================================================
	// Property - Public
	public final Fort getFortById(int fortId)
	{
		for (Fort f : getForts())
		{
			if (f.getFortId() == fortId)
				return f;
		}
		return null;
	}

	public final Fort getFortByOwner(L2Clan clan)
	{
		for (Fort f : getForts())
		{
			if (f.getOwnerId() == clan.getClanId())
				return f;
		}
		return null;
	}

	public final Fort getFort(String name)
	{
		for (Fort f : getForts())
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
				return f;
		}
		return null;
	}

	public final Fort getFort(int x, int y, int z)
	{
		for (Fort f : getForts())
		{
			if (f.checkIfInZone(x, y, z))
				return f;
		}
		return null;
	}

	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.getFortId() == fortId)
				return i;
		}
		return -1;
	}

	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
				return i;
		}
		return -1;
	}

	public final List<Fort> getForts()
	{
		if (_forts == null)
			_forts = new FastList<Fort>();
		return _forts;
	}

}
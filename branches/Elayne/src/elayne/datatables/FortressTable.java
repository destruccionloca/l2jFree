package elayne.datatables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import elayne.templates.L2Fortress;
import elayne.util.connector.ServerDB;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class FortressTable
{
	private static FortressTable 			_instance;
	private FastMap<Integer, L2Fortress> 	_fortresses = new FastMap<Integer, L2Fortress>();

	/** Returns the only instance of this class. */
	public static FortressTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortressTable();
			return _instance;
		}
		return _instance;
	}

	/** Constructor. */
	private FortressTable()
	{
		_fortresses = new FastMap<Integer, L2Fortress>();
	}

	public L2Fortress getFortress(int id)
	{
		return _fortresses.get(id);
	}

	public int getClanFort(int clanId)
	{
		for (int fortId : _fortresses.keySet())
		{
			L2Fortress fort = getFortress(fortId);
			if (fort != null)
				if (fort.getOwner() == clanId)
					return fort.getFortId();
		}
		return 0;
	}

	public void load() throws IOException
	{
		int loaded = 0;
		int free = 0;
		int taken = 0;

		Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `fort`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int id = rset.getInt("id");
				String name = rset.getString("name");
				long time = rset.getLong("lastOwnedTime");
				int owner = rset.getInt("owner");
				int type = rset.getInt("fortType");
				int state = rset.getInt("state");
				int castleId = rset.getInt("castleId");

				if (owner == 0)
					free++;
				else
					taken++;

				_fortresses.put(id, new L2Fortress(id, name, time, owner, type, state, castleId));
				loaded++;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("FortressTable: Exception while getting fortresses: " + e.toString());
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			System.out.println("FortressTable: " + loaded + " fortresses loaded successfully, " + free + " free, " + taken + " owned.");
		}
	}
}

package elayne.datatables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import elayne.templates.L2Castle;
import elayne.util.connector.ServerDB;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class CastleTable
{
	private static CastleTable 				_instance;
	private FastMap<Integer, L2Castle>	 	_castles = new FastMap<Integer, L2Castle>();

	/** Returns the only instance of this class. */
	public static CastleTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CastleTable();
			return _instance;
		}
		return _instance;
	}

	/** Constructor. */
	private CastleTable()
	{
		_castles = new FastMap<Integer, L2Castle>();
	}

	public L2Castle getCastle(int id)
	{
		return _castles.get(id);
	}

	public void load() throws IOException
	{
		int loaded = 0;

		Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, name, taxPercent, treasury, siegeDate FROM `castle`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int id = rset.getInt("id");
				String name = rset.getString("name");
				int tax = rset.getInt("taxPercent");
				int treasury = rset.getInt("treasury");
				Long siegeDate = rset.getLong("siegeDate");

				_castles.put(id, new L2Castle(id, name, tax, treasury, siegeDate));
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
			System.out.println("CastleTable: " + loaded + " castles loaded successfully");
		}
	}
}

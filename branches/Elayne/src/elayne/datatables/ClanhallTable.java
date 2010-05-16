package elayne.datatables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import elayne.templates.L2Clanhall;
import elayne.util.connector.ServerDB;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class ClanhallTable
{
	private static ClanhallTable 			_instance;
	private FastMap<Integer, L2Clanhall> 	_clanhalls = new FastMap<Integer, L2Clanhall>();

	/** Returns the only instance of this class. */
	public static ClanhallTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanhallTable();
			return _instance;
		}
		return _instance;
	}

	/** Constructor. */
	private ClanhallTable()
	{
		_clanhalls = new FastMap<Integer, L2Clanhall>();
	}

	public L2Clanhall getClanhall(int id)
	{
		return _clanhalls.get(id);
	}

	public int getClanCH(int clanId)
	{
		for (int clanhallId : _clanhalls.keySet())
		{
			L2Clanhall clanhall = getClanhall(clanhallId);
			if (clanhall != null)
				if (clanhall.getOwnerId() == clanId)
					return clanhall.getClanhallId();
		}
		return 0;
	}

	public void load() throws IOException
	{
		int loaded = 0;

		Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `clanhall`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int id = rset.getInt("id");
				String name = rset.getString("name");
				int ownerId = rset.getInt("ownerId");
				int lease = rset.getInt("lease");
				String desc = rset.getString("desc");
				String location = rset.getString("location");
				int grade = rset.getInt("grade");

				_clanhalls.put(id, new L2Clanhall(id, name, ownerId, lease, desc, location, grade));
				loaded++;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Clanhall table: Exception while getting clanhalls:" + e.toString());
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
			System.out.println("ClanhallTable: " + loaded + " clanhalls loaded correctly.");
		}
	}
}

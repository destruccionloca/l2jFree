package elayne.datatables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import elayne.util.connector.ServerDB;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class LeaderTable
{
	private static LeaderTable 				_instance;
	private FastMap<Integer, Integer>	 	_leaders = new FastMap<Integer, Integer>();

	/** Returns the only instance of this class. */
	public static LeaderTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new LeaderTable();
			return _instance;
		}
		return _instance;
	}

	/** Constructor. */
	private LeaderTable()
	{
		_leaders = new FastMap<Integer, Integer>();
	}

	public boolean isLeader(int playerId)
	{
		if (_leaders.containsKey(playerId))
			return true;
		else
			return false;
	}

	public void load() throws IOException
	{
		int loaded = 0;

		Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, leader_id FROM `clan_data`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int clanId = rset.getInt("clan_id");
				int leaderId = rset.getInt("leader_id");

				_leaders.put(leaderId, clanId);
				loaded++;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("LeaderTable: Exception while getting leaders: " + e.toString());
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
			System.out.println("LeaderTable: " + loaded + " leaders loaded successfully");
		}
	}
}

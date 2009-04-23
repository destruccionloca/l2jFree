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
package com.l2jfree.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class RecordTable
{
	private final static Log	_log				= LogFactory.getLog(RecordTable.class.getName());

	private static RecordTable	_instance;

	private int					_maxPlayer			= 0;
	private String				_strDateMaxPlayer	= null;

	/**
	* Not really useful to make an instance of recordtable because data is reloaded each time.
	* But it's quite easy to use like this.
	*/
	public static RecordTable getInstance()
	{
		if (_instance == null)
			_instance = new RecordTable();
		
		return _instance;
	}

	private RecordTable()
	{
		restoreRecordData();
	}

	/**
	 * 
	 */
	public void restoreRecordData()
	{
		Connection con = null;
		try
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("SELECT maxplayer, date FROM record ORDER by maxplayer desc limit 1");
				ResultSet recorddata = statement.executeQuery();

				fillRecordTable(recorddata);
				recorddata.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("error while creating record table " + e, e);
			}

		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void fillRecordTable(ResultSet Recorddata) throws Exception
	{
		// In fact, there is just one record
		while (Recorddata.next())
		{
			_maxPlayer = Recorddata.getInt("maxplayer");
			_strDateMaxPlayer = Recorddata.getString("date");
		}
	}

	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	public String getDateMaxPlayer()
	{
		return _strDateMaxPlayer;
	}

}

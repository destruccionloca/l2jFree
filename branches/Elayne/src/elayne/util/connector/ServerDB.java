/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package elayne.util.connector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import elayne.application.Activator;
import elayne.preferences.LoginPreferencePage;

public class ServerDB
{
	static Logger _log = Logger.getLogger(ServerDB.class.getName());

	public static enum ProviderType
	{
		MySql, MsSql
	}

	// =========================================================
	// Data Field
	private ProviderType _providerType;
	private ComboPooledDataSource _source;
	private static ServerDB _instance;

	// =========================================================
	// Constructor
	public ServerDB() throws SQLException
	{
		try
		{
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);

			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(50);

			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time cause there is a "long" delay on acquire
			// connection so taking more than one connection at once will make connection pooling more effective.
			//This "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("elayne_test_table");
			_source.setTestConnectionOnCheckin(false);
			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 seconds.
			_source.setMaxIdleTime(0); // 0 = idle connections never expire *THANKS* to connection testing configured
			// above but I prefer to disconnect all connections not used for more than 1 hour enables statement caching, there is a
			// "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);

			_source.setBreakAfterAcquireFailure(false);
			IPreferencesService service = Platform.getPreferencesService();
			String serverDbHost = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_HOST, "127.0.0.1", null);
			String serverDbName = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB, "l2jdb", null);
			String serverDbUser = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_USER, "root", null);
			String serverDbPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_PASS, "password", null);

			_source.setDriverClass("com.mysql.jdbc.Driver");
			_source.setJdbcUrl("jdbc:mysql://" + serverDbHost + "/" + serverDbName);
			_source.setUser(serverDbUser);
			_source.setPassword(serverDbPassword);

			/* Test the connection */
			_source.getConnection().close();

			_log.fine("Database Connection Working");

			if ("com.mysql.jdbc.Driver".toLowerCase().contains("microsoft"))
				_providerType = ProviderType.MsSql;
			else
				_providerType = ProviderType.MySql;
		}
		catch (SQLException x)
		{
			_log.fine("Database Connection FAILED");
			// rethrow the exception
			throw x;
		}
		catch (Exception e)
		{
			_log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection: " + e);
		}
		System.out.println("ServerDB: Connection working.");
	}

	// =========================================================
	// Method - Public
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord)
		{
			if (getProviderType() == ProviderType.MsSql)
				msSqlTop1 = " Top 1 ";
			if (getProviderType() == ProviderType.MySql)
				mySqlTop1 = " Limit 1 ";
		}
		String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
		return query;
	}

	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
	}

	public final String safetyString(String[] whatToCheck)
	{
		String braceLeft = "`";
		String braceRight = "`";
		if (getProviderType() == ProviderType.MsSql)
		{
			braceLeft = "[";
			braceRight = "]";
		}

		String result = "";
		for (String word : whatToCheck)
		{
			if (result != "")
				result += ", ";
			result += braceLeft + word + braceRight;
		}
		return result;
	}

	// =========================================================
	// Property - Public

	public static ServerDB getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new ServerDB();
		}
		return _instance;
	}

	public Connection getConnection() // throws
	// SQLException
	{
		Connection con = null;

		while (con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch (SQLException e)
			{
				_log.warning("L2DatabaseFactory: getConnection() failed, trying again " + e);
			}
		}
		return con;
	}

	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}

	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}

	public final ProviderType getProviderType()
	{
		return _providerType;
	}
}

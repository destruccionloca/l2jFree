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

public class LoginDB
{
	static Logger _log = Logger.getLogger(LoginDB.class.getName());

	public static enum ProviderType
	{
		MySql, MsSql
	}

	// =========================================================
	// Data Field
	private ProviderType _providerType;
	private ComboPooledDataSource _source;
	private static LoginDB _instance;

	// =========================================================
	// Constructor
	public LoginDB() throws SQLException
	{
		try
		{
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);

			_source.setInitialPoolSize(2);
			_source.setMinPoolSize(2);
			_source.setMaxPoolSize(25);

			_source.setAcquireRetryAttempts(0);
			_source.setAcquireRetryDelay(500);
			_source.setCheckoutTimeout(0);
			_source.setAcquireIncrement(5);
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);
			_source.setIdleConnectionTestPeriod(3600);
			_source.setMaxIdleTime(0);
			_source.setMaxStatementsPerConnection(100);
			_source.setBreakAfterAcquireFailure(false);
			IPreferencesService service = Platform.getPreferencesService();
			String loginDbHost = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_HOST, "127.0.0.1", null);
			String loginDbName = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB, "l2jdb", null);
			String loginDbUser = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_USER, "root", null);
			String loginDbPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.LOGIN_DB_PASS, "password", null);

			_source.setDriverClass("com.mysql.jdbc.Driver");
			_source.setJdbcUrl("jdbc:mysql://" + loginDbHost + "/" + loginDbName);
			_source.setUser(loginDbUser);
			_source.setPassword(loginDbPassword);

			/* Test the connection */
			_source.getConnection().close();
			_log.info("Database Connection Working");

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
		System.out.println("LoginDB: Dtabase imported correctly.");
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

	public static LoginDB getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new LoginDB();
		}
		return _instance;
	}

	public Connection getConnection()
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

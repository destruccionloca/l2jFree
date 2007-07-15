package net.sf.l2j.tools.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tools class to start/stop an embedded derby database (for test purpose)
 */
public class DerbyHelper
{

	public static final String USER = "USER1";

	public static final String PASSWORD = "USER1";

	public final static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

	public final static String PROTOCOL = "jdbc:derby:derbyDB";

	private static final Log s_log = LogFactory.getLog(DerbyHelper.class);

	/**
	 * start a derby database in target directory
	 * 
	 */
	public static void startup()
	{
		System.getProperties().put("derby.system.home", "target/data");

		s_log.debug("Starting database Derby in embedded mode.");

		try
		{
			Connection conn = getConnection();
			conn.close();
		}
		catch (Throwable e)
		{
			s_log.error("exception thrown:");

			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				s_log.error(e);
			}
		}

	}

	/**
	 * return a valid connection to database
	 * 
	 * @return a valid connection
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Class.forName(DRIVER).newInstance();

		Connection conn = null;
		Properties props = new Properties();
		props.put("user", USER);
		props.put("password", PASSWORD);

		/*
		 * The connection specifies create=true to cause the database to be
		 * created. To remove the database, remove the directory derbyDB and its
		 * contents. The directory derbyDB will be created under the directory
		 * that the system property derby.system.home points to, or the current
		 * directory if derby.system.home is not set.
		 */
		conn = DriverManager.getConnection(PROTOCOL + ";create=true", props);
		return conn;
	}

	/**
	 * Stop database
	 */
	public static void shutdown()
	{
		try
		{
			boolean gotSQLExc = false;
			try
			{
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			}
			catch (SQLException se)
			{
				gotSQLExc = true;
			}

			if (!gotSQLExc)
			{
				s_log.error("Database did not shut down normally");
			}
			else
			{
				s_log.debug("Database shut down normally");
			}
		}
		catch (Throwable e)
		{
			s_log.error("exception thrown:");

			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				s_log.error(e);
			}
		}
	}

	/**
	 * Recursive print of exception
	 * 
	 * @param e
	 */
	public static void printSQLError(SQLException e)
	{
		while (e != null)
		{

			if (!e.getSQLState().equals("01J01"))
			{
				s_log.error(e.toString());
				e = e.getNextException();
			}
		}
	}
}

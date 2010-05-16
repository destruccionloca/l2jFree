package elayne;

import java.io.IOException;
import java.rmi.ConnectException;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.sun.jmx.snmp.daemon.CommunicationException;

import elayne.application.Activator;
import elayne.datatables.ArmorTable;
import elayne.datatables.CastleTable;
import elayne.datatables.CharTemplateTable;
import elayne.datatables.ClanhallTable;
import elayne.datatables.DetailedItemTable;
import elayne.datatables.FortressTable;
import elayne.datatables.GetBannedPlayers;
import elayne.datatables.GetOnlinePlayers;
import elayne.datatables.HennaTable;
import elayne.datatables.ItemTable;
import elayne.datatables.SkillsTable;
import elayne.datatables.WeaponTable;
import elayne.model.ConnectionDetails;
import elayne.preferences.LoginPreferencePage;
import elayne.rmi.RemoteAdministrationClient;
import elayne.util.Base64;
import elayne.util.connector.LoginDB;
import elayne.util.connector.ServerDB;

/**
 * Encapsulates the state for a session, including the connection details (user
 * name, password, server) and the connection itself.
 */
public class Session implements IAdaptable
{

	private ConnectionDetails _connectionDetails;
	private static Session _instance;
	private boolean _isAllowedUser = false;

	public static Session getInstance()
	{
		if (_instance == null)
			_instance = new Session();
		return _instance;
	}

	private Session()
	{
	// enforce the singleton patter
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter)
	{
		return null;
	}

	public boolean getIsAllowedUser()
	{
		return _isAllowedUser;
	}

	public ConnectionDetails getConnectionDetails()
	{
		return _connectionDetails;
	}

	public void setConnectionDetails(ConnectionDetails connectionDetails)
	{
		_connectionDetails = connectionDetails;
	}

	/**
	 * Establishes the connection to the server and logs in. The connection
	 * details must have already been set.
	 */
	public void connectAndLogin(final IProgressMonitor monitor)
	{
		try
		{
			monitor.beginTask("Connecting...", IProgressMonitor.UNKNOWN);
			monitor.subTask("Conecting to login server...");
			try
			{
				if (selectAccount(monitor))
				{
					if (_isAllowedUser)
					{
						monitor.subTask("Conecting to Server database...");
						ServerDB.getInstance();

						monitor.subTask("Getting Items...");
						ItemTable.getInstance().load();

						monitor.subTask("Getting Armors...");
						ArmorTable.getInstance().load();

						monitor.subTask("Getting Weapons...");
						WeaponTable.getInstance().load();

						monitor.subTask("Getting General Items Information...");
						DetailedItemTable.getInstance().load();

						monitor.subTask("Getting Skills...");
						SkillsTable.getInstance().load();

						monitor.subTask("Getting Character Templates...");
						CharTemplateTable.getInstance().load();

						monitor.subTask("Getting castles...");
						CastleTable.getInstance().load();

						monitor.subTask("Getting clanhalls...");
						ClanhallTable.getInstance().load();

						monitor.subTask("Getting fortresses...");
						FortressTable.getInstance().load();

						monitor.subTask("Getting Henna Templates...");
						HennaTable.getInstance().restore();

						monitor.subTask("Getting Online Players...");
						GetOnlinePlayers.getInstance().getOnlinePlayers(null, true);

						monitor.subTask("Getting Banned Players...");
						GetBannedPlayers.getInstance().getBannedPlayers(null, true);

						IPreferencesService service = Platform.getPreferencesService();
						boolean auto_logn_rmi = service.getBoolean(Activator.PLUGIN_ID, LoginPreferencePage.AUTO_LOGIN_RMI, false, null);
						if (auto_logn_rmi)
						{
							// GET AND HOLD A SERVER CONNECTION
							monitor.subTask("Getting RMI Server connection...");
							RemoteAdministrationClient.getInstance().connect();
						}
					}

				}
				else
				{
					monitor.subTask("Login not allowed to continue.");
				}
			}
			catch (CommunicationException e)
			{
				System.out.println("Exception while connecting to servers: " + e.getMessage());
			}
			catch (ConnectException e)
			{
				System.out.println("Exception while connecting to servers database: " + e.getMessage());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			monitor.done();
		}
	}

	private boolean selectAccount(IProgressMonitor monitor)
	{
		try
		{
			// Encode Password
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] newpass;
			newpass = getConnectionDetails().getPassword().getBytes("UTF-8");
			newpass = md.digest(newpass);

			monitor.subTask("Connecting to Login database...");
			// Add to Base
			java.sql.Connection con = null;
			// Connect to the Login DataBase
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT login, password, accessLevel FROM accounts WHERE login=? AND password=?");
			statement.setString(1, getConnectionDetails().getUserId());
			statement.setString(2, Base64.encodeBytes(newpass));
			ResultSet rset = statement.executeQuery();
			int acl = 0;
			while (rset.next())
			{
				acl = rset.getInt("accessLevel");
			}
			statement.close();
			/*
			 * Access level is checked here. That's the place in which we can
			 * add configuration options for minimum access levels.
			 */
			if (acl >= 100)
			{
				_isAllowedUser = true;
				return true;
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception while connecting to server. Is the server online?");
		}
		return false;
	}
}

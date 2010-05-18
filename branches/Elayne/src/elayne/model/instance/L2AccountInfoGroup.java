package elayne.model.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.util.connector.LoginDB;

/**
 * This group represents a group in which the account information for an
 * L2PcInstance is stored and displayed.
 * @author polbat02
 */
public class L2AccountInfoGroup extends L2GroupEntry
{
	private static final String RESTORE_ACCOUNT = "SELECT password, lastactive, accessLevel, lastIP, lastServerId FROM `accounts` WHERE `login` =?";
	private int _accessLevel;
	private String _encryptedPass;
	private long _lastActive;
	private String _lastIp;
	private int _lastServerId;
	private String _login;
	private L2PcInstance _parent;
	private L2CharacterEntry _encryptedPasswordEntry;

	/**
	 * Constructor defining ONLY an {@link L2PcInstance} which will be the
	 * parent of this group.
	 * @param parent
	 */
	public L2AccountInfoGroup(L2PcInstance parent)
	{
		super(parent, "Account Information");
		_parent = parent;
	}

	/**
	 * @return the access level for the account of the parent
	 * {@link L2PcInstance}.
	 */
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	/**
	 * @return the encrypted password for the account of the parent
	 * {@link L2PcInstance}.
	 */
	public String getEncryptedPass()
	{
		return _encryptedPass;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ACCOUNT_INFORMATION);
	}

	/**
	 * @return the last known IP for the account of the parent
	 * {@link L2PcInstance}.
	 */
	public String getLastIp()
	{
		return _lastIp;
	}

	/**
	 * @return the last known server for the account of the parent
	 * {@link L2PcInstance}. Notice that the server number returned, is
	 * relative to the one in the <code>GAMESERVERS</code> table in the login
	 * server database.
	 */
	 
	public int getLastServer()
	{
		return _lastServerId;
	}

	/**
	 * @return the account of the parent {@link L2PcInstance}.
	 */
	public String getLogin()
	{
		return _login;
	}

	public String getLastActive(long timestamp)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);

		return String.valueOf(cal.getTime());
	}

	@Override
	public L2PcInstance getParent()
	{
		return _parent;
	}

	/**
	 * Restores all the account information related with the parent
	 * {@link L2PcInstance}. When restored, the entries that this group will
	 * contain, are added to this group: Account, Encrypted password, access
	 * level, last IP and last server are the entries.
	 */
	public void restore()
	{
		clearEntries();

		java.sql.Connection con = null;
		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_ACCOUNT);
			statement.setString(1, getParent().getAccount());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_login = getParent().getAccount();
				_encryptedPass = rset.getString("password");
				_lastActive = rset.getLong("lastactive");
				_accessLevel = rset.getInt("accessLevel");
				_lastIp = rset.getString("lastIP");
				_lastServerId = rset.getInt("lastServerId");

				addEntry(new L2CharacterEntry(this, "Account:", _login));
				_encryptedPasswordEntry = new L2CharacterEntry(this, "Encrypted Password:", _encryptedPass);
				addEntry(_encryptedPasswordEntry);
				addEntry(new L2CharacterEntry(this, "Access Level:", _accessLevel));
				addEntry(new L2CharacterEntry(this, "Last Ip:", _lastIp));
				addEntry(new L2CharacterEntry(this, "Last Server:", _lastServerId));
				addEntry(new L2CharacterEntry(this, "Last Active:", getLastActive(_lastActive)));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		getParent().addEntry(this);
	}

	/**
	 * @return The {@link L2CharacterEntry} that represents the encrypted
	 * password.
	 */
	public L2CharacterEntry getEncryptedPasswordEntry()
	{
		return _encryptedPasswordEntry;
	}

	/**
	 * Sets a new Encrypted password onto this groups parent player.
	 * @param newPass
	 */
	public void setEncryptedPasswordEntry(String newPass)
	{
		_encryptedPasswordEntry.setField(newPass);
	}
}

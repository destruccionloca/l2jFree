package elayne.actions;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.ChangeCustomValueDialog;
import elayne.instancemanager.PlayersManager;
import elayne.model.instance.L2PcInstance;
import elayne.util.Base64;
import elayne.util.connector.LoginDB;

/**
 * This class is responsible for changing passwords for a given (selected)
 * player ({@link L2PcInstance}). Some modifications may have to be done to
 * this class.
 * @author polbat02
 */
public class RequestAccountPasswordChange extends ElayneAction
{
	/** The id that this action has */
	private final static String ID = "requestAccountPasswordChange";

	public RequestAccountPasswordChange(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("&Change Password");
		setToolTipText("Change the password for a given account.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CHANGE_PASSWORD));
	}

	/**
	 * Checks if a given string is composed of numbers and letters only.
	 * @param text -> The text to be checked.
	 * @return True if the text is alpha numeric.
	 */
	private boolean isAlphaNumeric(String text)
	{
		if (text == null)
			return false;
		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}
		}
		return result;
	}

	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(".*");
		}
		catch (PatternSyntaxException e)
		{
			System.out.println("RequestAccountPasswordChange : Account password pattern is wrong!");
			e.printStackTrace();
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}

	@Override
	public void run()
	{
		Object obj = _selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			ChangeCustomValueDialog r = new ChangeCustomValueDialog(_window.getShell(), "Password");
			int code = r.open();
			if (code == Window.CANCEL)
				return;
			if (code == Window.OK)
			{
				final String password = r.getNewValue();
				boolean isChangeConfirmed = r.getChangeConfirmmed();
				if (!isChangeConfirmed)
				{
					sendMessage("Change not confirmed.");
					return;
				}

				try
				{
					if ((password.length() < 3) || (password.length() > 16) || !isAlphaNumeric(password) || !isValidName(password))
					{
						sendMessage("The password has to be shorter than 16 characters and composed of alphanumerical values.");
						return;
					}

					MessageDigest md = MessageDigest.getInstance("SHA");
					byte[] newPass;
					newPass = password.getBytes("UTF-8");
					newPass = md.digest(newPass);
					String newPassEncoded = Base64.encodeBytes(newPass);

					L2PcInstance player = (L2PcInstance) obj;

					if (player.isOnline())
					{
						sendMessage("You can not modify an online user.");
						return;
					}

					else if (updatePlayerPassword(newPassEncoded, player.getAccount()))
					{
						player.getAccountInformation().setEncryptedPasswordEntry(newPassEncoded);
						PlayersManager.getInstance().refreshViewer();
						sendMessage("Password Changed Successfully.");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			_selection = (IStructuredSelection) incoming;
			setEnabled(_selection.size() == 1 && _selection.getFirstElement() instanceof L2PcInstance);
		}
		else
			setEnabled(false);
	}

	/**
	 * Update a password for a given account.
	 * @param newPassword
	 * @param login
	 * @return
	 */
	private boolean updatePlayerPassword(String newPassword, String login)
	{
		String sql = "UPDATE `accounts` SET `password`=? WHERE (`login`=?)";
		Connection con;
		try
		{
			con = LoginDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, newPassword);
			statement.setString(2, login);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestAccountPasswordChange: Problems ocurred during acount password change: " + e.getMessage());
			return false;

		}
		return true;
	}
}

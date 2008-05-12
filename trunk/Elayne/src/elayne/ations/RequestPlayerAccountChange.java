/**
 * 
 */
package elayne.ations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.ChangeCustomValueDialog;
import elayne.model.instance.L2PcInstance;
import elayne.util.connector.ServerDB;

/**
 * This class manages all requests regarding a player account change.
 * @author polbat02
 */
public class RequestPlayerAccountChange extends ElayneAction
{
	/** ID of the action */
	public final static String ID = "requestPlayerAccountChange";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestPlayerAccountChange(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("&Change Account");
		setToolTipText("Change the account of a Player.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CHANGE_NAME));
	}

	@Override
	public void dispose()
	{
		super.dispose();
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
			System.out.println("RequestPlayerAccountChange : Character account pattern is wrong!");
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

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			// Open up a new Change Custom Value Dialog where the value is Account.
			ChangeCustomValueDialog r = new ChangeCustomValueDialog(window.getShell(), "Account");
			int code = r.open();
			if (code == Window.CANCEL)
				return;
			if (code == Window.OK)
			{
				// Get the new account that will need to be checked.
				final String account = r.getNewValue();
				boolean isChangeConfirmed = r.getChangeConfirmmed();
				if (!isChangeConfirmed)
				{
					sendMessage("Change not confirmed.");
					return;
				}

				try
				{
					// Some basic checks for the given string (value | account).
					if ((account.length() < 3) || (account.length() > 16) || !isAlphaNumeric(account) || !isValidName(account))
					{
						sendMessage("The account has to be shorter than 16 characters and composed of alphanumerical values.");
						return;
					}

					L2PcInstance player = (L2PcInstance) obj;

					int objectId = player.getObjectId();

					if (player.isOnline())
					{
						sendMessage("You can not modify an online user.");
						return;
					}

					else if (updatePlayerAccount(account, objectId))
					{
						player.setAccount(account);
						treeViewer.refresh();
						sendMessage("Account Changed Successfully. \n" + "Please keep in mind that this player is no longer in this account. \n "
												+ "It's only kept here to in case you want to work on it a bit longer.");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			// Remember the selection for later usage.
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);
		}
		// Not enable the action.
		else
			setEnabled(false);
	}

	/**
	 * Applies a new name to the specified objectId.
	 * @param serverId
	 * @param name
	 * @param objectId
	 * @return
	 */
	private boolean updatePlayerAccount(String account, int objectId)
	{
		String sql = "UPDATE `characters` SET `account_name`=? WHERE (`charId`=?)";
		Connection con;
		try
		{
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, account);
			statement.setInt(2, objectId);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestPlayerAccountChange: Problems ocurred during player account change: " + e.getMessage());
			return false;

		}
		return true;
	}
}

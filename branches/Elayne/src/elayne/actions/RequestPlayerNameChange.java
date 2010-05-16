/**
 * 
 */
package elayne.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
 * This class manages all requests regarding a player name change.
 * @author polbat02
 */
public class RequestPlayerNameChange extends ElayneAction
{
	/** ID of the action */
	public static final String ID = "requestPlayerNameChange";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestPlayerNameChange(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("&Change Name");
		setToolTipText("Change the name of a Player.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CHANGE_NAME));
	}

	private boolean doesCharNameExist(String name)
	{
		boolean result = true;
		java.sql.Connection con = null;

		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
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
				System.out.println("RequestPlayerNameChange: Exception while closing connection. " + e.getMessage());
			}
		}
		return result;
	}

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
			System.out.println("RequestPlayerNameChange : Character name pattern is wrong!");
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
		Object obj = _selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			ChangeCustomValueDialog r = new ChangeCustomValueDialog(_window.getShell(), "Name");
			int code = r.open();
			if (code == Window.CANCEL)
				return;
			if (code == Window.OK)
			{
				final String name = r.getNewValue();
				boolean isChangeConfirmed = r.getChangeConfirmmed();
				if (!isChangeConfirmed)
				{
					sendMessage("Change not confirmed.");
					return;
				}

				ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
				progress.setCancelable(false);
				try
				{
					if (doesCharNameExist(name))
					{
						sendMessage("This name already Exists in the DataBase.");
						return;
					}
					if ((name.length() < 3) || (name.length() > 16) || !isAlphaNumeric(name) || !isValidName(name))
					{
						sendMessage("The name has to be shorter than 16 characters and composed of alphanumerical values.");
						return;
					}

					L2PcInstance player = (L2PcInstance) obj;
					int objectId = player.getObjectId();
					if (player.isOnline())
					{
						sendMessage("You can not modify an online user.");
						return;
					}
					else if (updatePlayerName(name, objectId))
					{
						player.setName(name);
						_treeViewer.refresh();
						sendMessage("Name Changed Successfully.");
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
			_selection = (IStructuredSelection) incoming;

			setEnabled(_selection.size() == 1 && _selection.getFirstElement() instanceof L2PcInstance);
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
	private boolean updatePlayerName(String name, int objectId)
	{
		String sql = "UPDATE `characters` SET `char_name`=? WHERE (`charId`=?)";
		Connection con;
		try
		{
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, name);
			statement.setInt(2, objectId);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestPlayerNameChange: Problems ocurred during player name change: " + e.getMessage());
			return false;

		}
		return true;
	}
}

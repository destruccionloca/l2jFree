/**
 * 
 */
package elayne.ations;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.instance.L2PcInstance;
import elayne.util.connector.ServerDB;

/**
 * A simple action to clear Karma from players.
 * @author polbat02
 */
public class RequestPlayerKarmaCleaning extends ElayneAction
{
	private final static String ID = "requestPlayerKarmaCleaning";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestPlayerKarmaCleaning(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Clear Karma");
		setToolTipText("Clear the Karma from a Player");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLEAR_KARMA));
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
			L2PcInstance player = ((L2PcInstance) obj);
			if (sendConfirmationMessage("Remove Karma", "Are you sure that you want to clear the karma from the player " + player.getName() + "?"))
			{
				if (player.isOnline())
				{
					//TODO: Modify this with a RMI connection to the server requesting to clear the KARMA from a player.
					sendMessage("You can't modify an online player.");
					return;
				}
				if (player.getKarma() <= 0)
				{
					sendMessage("This player doesn't have karma.");
					return;
				}
				if (setKarma(0, player.getObjectId()))
				{
					player.setKarma(0);
					treeViewer.refresh();
					sendMessage("Karma Cleared from the player " + player.getName());
					return;
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
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);
		}
		else
			setEnabled(false);
	}

	/**
	 * Apply a certain amount of Karma to a player in the Database
	 * @param newKarma
	 * @param objectId
	 * @return
	 */
	private boolean setKarma(int newKarma, int objectId)
	{
		final String SQL = "UPDATE `characters` SET `karma`=? WHERE (`charId`=?)";
		java.sql.Connection con = null;
		boolean executed = false;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, newKarma);
			statement.setInt(2, objectId);
			statement.executeUpdate();
			statement.close();
			executed = true;
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
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}
}

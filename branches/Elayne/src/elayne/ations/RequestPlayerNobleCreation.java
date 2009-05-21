/**
 * 
 */
package elayne.ations;

import java.sql.PreparedStatement;

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
 * @author polbat02
 */
public class RequestPlayerNobleCreation extends ElayneAction
{
	public final static String ID = "requestPlayerNobleCreation";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestPlayerNobleCreation(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Make Noble");
		setToolTipText("Add the noble status to a player");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.MAKE_NOBLE));
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
			boolean isNoble = player.isNoble() == 1;
			String text = "";
			if (isNoble)
				text = "Are you sure that you want to remove the Noble status from the player " + player.getName() + "?";
			else
				text = "Are you sure that you want to add the Noble status to the player " + player.getName() + "?";
			if (sendConfirmationMessage("Modify Noble", text))
			{
				if (player.isOnline())
				{
					sendMessage("You can't modify an online player.");
					return;
				}
				else if (isNoble)
				{
					if (updateNoble(0, player.getObjectId()))
					{
						player.setNoble(0);
						sendMessage("The Player " + player.getName() + " is no longer a Noble.");
						setLabels(player);
						return;
					}
				}
				else
				{
					if (updateNoble(1, player.getObjectId()))
					{
						player.setNoble(1);
						sendMessage("The Player " + player.getName() + " is now a Noble.");
						setLabels(player);
						return;
					}
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
		// First check if this is a structured Selection and
		// not another type of selection such as a text from a text field or such.
		if (incoming instanceof IStructuredSelection)
		{
			// Remember the selection for later usage.
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);
			if (isEnabled())
				setLabels((L2PcInstance) selection.getFirstElement());
		}
		// Not enable the action.
		else
			setEnabled(false);
	}

	private void setLabels(L2PcInstance activeChar)
	{
		if (activeChar.isNoble() == 1)
		{
			setText("Delete Noble");
			setToolTipText("Erase the noble status from a noble.");
			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.DELETE_NOBLE));
		}
		else
		{
			setText("Make Noble");
			setToolTipText("Add the noble status to a player");
			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.MAKE_NOBLE));
		}
	}

	private boolean updateNoble(int noble, int objectId)
	{
		final String SQL = "UPDATE `characters` SET `nobless`=? WHERE (`charId`=?)";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, noble);
			statement.setInt(2, objectId);
			statement.executeUpdate();
			statement.close();
			con.close();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}

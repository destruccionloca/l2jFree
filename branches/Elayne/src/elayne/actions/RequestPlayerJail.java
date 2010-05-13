package elayne.actions;

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

public class RequestPlayerJail extends ElayneAction
{
	public final static String ID = "requestPlayerJail";

	public RequestPlayerJail(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Jail Player");
		setToolTipText("Lock a Player in Jail");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.JAIL_PLAYER));
	}

	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			L2PcInstance player = ((L2PcInstance) obj);
			String text = "";
			if (player.isInJail())
				text = "Are you sure that you want to remove the player " + player.getName() + " from jail?";
			else
				text = "Are you sure that you want to jail the player " + player.getName() + "?";
			if (sendConfirmationMessage("Jail Player", text))
			{
				if (player.isOnline())
				{
					sendMessage("You can't modify an online player.");
					return;
				}
				else if (player.isInJail())
				{
					if (updateJail(0, player.getObjectId()))
					{
						player.setInJail(false);
						sendMessage("The Player " + player.getName() + " is no longer in Jail.");
						setLabels(player);
						return;
					}
				}
				else
				{
					if (updateJail(1, player.getObjectId()))
					{
						player.setInJail(true);
						sendMessage("The Player " + player.getName() + " is now locked in Jail.");
						setLabels(player);
						return;
					}
				}
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);

			if (isEnabled())
				setLabels((L2PcInstance) selection.getFirstElement());
		}
		else
			setEnabled(false);
	}

	private void setLabels(L2PcInstance activeChar)
	{
		if (!activeChar.isInJail())
		{
			setText("Jail Player");
			setToolTipText("Lock a Player in Jail");
			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.JAIL_PLAYER));
		}
		else
		{
			setText("Un Jail Player");
			setToolTipText("Releases a Player from the Jail");
			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.UN_JAIL_PLAYER));
		}
	}

	private boolean updateJail(int jail, int objectId)
	{
		final String SQL = "UPDATE `characters` SET `in_jail`=? WHERE (`charId`=?)";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, jail);
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

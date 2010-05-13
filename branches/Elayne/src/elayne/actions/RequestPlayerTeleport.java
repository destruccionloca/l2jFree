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

public class RequestPlayerTeleport extends ElayneAction
{

	private final static String ID = "requestPlayerTeleport";

	public RequestPlayerTeleport(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Teleport to Giran");
		setToolTipText("Teleport a player to Giran");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.TELEPORT));
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			L2PcInstance player = ((L2PcInstance) obj);

			if (sendConfirmationMessage("Teleport to Town", "Are you sure that you want to teleport this player to town?"))
			{
				if (player.getOnline() == 1)
					sendMessage("Player cannot be moved while Online");
				else if (player.getAccessLevel() < 0)
					sendMessage("This Player is Banned. Cannot be moved.");
				else if (player.isInJail())
					sendMessage("This Player is in Jail. Cannot be moved.");
				else if (teleportToGiran(player.getObjectId()))
					sendMessage("Player Teleported to Giran Correctlly.");
				else
					sendMessage("Problems Ocurred upon Teleport.");
			}
		}
	}

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

	private boolean teleportToGiran(int objectId)
	{
		final String SQL = "UPDATE `characters` SET `x`='83400',`y`='147943',`z`='-3404' WHERE (`charId`=?)";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, objectId);
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

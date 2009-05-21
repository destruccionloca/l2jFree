package elayne.ations;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.instance.L2PcInstance;
import elayne.rmi.RemoteAdministrationClient;

public class RequestPlayerKickFromServer extends ElayneAction
{

	private static final String ID = "requestPlayerKickFromServer";

	public RequestPlayerKickFromServer(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Kick Player");
		setToolTipText("Attempts to kick an online user from the server.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.KICK_PLAYER));
	}

	@Override
	public void run()
	{
		if (isRMIConnected())
		{
			if (sendConfirmationMessageWithNoLabel("Kick Player", "Are you sure that you want to kick this player form the server?"))
			{
				try
				{
					L2PcInstance player = (L2PcInstance) selection.getFirstElement();
					if (player.isOnline())
					{
						int result = RemoteAdministrationClient.getInstance().kickPlayerFromServer(player.getName());
						if (result == 1)
						{
							System.out.println("The player " + player.getName() + " was successfully kicked.");
							player.setOnline(false);
						}
						else if (result == 2)
							sendMessage("The player " + player.getName() + " doesnt' seem to be online. Kick action could not be completed.");
						else if (result == 3)
							sendMessage("An error occurred while sending kicking the player from the server.");
					}
				}
				catch (RemoteException e)
				{
					System.out.println("RequestKickPlayerFromServer: Error while sending kicking a player: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else
			sendError("You are not connected to the RMI Server please configure a RMI Server for your Lineage 2 Server.");
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance && ((L2PcInstance) selection.getFirstElement()).isOnline());
		}
		else
			setEnabled(false);
	}

}

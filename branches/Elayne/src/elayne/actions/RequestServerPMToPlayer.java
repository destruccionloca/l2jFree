package elayne.actions;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.MessageDialog;
import elayne.model.instance.L2PcInstance;
import elayne.rmi.RemoteAdministrationClient;

public class RequestServerPMToPlayer extends ElayneAction
{

	private static final String ID = "requestServerPMToPlayer";

	public RequestServerPMToPlayer(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Send Private Message");
		setToolTipText("Attempts to send a private message to an online user.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SEND_PRIVATE_MESSAGE));
	}

	@Override
	public void run()
	{
		if (isRMIConnected())
		{
			MessageDialog msg = new MessageDialog(window.getShell());
			int code = msg.open();
			if (code == Window.CANCEL)
				return;
			if (code == Window.OK)
			{
				String message = msg.getNewMessage();
				if (message == null || message.equals(""))
				{
					sendMessage("This message is not valid.");
					return;
				}
				try
				{
					L2PcInstance player = (L2PcInstance) selection.getFirstElement();
					if (player.isOnline())
					{
						int result = RemoteAdministrationClient.getInstance().sendPrivateMessage(player.getName(), message);
						if (result == 1)
						{
							System.out.println("Message (" + message + ") sent correctly");
							run();
						}
						else if (result == 2)
							sendMessage("The player " + player.getName() + " doesnt' seem to be online. The message could not be sent.");
						else if (result == 3)
							sendMessage("An error occurred while sending the message.");
					}
				}
				catch (RemoteException e)
				{
					System.out.println("RequestSendPrivateMessage: Error while sending the message: " + e.getMessage());
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

package elayne.ations;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.MessageDialog;
import elayne.rmi.RemoteAdministrationClient;

public class RequestServerPMToOnlineGms extends ElayneAction
{

	private static final String ID = "requestServerPMToOnlineGms";

	public RequestServerPMToOnlineGms(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("GM Chat");
		setToolTipText("Send a message to any online GM.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SEND_GM_CHAT));
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
					int numOfGms = RemoteAdministrationClient.getInstance().sendMessageToGms(message);
					System.out.println("Message (" + message + ") sent correctly to " + numOfGms + " GMs.");
					run();
				}
				catch (RemoteException e)
				{
					System.out.println("RequestSendMessageToGMs: Error while sending the message: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else
			sendError("You are not connected to the RMI Server please configure a RMI Server for your Lineage 2 Server.");
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{}
}

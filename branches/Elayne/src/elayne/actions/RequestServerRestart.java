package elayne.actions;

import java.rmi.RemoteException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.rmi.RemoteAdministrationClient;

public class RequestServerRestart extends ElayneAction
{

	public static final int ABORT = 3;
	private static final String ID = "requestServerRestart";
	public static final int SERVER_RESTART = 1;
	public static final int SERVER_SHUT_DOWN = 2;
	private int _procedure;

	public RequestServerRestart(IWorkbenchWindow window, int procedure)
	{
		super(window);
		_procedure = procedure;
		setNewId(setId());
		setText(setText());
		setToolTipText(setToolTipText());
		setImageDescriptor(setImageDescriptor());
	}

	@Override
	public void run()
	{
		if (isRMIConnected())
		{
			try
			{
				switch (_procedure)
				{
					case 1:
						if (sendConfirmationMessageWithNoLabel("Server Restart", "Are you sure you want to restart the server? If yes, the server will restart in 5 minutes."))
						{
							RemoteAdministrationClient.getInstance().scheduleServerRestart(300);
							System.out.println("The server will be restarting in 300 seconds.");
							break;
						}
					case 2:
						if (sendConfirmationMessageWithNoLabel("Server Shutdown", "Are you sure you want to shut down the server? If yes, the server will shut down in 5 minutes."))
						{
							RemoteAdministrationClient.getInstance().scheduleServerShutDown(300);
							System.out.println("The server will be shuting down in 300 seconds.");
							break;
						}
					case 3:
						if (sendConfirmationMessageWithNoLabel("Abort Server Restart", "Are you sure you want to abort the restart procedure?"))
						{
							RemoteAdministrationClient.getInstance().abortServerRestart();
							System.out.println("Server Restart aborted.");
							break;
						}
				}
			}
			catch (RemoteException e)
			{
				System.out.println("RequestServerRestart: Error while issuing restart/shutdown/abort: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else
			sendError("You are not connected to the RMI Server please configure a RMI Server for your Lineage 2 Server.");
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{}

	private String setId()
	{
		if (_procedure == 1)
			return "requestServerRestart";
		else if (_procedure == 2)
			return "requestServerShutDown";
		else if (_procedure == 3)
			return "requestServerRestartAbort";
		return ID;
	}

	private ImageDescriptor setImageDescriptor()
	{
		if (_procedure == 1)
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SERVER_RESTART);
		else if (_procedure == 2)
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SERVER_SHUT_DOWN);
		else if (_procedure == 3)
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ABORT_SERVER_RESTART);
		return null;
	}

	private String setText()
	{
		if (_procedure == 1)
			return "Server Restart";
		else if (_procedure == 2)
			return "Server Shutdown";
		else if (_procedure == 3)
			return "Abort Server Restart";
		return null;
	}

	private String setToolTipText()
	{
		if (_procedure == 1)
			return "Issues a server restart through RMI.";
		else if (_procedure == 2)
			return "Issues a server shutdown through RMI.";
		else if (_procedure == 3)
			return "Aborts a server restart through RMI.";
		return null;
	}
}

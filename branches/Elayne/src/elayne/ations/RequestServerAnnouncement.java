package elayne.ations;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.AnnouncementDialog;
import elayne.rmi.RemoteAdministrationClient;

/**
 * The {@link RequestServerAnnouncement} class defines a new action that is used
 * to perform an announcement inside the game. An announcement can only be
 * performed if a RMI connection is established.
 * @author polbat02
 */
public class RequestServerAnnouncement extends ElayneAction
{

	private static final String ID = "requestServerAnnouncement";

	/**
	 * Defines a new action that is used to perform an announcement inside the
	 * game.
	 * @param window
	 */
	public RequestServerAnnouncement(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("Perform Announcement");
		setToolTipText("Performs an announcement ingame.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ANNOUNCE));
	}

	@Override
	public void run()
	{
		if (isRMIConnected())
		{
			AnnouncementDialog ad = new AnnouncementDialog(window.getShell());
			int code = ad.open();
			if (code == Window.CANCEL)
				return;
			if (code == Window.OK)
			{
				String announcement = ad.getNewAnnouncement();
				if (announcement == null || announcement.equals(""))
				{
					sendError("This announcement is not valid.");
					return;
				}
				try
				{
					RemoteAdministrationClient.getInstance().announceToAll(announcement);
					System.out.println("Announcement (" + announcement + ") sent correctly");
					run();
				}
				catch (RemoteException e)
				{
					System.out.println("RequestAnnouncement: Error while performing the announcement: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else
			sendError("You are not connected to the RMI Server please configure a RMI Server for your Lineage 2 Server.");
	}

	// This method has no use in this action.
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{}
}

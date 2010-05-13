package elayne.actions;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import elayne.rmi.RemoteAdministrationClient;

public class RequestServerInstanceReload extends ElayneAction
{

	public static final int HTML = 4;
	private static final String ID = "requestServerInstanceReload";
	public static final int INSTANCE_MANAGER = 6;
	public static final int ITEMS = 5;
	public static final int MULTISELL = 1;
	public static final int NPC = 3;
	public static final int SKILLS = 2;
	public static final int SPAWNS = 9;
	public static final int TELEPORTS = 8;
	public static final int ZONES = 7;
	private int actionId;

	/**
	 * Reloads something in-game.
	 * @param reloadProcedure --> Allowed procedures: 1(MULTISELL), 2(SKILLS),
	 * 3(NPC), 4(HTML), 5(ITEMS), 6(INSTANCE MANAGERS), 7(ZONES), 8(TELEPORTS),
	 * 9(SPAWNS).
	 * @throws RemoteException
	 */
	public RequestServerInstanceReload(IWorkbenchWindow window, int actionId)
	{
		super(window);
		this.actionId = actionId;
		setNewId(ID);
		setText();
	}

	@Override
	public void run()
	{
		if (isRMIConnected())
		{
			try
			{
				if (actionId > 0 && actionId <= 9)
				{
					RemoteAdministrationClient.getInstance().reload(actionId);
					System.out.println("Reload complete.");
				}
			}
			catch (RemoteException e)
			{
				System.out.println("RequestReloadInstance: Error while reloading: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else
			sendError("You are not connected to the RMI Server please configure a RMI Server for your Lineage 2 Server.");
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{}

	private String setText()
	{
		switch (actionId)
		{
			case 1:
				setText("Reload Multisell Stores");
				setToolTipText("Reload all multisells store through RMI.");
				break;
			case 2:
				setText("Reload Skills");
				setToolTipText("Reload all skills through RMI.");
				break;
			case 3:
				setText("Reload Npcs");
				setToolTipText("Reload all npcs through RMI.");
				break;
			case 4:
				setText("Reload HTML");
				setToolTipText("Reload all htmls through RMI.");
				break;
			case 5:
				setText("Reload Items");
				setToolTipText("Reload all the items through RMI.");
				break;
			case 6:
				setText("Reload Instance Managers");
				setToolTipText("Reload every instance managers through RMI.");
				break;
			case 7:
				setText("Rload Zones");
				setToolTipText("Reload all the zones through RMI.");
				break;
			case 8:
				setText("Reload Teleports");
				setToolTipText("Reload all the teleports through RMI.");
				break;
			case 9:
				setText("Reload Spawns");
				setToolTipText("Reload all the spawns through RMI.");
				break;
		}
		return null;
	}
}

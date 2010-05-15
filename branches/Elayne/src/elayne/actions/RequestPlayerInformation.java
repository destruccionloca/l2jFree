package elayne.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.instancemanager.PlayersManager;
import elayne.model.L2RootSession;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2PcInstance;
import elayne.views.PlayerInfoView;

/**
 * This Class manages all Player Information Related actions.<br>
 * Read on into every Method and Constructor for more information.
 * @author polbat02
 */
public class RequestPlayerInformation extends ElayneAction
{
	/** ID of the action */
	public final static String ID = "requestPlayerInformation";

	/** New line in a text field or text area. */
	@SuppressWarnings("unused")
	private final static String newLine = "\n";

	private String _name = "";

	/**
	 * CONSTRUCTOR<br>
	 * This Constructor instantiates the viewer and the OnlineSession and sets
	 * it's basic information.
	 * @param viewer: The {@link TreeViewer} to instantiate.
	 * @param session: The {@link L2RootSession} to instantiate.
	 */
	public RequestPlayerInformation(IWorkbenchWindow window, TreeViewer viewer)
	{
		super(window, viewer);
		setNewId(ID);
		setText("&Basic Information");
		setToolTipText("Show detailed information of the selected player's account.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.INFORMATION));
	}

	private void openNewPlayerInvoView(final String name)
	{
		if (PlayersManager.getInstance().isKnownPlayer(name) && !PlayersManager.getInstance().containsGroup(name))
		{
			L2PcInstance player = PlayersManager.getInstance().getPlayer(name);
			PlayersManager.getInstance().addPlayer(player);
		}
		else if (!(PlayersManager.getInstance().isKnownPlayer(name)) && L2PcInstance.isRealPlayer(name))
		{
			L2PcInstance player = new L2PcInstance(null, name);
			PlayersManager.getInstance().addPlayer(player);
		}
		showPage();
	}

	@Override
	public void run()
	{
		if (treeViewer == null)
		{
			openNewPlayerInvoView(_name);
			return;
		}
		// Get the selected object in the viewer.
		ISelection selectedObject = treeViewer.getSelection();
		// Define an Object casted from the selection.
		// This Object will later be used to define which action we need to run.
		Object obj = ((IStructuredSelection) selectedObject).getFirstElement();

		/*
		 * If the Object is an instance of an OnlineEntry or an instance of a
		 * PlayerBriefEntry... : 1- Get the account and set it as the NEW
		 * account to look for. 2- Get the information from the entry and make a
		 * StringBuffer to notify the player with an information message.
		 */
		if (obj instanceof L2CharacterBriefEntry)
		{
			String name = ((L2CharacterBriefEntry) obj).getName();
			openNewPlayerInvoView(name);
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection Iselection)
	{}

	/**
	 * Sets an account to look for in case this action is issued for a new view.
	 * @param account
	 */
	public void setName(String name)
	{
		_name = name;
	}

	private void showPage()
	{
		IWorkbenchPage page = window.getActivePage();
		try
		{
			page.showView(PlayerInfoView.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}
}

package elayne.ations;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import elayne.instancemanager.ClansManager;
import elayne.model.instance.L2Clan;
import elayne.views.ClanInfoView;

/**
 * This class represents an action that initiates the search for a certain clan.<br>
 * I personally don't like the way this class is used... Should be reprogrammed
 * some other way... changing the ways that: - A clan id needs to be set from
 * the search view. - A clan name needs to be set from the search view.<br>
 * Other than that, this class contains methods that help us know which is the
 * best way to display a clan information in the {@link ClanInfoView}.
 * @author polbat02
 */
public class RequestClanInformation extends ElayneAction
{

	private static final String ID = "requestClanInformation";
	private int id;
	private String name;

	/**
	 * Defines a new {@link RequestClanInformation} action.
	 * @param window -> The window to which this action has access to.
	 */
	public RequestClanInformation(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("&Clan Information");
		setToolTipText("Show detailed information of the selected clan.");
	}

	/**
	 * Attempts to add / display / restore a clan into the {@link ClanInfoView}.
	 * Look inside the method for more information.
	 * @param clanId
	 * @param clanName
	 */
	private void openClanInfoView(final int clanId, String clanName)
	{
		/*
		 * This method: First checks if a clan is already known (which doesn't
		 * explicitly mean that the clan is shown inside the view). It also
		 * checks (since the previous check didn't tell us that) if the view
		 * does not contain this clan (if known). If the previous checks are
		 * given, we will add the already known clan into the viewer. Else we
		 * still make sure this ain't a known clan and then (if not known) we
		 * define it and add it into the ClansManager. Finally we'll display the
		 * ClanInfoView.
		 */
		if (ClansManager.getInstance().isKnownClan(clanId) && !ClansManager.getInstance().containsGroup(ClansManager.getInstance().getClan(clanId).getName()))
		{
			L2Clan clan = ClansManager.getInstance().getClan(clanId);
			ClansManager.getInstance().addClan(clan);
		}
		else if (!(ClansManager.getInstance().isKnownClan(clanId)))
		{
			L2Clan clan = new L2Clan(clanId, clanName, null);
			ClansManager.getInstance().addClan(clan);
		}
		showPage();
	}

	@Override
	public void run()
	{
		try
		{
			openClanInfoView(id, name);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{}

	public void setClanId(int id)
	{
		this.id = id;
	}

	public void setClanName(String name)
	{
		this.name = name;
	}

	/**
	 * Attempts to display the ClanInvoView (or bring front).
	 */
	private void showPage()
	{
		IWorkbenchPage page = window.getActivePage();
		try
		{
			page.showView(ClanInfoView.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}
}

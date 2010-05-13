package elayne.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

import elayne.actions.ClearPlayerFromTreeViewer;
import elayne.actions.RequestAccountPasswordChange;
import elayne.actions.RequestClanPlayerKick;
import elayne.actions.RequestItemAward;
import elayne.actions.RequestItemCountChange;
import elayne.actions.RequestItemDeletion;
import elayne.actions.RequestItemEnchant;
import elayne.actions.RequestItemInventoryWipe;
import elayne.actions.RequestPlayerAccountChange;
import elayne.actions.RequestPlayerBan;
import elayne.actions.RequestPlayerInformation;
import elayne.actions.RequestPlayerJail;
import elayne.actions.RequestPlayerKarmaCleaning;
import elayne.actions.RequestPlayerKickFromServer;
import elayne.actions.RequestPlayerNameChange;
import elayne.actions.RequestPlayerNobleCreation;
import elayne.actions.RequestPlayerRefresh;
import elayne.actions.RequestPlayerSubClassDeletion;
import elayne.actions.RequestPlayerTeleport;
import elayne.actions.RequestServerPMToPlayer;
import elayne.actions.RequestSkillDeletion;
import elayne.actions.RequestSkillsWipe;
import elayne.instancemanager.PlayersManager;
import elayne.model.L2AdapterFactory;
import elayne.model.L2Character;
import elayne.model.L2RootSession;

public class PlayerInfoView extends ViewPart
{
	private TreeViewer viewer;

	// ACTIONS:
	private Action actionShowInfo;
	private Action requestItemDelete;
	private Action requestItemEnchant;
	private Action requestWipeInventory;
	private Action requestTeleportToGiran;
	private Action requestPlayerNameChange;
	private Action requestPlayerAccountChange;
	private Action requestDeleteSkill;
	private Action requestWipeSkills;
	private Action requestClearKarma;
	private Action requestMakeNoble;
	private Action requestJailPlayer;
	private Action requestKickPlayerFromClan;
	private Action requestDeleteSubClass;
	private Action requestSendPrivateMessage;
	private Action requestKickPlayerFromServer;
	private Action requestRefreshPlayer;
	private Action banPlayer;
	private Action banAccount;
	private Action banAllAccounts;
	private Action unBanPlayer;
	private Action unBanAccount;
	private Action unBanAllAccounts;
	private Action requestRemovePlayer;
	private Action requestChangeItemCount;
	private Action requestAwardItemToPlayer;
	private Action requestAccountPasswordChange;

	private IAdapterFactory adapterFactory = new L2AdapterFactory();
	public L2RootSession session;
	public static final String ID = "elayne.views.playerInfo";

	@Override
	public void createPartControl(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(PlayersManager.getInstance());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		PlayersManager.getInstance().setViewer(viewer);
		Platform.getAdapterManager().registerAdapters(adapterFactory, L2Character.class);
		getSite().setSelectionProvider(viewer);
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				actionShowInfo.run();
			}
		});
		setFocus();
		viewer.refresh();
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				PlayerInfoView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		fillActions(manager);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		fillActions(manager);
	}

	private void fillActions(IMenuManager manager)
	{
		// ITEM RELATED MENU
		MenuManager itemMenu = new MenuManager("&Item Related", "item related");
		itemMenu.add(requestItemDelete);
		itemMenu.add(requestItemEnchant);
		itemMenu.add(requestWipeInventory);
		itemMenu.add(requestChangeItemCount);
		itemMenu.add(requestAwardItemToPlayer);

		// SKILL RELATED MENU
		MenuManager skillMenu = new MenuManager("&Skill Related", "skill related");
		skillMenu.add(requestDeleteSkill);
		skillMenu.add(requestWipeSkills);

		// BAN RELATED MENU
		MenuManager banMenu = new MenuManager("&Ban Player", "ban player");
		banMenu.add(banPlayer);
		banMenu.add(banAccount);
		banMenu.add(banAllAccounts);

		// UN BAN RELATED MENU
		MenuManager unBanMenu = new MenuManager("&Un Ban Player", "un ban player");
		unBanMenu.add(unBanPlayer);
		unBanMenu.add(unBanAccount);
		unBanMenu.add(unBanAllAccounts);

		// ADD FINAL MENU TO THE MANAGER
		manager.add(requestRefreshPlayer);
		manager.add(requestPlayerNameChange);
		manager.add(requestPlayerAccountChange);
		manager.add(requestAccountPasswordChange);
		manager.add(requestTeleportToGiran);
		manager.add(requestClearKarma);
		manager.add(requestMakeNoble);
		manager.add(requestJailPlayer);
		manager.add(requestKickPlayerFromClan);
		manager.add(requestDeleteSubClass);
		manager.add(requestSendPrivateMessage);
		manager.add(requestKickPlayerFromServer);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(banMenu);
		manager.add(unBanMenu);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(itemMenu);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(skillMenu);
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(requestRemovePlayer);
		manager.add(requestRefreshPlayer);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(requestKickPlayerFromServer);
		manager.add(requestSendPrivateMessage);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(banPlayer);
		manager.add(unBanPlayer);
		manager.add(requestTeleportToGiran);
		manager.add(requestDeleteSkill);
		manager.add(requestItemDelete);
		manager.add(requestItemEnchant);
	}

	private void makeActions()
	{
		actionShowInfo = new RequestPlayerInformation(getSite().getWorkbenchWindow(), viewer);
		requestItemDelete = new RequestItemDeletion(getSite().getWorkbenchWindow(), viewer);
		requestItemEnchant = new RequestItemEnchant(getSite().getWorkbenchWindow(), viewer);
		requestWipeInventory = new RequestItemInventoryWipe(getSite().getWorkbenchWindow(), viewer);
		requestTeleportToGiran = new RequestPlayerTeleport(getSite().getWorkbenchWindow(), viewer);
		requestPlayerNameChange = new RequestPlayerNameChange(getSite().getWorkbenchWindow(), viewer);
		requestAccountPasswordChange = new RequestAccountPasswordChange(getSite().getWorkbenchWindow());
		requestPlayerAccountChange = new RequestPlayerAccountChange(getSite().getWorkbenchWindow(), viewer);
		requestClearKarma = new RequestPlayerKarmaCleaning(getSite().getWorkbenchWindow(), viewer);
		requestMakeNoble = new RequestPlayerNobleCreation(getSite().getWorkbenchWindow(), viewer);
		requestJailPlayer = new RequestPlayerJail(getSite().getWorkbenchWindow(), viewer);
		requestKickPlayerFromClan = new RequestClanPlayerKick(getSite().getWorkbenchWindow(), viewer);
		requestDeleteSubClass = new RequestPlayerSubClassDeletion(getSite().getWorkbenchWindow(), viewer);
		requestSendPrivateMessage = new RequestServerPMToPlayer(getSite().getWorkbenchWindow(), viewer);
		requestKickPlayerFromServer = new RequestPlayerKickFromServer(getSite().getWorkbenchWindow(), viewer);
		requestRefreshPlayer = new RequestPlayerRefresh(getSite().getWorkbenchWindow(), viewer);
		banPlayer = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 1);
		banAccount = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 2);
		banAllAccounts = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 3);
		unBanPlayer = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 4);
		unBanAccount = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 5);
		unBanAllAccounts = new RequestPlayerBan(getSite().getWorkbenchWindow(), viewer, 6);
		requestDeleteSkill = new RequestSkillDeletion(getSite().getWorkbenchWindow(), viewer);
		requestWipeSkills = new RequestSkillsWipe(getSite().getWorkbenchWindow(), viewer);
		requestRemovePlayer = new ClearPlayerFromTreeViewer(getSite().getWorkbenchWindow());
		requestChangeItemCount = new RequestItemCountChange(getSite().getWorkbenchWindow(), viewer);
		requestAwardItemToPlayer = new RequestItemAward(getSite().getWorkbenchWindow());
	}

	/**
	 * This shows up a new Information message for the OnlinePlayers view.
	 * @param message
	 */
	@SuppressWarnings("unused")
	private void showMessage(String message)
	{
		MessageDialog.openInformation(getSite().getShell(), "Online Players", message);
	}

	public TreeViewer getViewer()
	{
		return viewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose()
	{
		Platform.getAdapterManager().unregisterAdapters(adapterFactory);
		super.dispose();
	}
}

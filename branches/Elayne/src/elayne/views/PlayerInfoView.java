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
	private TreeViewer _viewer;

	// ACTIONS:
	private Action _actionShowInfo;
	private Action _requestItemDelete;
	private Action _requestItemEnchant;
	private Action _requestWipeInventory;
	private Action _requestTeleportToGiran;
	private Action _requestPlayerNameChange;
	private Action _requestPlayerAccountChange;
	private Action _requestDeleteSkill;
	private Action _requestWipeSkills;
	private Action _requestClearKarma;
	private Action _requestMakeNoble;
	private Action _requestJailPlayer;
	private Action _requestKickPlayerFromClan;
	private Action _requestDeleteSubClass;
	private Action _requestSendPrivateMessage;
	private Action _requestKickPlayerFromServer;
	private Action _requestRefreshPlayer;
	private Action _banPlayer;
	private Action _banAccount;
	private Action _banAllAccounts;
	private Action _unBanPlayer;
	private Action _unBanAccount;
	private Action _unBanAllAccounts;
	private Action _requestRemovePlayer;
	private Action _requestChangeItemCount;
	private Action _requestAwardItemToPlayer;
	private Action _requestAccountPasswordChange;

	private IAdapterFactory adapterFactory = new L2AdapterFactory();
	public L2RootSession session;
	public static final String ID = "elayne.views.playerInfo";

	@Override
	public void createPartControl(Composite parent)
	{
		_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_viewer.setContentProvider(PlayersManager.getInstance());
		_viewer.setLabelProvider(new WorkbenchLabelProvider());
		PlayersManager.getInstance().setViewer(_viewer);
		Platform.getAdapterManager().registerAdapters(adapterFactory, L2Character.class);
		getSite().setSelectionProvider(_viewer);
		_viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				_actionShowInfo.run();
			}
		});
		setFocus();
		_viewer.refresh();
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
		Menu menu = menuMgr.createContextMenu(_viewer.getControl());
		_viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, _viewer);
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
		itemMenu.add(_requestItemDelete);
		itemMenu.add(_requestItemEnchant);
		itemMenu.add(_requestWipeInventory);
		itemMenu.add(_requestChangeItemCount);
		itemMenu.add(_requestAwardItemToPlayer);

		// SKILL RELATED MENU
		MenuManager skillMenu = new MenuManager("&Skill Related", "skill related");
		skillMenu.add(_requestDeleteSkill);
		skillMenu.add(_requestWipeSkills);

		// BAN RELATED MENU
		MenuManager banMenu = new MenuManager("&Ban Player", "ban player");
		banMenu.add(_banPlayer);
		banMenu.add(_banAccount);
		banMenu.add(_banAllAccounts);

		// UN BAN RELATED MENU
		MenuManager unBanMenu = new MenuManager("&Unban Player", "unban player");
		unBanMenu.add(_unBanPlayer);
		unBanMenu.add(_unBanAccount);
		unBanMenu.add(_unBanAllAccounts);

		// ADD FINAL MENU TO THE MANAGER
		manager.add(_requestRefreshPlayer);
		manager.add(_requestPlayerNameChange);
		manager.add(_requestPlayerAccountChange);
		manager.add(_requestAccountPasswordChange);
		manager.add(_requestTeleportToGiran);
		manager.add(_requestClearKarma);
		manager.add(_requestMakeNoble);
		manager.add(_requestJailPlayer);
		manager.add(_requestKickPlayerFromClan);
		manager.add(_requestDeleteSubClass);
		manager.add(_requestSendPrivateMessage);
		manager.add(_requestKickPlayerFromServer);
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
		manager.add(_requestRemovePlayer);
		manager.add(_requestRefreshPlayer);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(_requestKickPlayerFromServer);
		manager.add(_requestSendPrivateMessage);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(_banPlayer);
		manager.add(_unBanPlayer);
		manager.add(_requestTeleportToGiran);
		manager.add(_requestDeleteSkill);
		manager.add(_requestItemDelete);
		manager.add(_requestItemEnchant);
	}

	private void makeActions()
	{
		_actionShowInfo = new RequestPlayerInformation(getSite().getWorkbenchWindow(), _viewer);
		_requestItemDelete = new RequestItemDeletion(getSite().getWorkbenchWindow(), _viewer);
		_requestItemEnchant = new RequestItemEnchant(getSite().getWorkbenchWindow(), _viewer);
		_requestWipeInventory = new RequestItemInventoryWipe(getSite().getWorkbenchWindow(), _viewer);
		_requestTeleportToGiran = new RequestPlayerTeleport(getSite().getWorkbenchWindow(), _viewer);
		_requestPlayerNameChange = new RequestPlayerNameChange(getSite().getWorkbenchWindow(), _viewer);
		_requestAccountPasswordChange = new RequestAccountPasswordChange(getSite().getWorkbenchWindow());
		_requestPlayerAccountChange = new RequestPlayerAccountChange(getSite().getWorkbenchWindow(), _viewer);
		_requestClearKarma = new RequestPlayerKarmaCleaning(getSite().getWorkbenchWindow(), _viewer);
		_requestMakeNoble = new RequestPlayerNobleCreation(getSite().getWorkbenchWindow(), _viewer);
		_requestJailPlayer = new RequestPlayerJail(getSite().getWorkbenchWindow(), _viewer);
		_requestKickPlayerFromClan = new RequestClanPlayerKick(getSite().getWorkbenchWindow(), _viewer);
		_requestDeleteSubClass = new RequestPlayerSubClassDeletion(getSite().getWorkbenchWindow(), _viewer);
		_requestSendPrivateMessage = new RequestServerPMToPlayer(getSite().getWorkbenchWindow(), _viewer);
		_requestKickPlayerFromServer = new RequestPlayerKickFromServer(getSite().getWorkbenchWindow(), _viewer);
		_requestRefreshPlayer = new RequestPlayerRefresh(getSite().getWorkbenchWindow(), _viewer);
		_banPlayer = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 1);
		_banAccount = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 2);
		_banAllAccounts = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 3);
		_unBanPlayer = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 4);
		_unBanAccount = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 5);
		_unBanAllAccounts = new RequestPlayerBan(getSite().getWorkbenchWindow(), _viewer, 6);
		_requestDeleteSkill = new RequestSkillDeletion(getSite().getWorkbenchWindow(), _viewer);
		_requestWipeSkills = new RequestSkillsWipe(getSite().getWorkbenchWindow(), _viewer);
		_requestRemovePlayer = new ClearPlayerFromTreeViewer(getSite().getWorkbenchWindow());
		_requestChangeItemCount = new RequestItemCountChange(getSite().getWorkbenchWindow(), _viewer);
		_requestAwardItemToPlayer = new RequestItemAward(getSite().getWorkbenchWindow());
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
		return _viewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		_viewer.getControl().setFocus();
	}

	@Override
	public void dispose()
	{
		Platform.getAdapterManager().unregisterAdapters(adapterFactory);
		super.dispose();
	}
}

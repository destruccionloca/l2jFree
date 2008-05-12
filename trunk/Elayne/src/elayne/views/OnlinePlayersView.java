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
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.ations.RequestPlayerInformation;
import elayne.ations.util.ActionWaiter;
import elayne.datatables.GetOnlinePlayers;
import elayne.model.L2AdapterFactory;
import elayne.model.L2Character;
import elayne.model.L2RootSession;

public class OnlinePlayersView extends ViewPart
{
	private TreeViewer viewer;
	private Action actionShowInfo;
	private Action refreshPlayersAction;
	private Action sortPlayersByName;
	private Action sortPlayersByLevel;
	private Action sortPlayersByAccesslevel;
	private Action sortPlayersBySex;
	private IAdapterFactory adapterFactory = new L2AdapterFactory();
	public L2RootSession session;
	public static final String ID = "elayne.views.OnlinePlayers";

	/**
	 * This is a call-back that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		// Load all the banned players
		GetOnlinePlayers.getInstance().getOnlinePlayers(this, false);

		Platform.getAdapterManager().registerAdapters(adapterFactory, L2Character.class);

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new BaseWorkbenchContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(session.getRoot());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				OnlinePlayersView.this.fillContextMenu(manager);
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
		manager.add(actionShowInfo);
		manager.add(new Separator());
		manager.add(refreshPlayersAction);
		manager.add(sortPlayersByName);
		manager.add(sortPlayersByLevel);
		manager.add(sortPlayersByAccesslevel);
		manager.add(sortPlayersBySex);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(actionShowInfo);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionShowInfo);
		manager.add(refreshPlayersAction);
		manager.add(sortPlayersByName);
		manager.add(sortPlayersByLevel);
	}

	private void makeActions()
	{
		actionShowInfo = new RequestPlayerInformation(getSite().getWorkbenchWindow(), viewer);

		// REFRESH PLAYER LIST
		refreshPlayersAction = new Action()
		{
			@Override
			public void run()
			{
				if (!refreshPlayersAction.isEnabled())
					return;
				ActionWaiter action = new ActionWaiter(refreshPlayersAction);
				GetOnlinePlayers.getInstance().getOnlinePlayers(OnlinePlayersView.this, true);
				viewer.setInput(session.getRoot());
				// FLOOD PROTECTOR USABLE FOR ALL ACTIONS.
				action.actionWait(60);
			}
		};
		refreshPlayersAction.setText("Refresh Players Online");
		refreshPlayersAction.setToolTipText("Refreshes the online players list.");
		refreshPlayersAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.REFRESH));
		refreshPlayersAction.setId("Elayne.actions.refreshOnlinePlayers");

		// SORT PLAYERS BY NAME
		sortPlayersByName = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByName(OnlinePlayersView.this);
				viewer.setInput(session.getRoot());
			}
		};
		sortPlayersByName.setText("Order by name");
		sortPlayersByName.setToolTipText("Orders the online players list by name.");
		sortPlayersByName.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_NAME));

		// SORT PLAYERS BY LEVEL
		sortPlayersByLevel = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByLevel(OnlinePlayersView.this);
				viewer.setInput(session.getRoot());
			}
		};
		sortPlayersByLevel.setText("Order by level");
		sortPlayersByLevel.setToolTipText("Orders the online players list by level.");
		sortPlayersByLevel.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_LEVEL));

		// SORT PKAYERS BY ACCESSLEVEL
		sortPlayersByAccesslevel = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByAccesslevel(OnlinePlayersView.this);
				viewer.setInput(session.getRoot());
			}
		};
		sortPlayersByAccesslevel.setText("Order by access level");
		sortPlayersByAccesslevel.setToolTipText("Orders the online players list by access level.");
		sortPlayersByAccesslevel.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_ACCESSLEVEL));

		// SORT PLAYERS BY SEX
		sortPlayersBySex = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByGender(OnlinePlayersView.this);
				viewer.setInput(session.getRoot());
			}
		};
		sortPlayersBySex.setText("Order by sex");
		sortPlayersBySex.setToolTipText("Orders the online players list by sex.");
		sortPlayersBySex.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_SEX));
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				actionShowInfo.run();
			}
		});
	}

	/**
	 * This shows up a new Information message for the OnlinePlayers view.
	 * @param message
	 */
	@SuppressWarnings("unused")
	private void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Online Players", message);
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
}

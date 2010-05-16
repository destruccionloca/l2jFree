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
import elayne.actions.RequestPlayerInformation;
import elayne.actions.util.ActionWaiter;
import elayne.application.Activator;
import elayne.datatables.GetOnlinePlayers;
import elayne.model.L2AdapterFactory;
import elayne.model.L2Character;
import elayne.model.L2RootSession;

public class OnlinePlayersView extends ViewPart
{
	private TreeViewer _viewer;
	private Action _actionShowInfo;
	private Action _refreshPlayersAction;
	private Action _sortPlayersByName;
	private Action _sortPlayersByLevel;
	private Action _sortPlayersByAccesslevel;
	private Action _sortPlayersBySex;
	private IAdapterFactory _adapterFactory = new L2AdapterFactory();
	public L2RootSession _session;
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

		Platform.getAdapterManager().registerAdapters(_adapterFactory, L2Character.class);

		_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_viewer.setContentProvider(new BaseWorkbenchContentProvider());
		_viewer.setLabelProvider(new WorkbenchLabelProvider());
		_viewer.setInput(_session.getRoot());
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
		manager.add(_actionShowInfo);
		manager.add(new Separator());
		manager.add(_refreshPlayersAction);
		manager.add(_sortPlayersByName);
		manager.add(_sortPlayersByLevel);
		manager.add(_sortPlayersByAccesslevel);
		manager.add(_sortPlayersBySex);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(_actionShowInfo);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(_actionShowInfo);
		manager.add(_refreshPlayersAction);
		manager.add(_sortPlayersByName);
		manager.add(_sortPlayersByLevel);
	}

	private void makeActions()
	{
		_actionShowInfo = new RequestPlayerInformation(getSite().getWorkbenchWindow(), _viewer);

		// REFRESH PLAYER LIST
		_refreshPlayersAction = new Action()
		{
			@Override
			public void run()
			{
				if (!_refreshPlayersAction.isEnabled())
					return;
				ActionWaiter action = new ActionWaiter(_refreshPlayersAction);
				GetOnlinePlayers.getInstance().getOnlinePlayers(OnlinePlayersView.this, true);
				_viewer.setInput(_session.getRoot());
				// FLOOD PROTECTOR USABLE FOR ALL ACTIONS.
				action.actionWait(60);
			}
		};
		_refreshPlayersAction.setText("Refresh Players Online");
		_refreshPlayersAction.setToolTipText("Refreshes the online players list.");
		_refreshPlayersAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.REFRESH));
		_refreshPlayersAction.setId("Elayne.actions.refreshOnlinePlayers");

		// SORT PLAYERS BY NAME
		_sortPlayersByName = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByName(OnlinePlayersView.this);
				_viewer.setInput(_session.getRoot());
			}
		};
		_sortPlayersByName.setText("Order by name");
		_sortPlayersByName.setToolTipText("Orders the online players list by name.");
		_sortPlayersByName.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_NAME));

		// SORT PLAYERS BY LEVEL
		_sortPlayersByLevel = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByLevel(OnlinePlayersView.this);
				_viewer.setInput(_session.getRoot());
			}
		};
		_sortPlayersByLevel.setText("Order by level");
		_sortPlayersByLevel.setToolTipText("Orders the online players list by level.");
		_sortPlayersByLevel.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_LEVEL));

		// SORT PKAYERS BY ACCESSLEVEL
		_sortPlayersByAccesslevel = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByAccesslevel(OnlinePlayersView.this);
				_viewer.setInput(_session.getRoot());
			}
		};
		_sortPlayersByAccesslevel.setText("Order by access level");
		_sortPlayersByAccesslevel.setToolTipText("Orders the online players list by access level.");
		_sortPlayersByAccesslevel.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_ACCESSLEVEL));

		// SORT PLAYERS BY SEX
		_sortPlayersBySex = new Action()
		{
			@Override
			public void run()
			{
				GetOnlinePlayers.getInstance().sortOnlinePlayersByGender(OnlinePlayersView.this);
				_viewer.setInput(_session.getRoot());
			}
		};
		_sortPlayersBySex.setText("Order by sex");
		_sortPlayersBySex.setToolTipText("Orders the online players list by sex.");
		_sortPlayersBySex.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SORT_BY_SEX));
	}

	private void hookDoubleClickAction()
	{
		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				_actionShowInfo.run();
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
		MessageDialog.openInformation(_viewer.getControl().getShell(), "Online Players", message);
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
}

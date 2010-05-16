package elayne.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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

import elayne.actions.RequestClanPlayerKick;
import elayne.actions.RequestPlayerInformation;
import elayne.instancemanager.ClansManager;
import elayne.model.L2AdapterFactory;
import elayne.model.L2Character;

public class ClanInfoView extends ViewPart
{
	public static final String ID = "elayne.views.clanView";
	private TreeViewer _viewer;
	private IAdapterFactory _adapterFactory = new L2AdapterFactory();
	private Action _actionShowInfo;
	private RequestClanPlayerKick _actionKickPlayerFromClan;

	public ClanInfoView()
	{}

	@Override
	public void createPartControl(Composite parent)
	{
		// Empty Group that acts as the Father of all other
		// groups.
		_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_viewer.setContentProvider(ClansManager.getInstance());
		_viewer.setLabelProvider(new WorkbenchLabelProvider());
		ClansManager.getInstance().setViewer(_viewer);
		Platform.getAdapterManager().registerAdapters(_adapterFactory, L2Character.class);
		getSite().setSelectionProvider(_viewer);
		_viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		hookDoubleClickAction();
		setFocus();
		_viewer.refresh();
	}

	private void makeActions()
	{
		_actionShowInfo = new RequestPlayerInformation(getSite().getWorkbenchWindow(), _viewer);
		_actionKickPlayerFromClan = new RequestClanPlayerKick(getSite().getWorkbenchWindow(), _viewer);
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

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				ClanInfoView.this.fillContextMenu(manager);
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
		// ADD FINAL MENU TO THE MANAGER
		manager.add(_actionKickPlayerFromClan);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(_actionKickPlayerFromClan);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public void setFocus()
	{
		_viewer.getControl().setFocus();
	}

}

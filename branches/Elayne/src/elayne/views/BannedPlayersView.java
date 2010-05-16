package elayne.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

import elayne.datatables.GetBannedPlayers;
import elayne.model.L2AdapterFactory;
import elayne.model.L2Character;
import elayne.model.L2RootSession;

public class BannedPlayersView extends ViewPart
{
	public static final String ID = "Elayne.views.banned";
	private TreeViewer _treeViewer;
	public L2RootSession _session;
	private IAdapterFactory _adapterFactory = new L2AdapterFactory();

	public BannedPlayersView()
	{
		super();
	}

	@Override
	public void createPartControl(Composite parent)
	{
		// Load all the banned players
		GetBannedPlayers.getInstance().getBannedPlayers(this, false);

		// DEFINE A NEW TREE VIEWER.
		_treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		Platform.getAdapterManager().registerAdapters(_adapterFactory, L2Character.class);

		getSite().setSelectionProvider(_treeViewer);

		_treeViewer.setLabelProvider(new WorkbenchLabelProvider());

		_treeViewer.setContentProvider(new BaseWorkbenchContentProvider());

		_treeViewer.setInput(_session.getRoot());

		hookContextMenu();
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
				BannedPlayersView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(_treeViewer.getControl());
		_treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, _treeViewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
	// manager.add(addBannedPlayer);
	// manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager)
	{
	// manager.add(addBannedPlayer);
	// Other plug-ins can contribute there actions here
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
	// manager.add(addBannedPlayer);
	}

	@Override
	public void setFocus()
	{
		_treeViewer.getControl().setFocus();
	}

	@Override
	public void dispose()
	{
		Platform.getAdapterManager().unregisterAdapters(_adapterFactory);
		super.dispose();
	}
}

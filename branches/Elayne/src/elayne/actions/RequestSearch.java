/**
 * 
 */
package elayne.actions;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.dialogs.SearchDialog;
import elayne.preferences.GeneralPreferencePage;
import elayne.views.SearchView;

/**
 * @author polbat02
 */
public class RequestSearch extends ElayneAction
{
	private static final String ID = "requestSearch";

	/**
	 * @param window
	 */
	public RequestSearch(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("&Search");
		setToolTipText("Search for something in the server");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SEARCH));
		setAccelerator(SWT.CTRL | 'H');
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		SearchDialog dialog = new SearchDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		int code = dialog.open();
		if (code == Window.CANCEL)
			return;
		if (code == Window.OK)
		{
			String searchField = dialog.getSearchInput();
			int searchType = dialog.getSearchType();

			IEclipsePreferences prefs = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

			if (searchField != null && searchField != "")
				prefs.put(GeneralPreferencePage.SEARCH_INPUT, searchField);
			if (searchType != 0)
				prefs.putInt(GeneralPreferencePage.SEARCH_TYPE, searchType);

			IWorkbenchPage page = _window.getActivePage();
			try
			{
				IViewPart view = page.findView(SearchView.ID);
				if (view != null)
				{
					SearchView searchView = ((SearchView) view);
					searchView.dispose();
					page.hideView(searchView);
				}
				page.showView(SearchView.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{

	}
}

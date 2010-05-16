package elayne.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.application.Activator;
import elayne.views.PlayerInfoView;

/**
 * This Class is used to control the Details of the Players of the Program
 * Owner.<br>
 * @author polbat02
 */
public class ShowPlayerInfoView extends Action implements IWorkbenchAction
{
	public static final String ID = "elayne.actions.showPlayerInfoView";
	private final IWorkbenchWindow _window;

	public ShowPlayerInfoView(IWorkbenchWindow window)
	{
		_window = window;
		setId(ID);
		setText("&Player Details");
		setToolTipText("Show the details of the last player browsed for.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/stats.png"));
	}

	public void dispose()
	{}

	@Override
	public void run()
	{
		IWorkbenchPage page = _window.getActivePage();
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

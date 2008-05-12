package elayne.ations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.views.OnlinePlayersView;

/**
 * @author polbat02
 */
public class ShowOnlinePlayersView extends Action implements IWorkbenchAction
{
	public final static String ID = "elayne.actions.showOnlinePlayersView";
	private final IWorkbenchWindow window;

	public ShowOnlinePlayersView(IWorkbenchWindow window)
	{
		this.window = window;
		setId(ID);
		setText("&Online Players");
		setToolTipText("Show the players that are currentlly playing");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ONLINE_GROUP));
	}

	public void dispose()
	{}

	@Override
	public void run()
	{
		IWorkbenchPage page = window.getActivePage();
		try
		{
			page.showView(OnlinePlayersView.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}
}

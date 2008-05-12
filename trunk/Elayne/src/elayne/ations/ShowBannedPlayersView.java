package elayne.ations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.views.BannedPlayersView;

/**
 * @author polbat02
 */
public class ShowBannedPlayersView extends Action implements IWorkbenchAction
{
	public final static String ID = "elayne.actions.showBannedPlayersView";
	private final IWorkbenchWindow window;

	public ShowBannedPlayersView(IWorkbenchWindow window)
	{
		this.window = window;
		setId(ID);
		setText("&Banned Players");
		setToolTipText("Show Banned Players");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.BAN_PLAYER));

	}

	public void dispose()
	{}

	@Override
	public void run()
	{
		IWorkbenchPage page = window.getActivePage();
		try
		{
			page.showView(BannedPlayersView.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}
}

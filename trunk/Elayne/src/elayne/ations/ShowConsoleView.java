package elayne.ations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * This Class is used to control the Details of the Players of the Program
 * Owner.<br>
 * @author polbat02
 */
public class ShowConsoleView extends Action implements IWorkbenchAction
{
	public final static String ID = "elayne.actions.showConsoleView";
	private final IWorkbenchWindow window;

	public ShowConsoleView(IWorkbenchWindow window)
	{
		this.window = window;
		setId(ID);
		setText("&Console");
		setToolTipText("Shows the console of the program.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.NAME));
	}

	public void dispose()
	{}

	@Override
	public void run()
	{
		IWorkbenchPage page = window.getActivePage();
		try
		{
			page.showView("org.eclipse.ui.console.ConsoleView");
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}
}

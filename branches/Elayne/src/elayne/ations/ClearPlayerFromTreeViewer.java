package elayne.ations;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.instancemanager.PlayersManager;
import elayne.model.instance.L2PcInstance;

public class ClearPlayerFromTreeViewer extends ElayneAction
{

	public final static String ID = "clearPlayerFromTreeViewer";

	public ClearPlayerFromTreeViewer(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("Remove");
		setToolTipText("Remove player from this view.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.DELETE));
	}

	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2PcInstance player = ((L2PcInstance) obj);
		PlayersManager.getInstance().removePlayer(player, false);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);
		}
		else
			setEnabled(false);
	}

}

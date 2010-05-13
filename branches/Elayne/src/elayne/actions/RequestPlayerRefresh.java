package elayne.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.actions.util.ActionWaiter;
import elayne.application.Activator;
import elayne.model.L2Character;
import elayne.model.instance.L2PcInstance;

public class RequestPlayerRefresh extends ElayneAction
{

	private static final String ID = "requestPlayerRefresh";
	private ActionWaiter waiter = new ActionWaiter(this);

	public RequestPlayerRefresh(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("&Refresh Player");
		setToolTipText("Refresh a player information since the last time it was checked in this session.");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.REFRESH));
	}

	@Override
	public void run()
	{
		final L2PcInstance player = (L2PcInstance) selection.getFirstElement();
		for (L2Character entry : player.getEntries())
			player.removeEntry(entry);
		getTreeViewer().refresh();
		ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
		progress.setCancelable(true);
		try
		{
			progress.run(true, true, new IRunnableWithProgress()
			{
				public void run(IProgressMonitor monitor) throws InvocationTargetException
				{
					try
					{
						monitor.beginTask("Refreshing player information...", IProgressMonitor.UNKNOWN);
						player.restore();
						player.fillStats(player.getParent(), true);
						monitor.done();
					}
					catch (Exception e)
					{
						monitor.done();
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{

		}
		getTreeViewer().refresh();
		waiter.actionWait(120);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (!waiter.isEnabled())
			setEnabled(false);
		else if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2PcInstance);
		}
		else
			setEnabled(false);
	}

}

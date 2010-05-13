/**
 * 
 */
package elayne.actions;

import java.sql.PreparedStatement;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.L2Character;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2Inventory;
import elayne.model.instance.L2PcInstance;
import elayne.templates.L2InventoryItem;
import elayne.util.connector.ServerDB;

/**
 * @author polbat02
 */
public class RequestItemInventoryWipe extends ElayneAction
{

	public final static String ID = "requestItemInventoryWipe";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestItemInventoryWipe(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Wipe Inventory");
		setToolTipText("Wipes a player's inventory");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.WIPE_INVENTORY));
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2PcInstance player = null;
		if (obj instanceof L2Inventory)
			player = ((L2Inventory) obj).getParent();
		else if (obj instanceof L2PcInstance)
			player = (L2PcInstance) obj;
		if (player == null)
		{
			sendMessage("Error! Could not find the parent player!");
			return;
		}
		if (sendConfirmationMessage("Wipe Inventory", "Are you sure that you whipe this inventory?"))
		{
			if (player.getInventory() != null)
			{
				if (player.isOnline())
				{
					sendMessage("Can't delete an item from an Online Player");
					return;
				}
				L2Inventory inv = player.getInventory();
				for (L2InventoryItem item : inv.getAllItems())
					inv.getItemsMap().remove(item.getObjectId());

				for (L2Character p : player.getEntries())
				{
					if (!(p instanceof L2GroupEntry))
						continue;
					if (p.getName().startsWith("Inventory"))
					{
						player.removeEntry(p);
					}
				}
				wipeInventory(player.getObjectId());
				treeViewer.refresh();
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
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && (selection.getFirstElement() instanceof L2PcInstance || selection.getFirstElement() instanceof L2Inventory));
		}
		// Not enable the action.
		else
			setEnabled(false);
	}

	private void wipeInventory(int objectId)
	{
		final String SQL = "DELETE FROM `items` WHERE (`owner_id`=?)";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, objectId);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}

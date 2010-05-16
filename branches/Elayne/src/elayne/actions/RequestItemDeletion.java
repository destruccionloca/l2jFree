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
import elayne.model.instance.L2Inventory;
import elayne.model.instance.L2InventoryEntry;
import elayne.model.instance.L2PcInstance;
import elayne.templates.L2InventoryItem;
import elayne.util.connector.ServerDB;

/**
 * @author polbat02
 */
public class RequestItemDeletion extends ElayneAction
{
	public final static String ID = "requestItemDeletion";

	/**
	 * Constructor.
	 * @param window
	 */
	public RequestItemDeletion(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Delete Item from Inventory");
		setToolTipText("Remove item from Inventory");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.DELETE_ITEM));
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	/**
	 * Checks if the given item (through it's id) is an item that corresponds to
	 * a pet.
	 * @param itemId
	 * @return true if the given item is a pet item.
	 */
	private boolean isPetItem(int itemId)
	{
		return (itemId == 2375 // Wolf
				|| itemId == 9882 || itemId == 10163 || itemId == 10307 // Great Wolf
				|| itemId == 10426 || itemId == 10611 //Fenrir 
				|| itemId == 4425 // Sin Eater
				|| itemId == 3500 || itemId == 3501 || itemId == 3502 // Hatchlings
				|| itemId == 4422 || itemId == 4423 || itemId == 4424 || itemId == 10308 || itemId == 10309
				|| itemId == 10310 // Striders
				|| itemId == 8663 // Wyvern
				|| itemId == 6648 || itemId == 6649 || itemId == 6650 // Babies
				|| itemId == 10311 || itemId == 10312 || itemId == 10313 // Improved Babies
				|| itemId == 13017 || itemId == 13018 || itemId == 13019 || itemId == 13020 || itemId == 13303 
				|| itemId == 13304 || itemId == 13305 || itemId == 13306 || itemId == 13548 || itemId == 13549 
				|| itemId == 13550 || itemId == 13551 || itemId == 14601 || itemId == 14602 || itemId == 14603 
				|| itemId == 14604); // Korean pets
	}

	private void removeItem(int itemObjectId)
	{
		final String SQL = "DELETE FROM `items` WHERE (`object_id`=?)";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, itemObjectId);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestDeleteItem: An error ocurred while removing an item from the Database: " + e.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void removePet(int itemObjectId)
	{
		java.sql.Connection con = null;
		try
		{ // if it's a pet control item, delete the pet
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, itemObjectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestDeleteItem: An error ocurred while removing a Pet from the DataBase: " + e.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run()
	{
		Object obj = _selection.getFirstElement();
		L2InventoryEntry pie = ((L2InventoryEntry) obj);
		if (sendConfirmationMessage("Remove Item", "Are you sure that you want to delete the item " + pie.getName() + "?"))
		{
			if (pie.getPlayerInfo() == null)
			{
				sendMessage("Error: Player is Null.");
				System.out.println("Player is null.");
				return;
			}
			L2PcInstance player = pie.getPlayerInfo();

			if (pie.getPlayerInfo().getInventory() == null)
			{
				System.out.println("Inventory is NULL!");
				return;
			}
			L2Inventory inv = player.getInventory();
			int objectId = pie.getObjectId();
			if (player.isOnline())
			{
				sendMessage("Can't delete an item from an Online Player");
				return;
			}

			L2InventoryItem item = inv.getItemsMap().get(objectId);

			inv.getItemsMap().remove(objectId);

			// Check if this is a pet.
			if (isPetItem(item.getId()))
				removePet(objectId);
			else
				removeItem(objectId);
			pie.getParent().removeEntry(pie);
			_treeViewer.refresh();
			sendMessage("Item Removed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			_selection = (IStructuredSelection) incoming;
			setEnabled(_selection.size() == 1 && _selection.getFirstElement() instanceof L2InventoryEntry);
		}
		else
			setEnabled(false);
	}
}

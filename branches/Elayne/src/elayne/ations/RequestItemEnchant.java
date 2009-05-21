package elayne.ations;

import java.sql.PreparedStatement;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import elayne.datatables.ItemTable;
import elayne.dialogs.ChangeItemEnchantDialog;
import elayne.instancemanager.PlayersManager;
import elayne.model.instance.L2Inventory;
import elayne.model.instance.L2InventoryEntry;
import elayne.model.instance.L2PcInstance;
import elayne.templates.L2InventoryItem;
import elayne.util.connector.ServerDB;

/**
 * This class manages all the requests regarding item enchant changes.<br>
 * This changes can ONLY affect items that can't be stacked and are weapons/armors items.<br>
 * This class extends {@link ElayneAction}, this way we only need to care about
 * selection, and run of the action.
 * @author SqueezeD
 */
public class RequestItemEnchant extends ElayneAction
{
	/** ID of the Action */
	private static final String ID = "requestItemEnchant";

	/**
	 * Defines a new {@link RequestItemEnchant} instance.
	 * @param window -> The window to which the action will have access.
	 * @param treeViewer -> The tree viewer on which the action will act.
	 */
	public RequestItemEnchant(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Change Item Enchant");
		setToolTipText("Change a item enchant.");
	}

	/**
	 * Checks if the id of an item corresponds to an item that represents a pet.
	 * @param itemId -> The item id to check.
	 * @return -> True if the item represents a pet item.
	 */
	private boolean isPetItem(int itemId)
	{
		return (itemId == 2375 // Wolf
								|| itemId == 9882 // Great Wolf
								|| itemId == 4425 // Sin Eater
								|| itemId == 3500 || itemId == 3501 || itemId == 3502 // HatcLings
								|| itemId == 4422 || itemId == 4423 || itemId == 4424 // StriDers
								|| itemId == 8663 // WyverN
								|| itemId == 6648 || itemId == 6649 || itemId == 6650); // Babies
	}

	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2InventoryEntry pie = ((L2InventoryEntry) obj);
		// Define a new ChangeItemEnchantDialog and open it.
		// The dialog will make the user choose the new enchant for the selected item.
		ChangeItemEnchantDialog rit = new ChangeItemEnchantDialog(window.getShell(), pie.getEnchantLevel());
		int code = rit.open();
		if (code == Window.CANCEL)
			return;
		if (code == Window.OK)
		{
			// Get the new amount for the item.
			int enchantChange = rit.getNewChangeLevel();

			// Sends a new confirmation label... to confirm that the user actually wants to change the amount of items for
			// This particular item. I doubt this is really needed since the user already confirmed the change
			// in the dialog....
			if (sendConfirmationMessageWithNoLabel("Change enchant", "Do you want to change the enchant of the item by " + enchantChange))
			{
				// Check if the player's online.
				if (pie.getPlayerInfo() != null && pie.getPlayerInfo().isOnline())
				{
					sendError("This item belongs to a player that is online. Changes can not be made to an online player.");
					return;
				}

				// Pet items can not be modified.
				if (isPetItem(pie.getItemId()))
				{
					sendError("This item is a pet. Cannot proceed.");
					return;
				}

				// Proceed to the item enchant change in the database!
				// FIXME: check if the item can or can not be stacked!
				updateItemEnchant(enchantChange, pie.getObjectId());

				// Set the new amount for the L2InventoryEntry, we will now have to change the "actual" inventory of this player in ELAYNE.
				pie.setEnchantLevel(enchantChange);

				// Proceed to changing the enchant for the L2InventoryItem
				L2PcInstance player = pie.getPlayerInfo();
				L2Inventory inventory = player.getInventory();
				if (inventory.getItemsMap().containsKey(pie.getItemId()))
				{
					L2InventoryItem item = inventory.getItemsMap().get(pie.getItemId());
					item.setEnchantLevel(enchantChange);
				}

				// Refresh the viewer
				PlayersManager.getInstance().refreshViewer();
				sendMessage("Enchant changed correctly for the item " + pie.getClearName());
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			selection = (IStructuredSelection) incoming;
			boolean isItemEntry = selection.size() == 1 && selection.getFirstElement() instanceof L2InventoryEntry;
			if (isItemEntry)
			{
				L2InventoryEntry item = (L2InventoryEntry) selection.getFirstElement();
				int id = item.getItemId();
				if(ItemTable.getInstance().isItem(id))
					setEnabled(false);
				else
					setEnabled(true);
			}
			else
				setEnabled(false);
		}
		else
			setEnabled(false);
	}

	/**
	 * Updates the count of an Enchants for a certain item object id.
	 */
	private void updateItemEnchant(int newEnchant, int itemObjId)
	{
		final String SQL = "UPDATE `items` SET `enchant_level`=? WHERE (`object_id`=?)  ";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, newEnchant);
			statement.setInt(2, itemObjId);
			statement.executeUpdate();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestEnchantItem: An error ocurred while enchanting an item : " + e.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{}
		}
	}
}

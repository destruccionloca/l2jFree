package elayne.actions;

import java.rmi.RemoteException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import elayne.datatables.ArmorTable;
import elayne.datatables.ItemTable;
import elayne.datatables.WeaponTable;
import elayne.dialogs.AwardItemDialog;
import elayne.instancemanager.PlayersManager;
import elayne.model.L2Character;
import elayne.model.instance.L2Inventory;
import elayne.model.instance.L2InventoryEntry;
import elayne.model.instance.L2PcInstance;
import elayne.rmi.RemoteAdministrationClient;
import elayne.templates.L2Armor;
import elayne.templates.L2InventoryItem;
import elayne.templates.L2Item;
import elayne.templates.L2Weapon;

public class RequestItemAward extends ElayneAction
{

	public final static String ID = "requestItemAward";

	public RequestItemAward(IWorkbenchWindow window)
	{
		super(window);
		setNewId(ID);
		setText("Add Item To Inventory");
		setToolTipText("Adds an item to a Player's Inventory");
		// TODO Add image descriptor...
	}

	@Override
	public void run()
	{
		/*
		 * Actions that need to be carried out during this action's life time:
		 * 1- Check the user is connected to the RMI server, else stop the
		 * process. 2- Ideally this method is supposed to work for both, online
		 * and off line players, therefore we're not going to be checking if the
		 * player is online or not. 3- Prompt a dialog (which will be rather
		 * complex) in which the user is asked the item that he wants to add
		 * into the inventory of that one particular player. The dialog includes
		 * a complex list that eases the item selection process. In the future,
		 * images could be added in order to help the user decide which item he
		 * wants to add and so on... 4- Once the item is selected, and we have
		 * checked if the item exists (in ELAYNE and only in ELAYNE since
		 * checking it in the server could give us some compatibility problems
		 * with pictures, ItemStats, and information) etc... we'll proceed to
		 * the item hand out. Therefore we'll connect to the server through RMI
		 * and ask the server to spawn a certain item to a player. The server
		 * will be the in charge of the object Id managing and others... A
		 * little complex as well. Maybe the RMI method that well be calling
		 * should return a string defining the action that has been processed.
		 * 5- Notify the ELAYNE user and finish the action run method.
		 */

		// Check if this user is connected to the RMI server.
		if (!isRMIConnected())
		{
			sendError("You're not connected to the server through RMI. Please do so in order to use this action.");
			return;
		}

		// Prompt a new AwardItemDialog to get the user to select which item needs to be added into
		// the inventory of a certain player, and in which amount. Only etc items can have an amount
		// different than 1, this could be changed later on...
		AwardItemDialog r = new AwardItemDialog(_window.getShell());
		int code = r.open();
		if (code == Window.CANCEL)
			return;
		if (code == Window.OK)
		{
			// The id of the item that we're going to add to a player.
			int id = r.getItemId();
			// The amount of items that we're going to add to a player.
			int amount = r.getAmount();
			// Define the type of item this is:
			// 1- Weapon.
			// 2- Armor.
			// 3- Item.
			int type = 0;
			// The item name.
			String itemName = "";
			if (WeaponTable.getInstance().isWeapon(id))
			{
				itemName = WeaponTable.getInstance().getWeapon(id).getName();
				type = 1;
				// Only one weapon can be given.
				if (amount > 1)
				{
					sendMessage("Only one weapon can be handed out at once.");
					amount = 1;
				}
			}
			else if (ArmorTable.getInstance().isArmor(id))
			{
				itemName = ArmorTable.getInstance().getArmor(id).getName();
				type = 2;
				// Only one armor can be given.
				if (amount > 1)
				{
					sendMessage("Only one armor can be handed out at once.");
					amount = 1;
				}
			}
			else if (ItemTable.getInstance().isItem(id))
			{
				itemName = ItemTable.getInstance().getItem(id).getName();
				type = 3;
			}

			if (sendConfirmationMessage("Confirm", "Are you sure that you want to award this player with " + amount + " " + itemName + "(s)?"))
			{
				L2PcInstance player = null;
				if (_selection.getFirstElement() instanceof L2PcInstance)
					player = ((L2PcInstance) _selection.getFirstElement());

				else if (_selection.getFirstElement() instanceof L2Inventory)
					player = ((L2Inventory) _selection.getFirstElement()).getParent();

				try
				{
					if (player == null)
						return;

					L2Inventory inventory = player.getInventory();

					int objectId = 0;

					// This check looks if the item is already in the inventory and is StackAble,
					// if that's the case, the process is stopped, and no RMI connection is issued. 
					if (ItemTable.getInstance().isItem(id))
					{
						for (L2Character oldItem : inventory.getEtcItems().getEntries())
						{
							if (oldItem instanceof L2InventoryEntry)
							{
								if (((L2InventoryEntry) oldItem).getClearName().toLowerCase().equals(ItemTable.getInstance().getItem(id).getName().toLowerCase()))
								{
									sendError("This etc item (non weapon / non armor) is already in the Inventory, "
															+ "please use the action that is used to change the amount of an Item to change this item's amount.");
									return;
								}
							}
						}
					}

					// This is the place in which we contact the server to issue the Item hand out.
					objectId = RemoteAdministrationClient.getInstance().awardItemToPlayer(player.getName(), id, amount);

					if (objectId != 0)
					{
						// Here the player has already gotten the new item inside the server.
						// Now it's time to add the item to the inventory of the player in
						// ELAYNE.

						// Weapons:
						if (type == 1)
						{
							L2Weapon weapon = WeaponTable.getInstance().getWeapon(id);
							L2InventoryEntry pie = new L2InventoryEntry(inventory.getWeapons(), player, weapon.getName(), "Weapon", id, 0, 1, "INVENTORY", objectId);
							inventory.getWeapons().addEntry(pie);
							inventory.getItemsMap().put(objectId, new L2InventoryItem(id, objectId, 1, "INVENTORY", amount));
						}
						// Armors:
						else if (type == 2)
						{
							L2Armor armor = ArmorTable.getInstance().getArmor(id);
							L2InventoryEntry pie = new L2InventoryEntry(inventory.getArmors(), player, armor.getName(), "Armor", id, 0, 1, "INVENTORY", objectId);
							inventory.getArmors().addEntry(pie);
							inventory.getItemsMap().put(objectId, new L2InventoryItem(id, objectId, 1, "INVENTORY", amount));
						}
						// etc Items:
						else if (type == 3)
						{
							L2Item item = ItemTable.getInstance().getItem(id);
							L2InventoryEntry pie = new L2InventoryEntry(inventory.getEtcItems(), player, item.getName(), "Item", id, 0, 1, "INVENTORY", objectId);
							inventory.getEtcItems().addEntry(pie);
							inventory.getItemsMap().put(objectId, new L2InventoryItem(id, objectId, 1, "INVENTORY", amount));
						}

						PlayersManager.getInstance().refreshViewer();
					}
					else
						sendError("The item could not be handed out.");
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			else
				System.out.println("Change discarted.");
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			_selection = (IStructuredSelection) incoming;
			setEnabled(_selection.size() == 1 && (_selection.getFirstElement() instanceof L2PcInstance || _selection.getFirstElement() instanceof L2Inventory));
		}
		else
			setEnabled(false);
	}

}

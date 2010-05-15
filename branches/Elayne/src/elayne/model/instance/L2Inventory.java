package elayne.model.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;

import javolution.util.FastMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.ArmorTable;
import elayne.datatables.ItemTable;
import elayne.datatables.WeaponTable;
import elayne.templates.L2InventoryItem;
import elayne.templates.L2Weapon;
import elayne.util.connector.ServerDB;

public class L2Inventory extends L2GroupEntry
{

	private static final String RESTORE_CHARACTER_ITEMS = "SELECT object_id, item_id, count, enchant_level, loc FROM `items` WHERE `owner_id` =? ORDER BY loc LIMIT 0,290";

	/**
	 * ALL ITEMS IN THE INV: Key = Item Id, Object = Item itself
	 */
	private FastMap<Integer, L2InventoryItem> _all_items = new FastMap<Integer, L2InventoryItem>();

	private L2RegularGroup _weapons;

	private L2RegularGroup _armors;

	private L2RegularGroup _items;

	public L2Inventory(L2PcInstance parent, String name)
	{
		super(parent, name);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.INVENTORY_GROUP);
	}

	public Set<Integer> getInventoryObjectsId()
	{
		return _all_items.keySet();
	}

	public Collection<L2InventoryItem> getAllItems()
	{
		return _all_items.values();
	}

	public FastMap<Integer, L2InventoryItem> getItemsMap()
	{
		return _all_items;
	}

	@Override
	public L2PcInstance getParent()
	{
		return (L2PcInstance) _parent;
	}

	public boolean isArmor(L2InventoryItem item)
	{
		return ArmorTable.getInstance().isArmor(item.getId());
	}

	public boolean isItem(L2InventoryItem item)
	{
		return ItemTable.getInstance().isItem(item.getId());
	}

	public boolean isWeapon(L2InventoryItem item)
	{
		return WeaponTable.getInstance().isWeapon(item.getId());
	}

	public void restore()
	{
		restoreItems(getParent().getObjectId());

		// WEAPONS --> Define a new Player Group in which we will include all the Weapon type of items.
		_weapons = new L2RegularGroup(this, "Weapons");
		// Add the new Player Group in the entry (which is an instance of a Player Group).
		addEntry(_weapons);
		// ARMORS --> Read on Weapons for Comments.
		_armors = new L2RegularGroup(this, "Armors");
		addEntry(_armors);
		// ITEMS --> Read on Weapons for Comments.
		_items = new L2RegularGroup(this, "Items");
		addEntry(_items);

		for (L2InventoryItem item : getAllItems())
		{
			if (isWeapon(item))
			{
				L2Weapon weapon = WeaponTable.getInstance().getWeapon(item.getId());

				L2InventoryEntry pie = new L2InventoryEntry(_weapons, getParent(), weapon.getName(), "Weapon", item.getId(), item.getEnchantLevel(), 1, item.getLocation(), item.getObjectId());
				_weapons.addEntry(pie);
			}

			else if (isArmor(item))
			{
				L2InventoryEntry pie = new L2InventoryEntry(_armors, getParent(), ArmorTable.getInstance().getArmor(item.getId()).getName(), "Armor", item.getId(), item.getEnchantLevel(), 1, item.getLocation(), item.getObjectId());
				_armors.addEntry(pie);
			}
			else if (isItem(item))
			{
				L2InventoryEntry pie = new L2InventoryEntry(_items, getParent(), ItemTable.getInstance().getItem(item.getId()).getName(), "Item", item.getId(), 0, item.getAmount(), item.getLocation(), item.getObjectId());
				_items.addEntry(pie);
			}
		}

		if (_weapons.getEntries().length == 0)
			removeEntry(_weapons);
		if (_armors.getEntries().length == 0)
			removeEntry(_armors);
		if (_items.getEntries().length == 0)
			removeEntry(_items);
		if (getEntries().length != 0)
		{
			getParent().addEntry(this);
		}
		else
		{
			getParent().removeEntry(this);
			System.out.println("The player " + getParent().getName() + " has an empty inventory.");
		}

	}

	public void restoreItems(int objectId)
	{
		if (!_all_items.isEmpty())
			return;
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER_ITEMS);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();
			int results = 0;
			while (rset.next())
			{
				int itemId = rset.getInt("item_id");
				int enchant = rset.getInt("enchant_level");
				String location = rset.getString("loc");
				int object_id = rset.getInt("object_id");
				if (WeaponTable.getInstance().isWeapon(itemId)) // This is a Weapon instance
				{
					L2InventoryItem item = new L2InventoryItem(itemId, object_id, enchant, location, 1);
					_all_items.put(object_id, item);
				}
				else if (ArmorTable.getInstance().isArmor(itemId)) // This is an Armor instance
				{
					L2InventoryItem item = new L2InventoryItem(itemId, object_id, enchant, location, 1);
					_all_items.put(object_id, item);
				}
				else if (ItemTable.getInstance().isItem(itemId)) // This is an Item instance
				{
					int amount = rset.getInt("count");
					L2InventoryItem item = new L2InventoryItem(itemId, object_id, enchant, location, amount);
					_all_items.put(object_id, item);
				}
				results++;
			}

			System.out.println("L2Inventory: " + results + " Items found in the inventory of the objectId: " + objectId);
			results = 0;
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
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

	public L2RegularGroup getWeapons()
	{
		return _weapons;
	}

	public void setWeapons(L2RegularGroup weapons)
	{
		_weapons = weapons;
	}

	public L2RegularGroup getArmors()
	{
		return _armors;
	}

	public void setArmors(L2RegularGroup armors)
	{
		_armors = armors;
	}

	public void setItems(L2RegularGroup items)
	{
		_items = items;
	}

	public L2RegularGroup getEtcItems()
	{
		return _items;
	}
}

package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.DetailedItemTable;
import elayne.templates.L2PictureLocation;

public class L2InventoryEntry extends L2GroupEntry
{
	private int _amount;

	private int _enchantLevel;

	private final L2GroupEntry _group;

	public final int _itemId;

	private String _location;

	public final String _name;

	private int _objectId;

	private final L2PcInstance _playerInfo;

	public final String _type;

	public L2InventoryEntry(L2GroupEntry group, L2PcInstance player, String name, String type, int itemId, int enchantLevel, int amount, String location, int objectId)
	{
		super(group, name);
		_group = group;
		_playerInfo = player;
		_name = name;
		_type = type;
		_itemId = itemId;
		_enchantLevel = enchantLevel;
		_amount = amount;
		_location = location;
		_objectId = objectId;
	}

	public int getAmount()
	{
		return _amount;
	}

	/**
	 * Set a new amount to this {@link L2InventoryEntry}.
	 * @param newAmount
	 */
	public void setAmount(int newAmount)
	{
		_amount = newAmount;
	}
	
	/**
	 * Set a new amount to this {@link L2InventoryEntry}.
	 * @param Enchant Level
	 */
	public void setEnchantLevel(int newEnchantlevel)
	{
		_enchantLevel = newEnchantlevel;
	}

	/**
	 * @return The Enchant Level for this {@link L2InventoryEntry}.
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		if (getItemId() != 0)
		{
			String path = IImageKeys.PIC_NOT_FOUND;
			L2PictureLocation pic_loc = DetailedItemTable.getInstance().getItem(getItemId());
			if (pic_loc != null)
				path = "icons/"+pic_loc.getPicLocation()+".png";
			ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, path);
			if (image != null)
				return image;
		}
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PIC_NOT_FOUND);
	}

	/**
	 * @return The item id of this {@link L2InventoryEntry}.
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * @return The location in terms of what the <code>Items</code> table in
	 * the l2jdb tells us for this {@link L2InventoryEntry}.
	 */
	public String getLocation()
	{
		return _location;
	}

	/**
	 * @return A human readable location depending on the location given by the
	 * getLocation() method.
	 */
	public String getLocName()
	{
		if (getLocation().contains("PAPERDOLL"))
			return "(Equiped)";
		else if (getLocation().contains("WAREHOUSE"))
			return "(Warehouse)";
		else if (getLocation().contains("INVENTORY"))
			return "(Inventory)";
		else if (getLocation().contains("CLANWH"))
			return "(Clan Warehouse)";
		return getLocation();
	}

	@Override
	public String getName()
	{
		if (getAmount() > 1)
			return getAmount() + " " + _name + " " + getLocName();
		if (getEnchantLevel() > 0)
			return "+" + getEnchantLevel() + " " + _name + " " + getLocName();
		return _name + " " + getLocName();
	}

	/**
	 * @return The objectId of the item that this {@link L2InventoryEntry}
	 * represents.
	 */
	public int getObjectId()
	{
		return _objectId;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _group;
	}

	/**
	 * @return The owner in terms of {@link L2PcInstance} of the item that this
	 * {@link L2InventoryEntry} represents.
	 */
	public L2PcInstance getPlayerInfo()
	{
		return _playerInfo;
	}

	/**
	 * @return The type of item that this {@link L2InventoryEntry} represents.
	 */
	public String getType()
	{
		return _type;
	}
}

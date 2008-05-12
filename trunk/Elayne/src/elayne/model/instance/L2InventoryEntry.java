package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.DetailedItemTable;

public class L2InventoryEntry extends L2GroupEntry
{
	private int amount;

	private int enchantLevel;

	private final L2GroupEntry group;

	public final int itemId;

	private String location;

	public final String name;

	private int objectId;

	private final L2PcInstance playerInfo;

	public final String type;

	public L2InventoryEntry(L2GroupEntry group, L2PcInstance player, String name, String type, int itemId, int enchantLevel, int amount, String location, int objectId)
	{
		super(group, name);
		this.group = group;
		this.playerInfo = player;
		this.name = name;
		this.type = type;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
		this.amount = amount;
		this.location = location;
		this.objectId = objectId;
	}

	public int getAmount()
	{
		return amount;
	}

	/**
	 * Set a new amount to this {@link L2InventoryEntry}.
	 * @param newAmount
	 */
	public void setAmount(int newAmount)
	{
		this.amount = newAmount;
	}
	
	/**
	 * Set a new amount to this {@link L2InventoryEntry}.
	 * @param Enchant Level
	 */
	public void setEnchantLevel(int newEnchantlevel)
	{
		this.enchantLevel = newEnchantlevel;
	}

	/**
	 * @return The Enchant Level for this {@link L2InventoryEntry}.
	 */
	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		if (getItemId() != 0)
		{
			String pic_loc = DetailedItemTable.getInstance().getItem(getItemId()).getPicLocation();
			ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/" + pic_loc + ".png");
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
		return itemId;
	}

	/**
	 * @return The location in terms of what the <code>Items</code> table in
	 * the l2jdb tells us for this {@link L2InventoryEntry}.
	 */
	public String getLocation()
	{
		return location;
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
			return getAmount() + " " + name + " " + getLocName();
		if (getEnchantLevel() > 0)
			return "+" + getEnchantLevel() + " " + name + " " + getLocName();
		return name + " " + getLocName();
	}

	/**
	 * @return The objectId of the item that this {@link L2InventoryEntry}
	 * represents.
	 */
	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return group;
	}

	/**
	 * @return The owner in terms of {@link L2PcInstance} of the item that this
	 * {@link L2InventoryEntry} represents.
	 */
	public L2PcInstance getPlayerInfo()
	{
		return playerInfo;
	}

	/**
	 * @return The type of item that this {@link L2InventoryEntry} represents.
	 */
	public String getType()
	{
		return type;
	}
}

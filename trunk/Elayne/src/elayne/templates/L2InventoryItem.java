package elayne.templates;

public class L2InventoryItem
{
	private int id;
	private int objectId;
	private int enchantLevel;
	private String location;
	private int amount;

	public L2InventoryItem(int id, int objectId, int enchantLevel, String location, int amount)
	{
		this.id = id;
		this.objectId = objectId;
		this.enchantLevel = enchantLevel;
		this.location = location;
		this.amount = amount;
	}

	public int getId()
	{
		return id;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	public int setEnchantLevel(int newEnchant)
	{
		return this.enchantLevel = newEnchant;
	}

	public String getLocation()
	{
		return location;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int newAmount)
	{
		this.amount = newAmount;
	}
}

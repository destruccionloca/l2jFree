package elayne.templates;

public class L2InventoryItem
{
	private final int _id;
	private final int _objectId;
	private int _enchantLevel;
	private final String _location;
	private int _amount;
	private String _type;

	public L2InventoryItem(int id, int objectId, int enchantLevel, String location, int amount, String type)
	{
		_id = id;
		_objectId = objectId;
		_enchantLevel = enchantLevel;
		_location = location;
		_amount = amount;
		_type = type;
	}

	public int getId()
	{
		return _id;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public int setEnchantLevel(int newEnchant)
	{
		return _enchantLevel = newEnchant;
	}

	public String getLocation()
	{
		return _location;
	}

	public int getAmount()
	{
		return _amount;
	}

	public void setAmount(int newAmount)
	{
		_amount = newAmount;
	}

	public String getType()
	{
		return _type;
	}
}

package elayne.templates;

public class L2Armor
{
	private final int _itemId;
	private final String _name;
	private final int _weight;
	private final int _price;
	private final String _sellable;
	private final String _type;

	public L2Armor(int itemId, String name, int weight, int price, String sellable, String type)
	{
		_itemId = itemId;
		_name = name;
		_weight = weight;
		_price = price;
		_sellable = sellable;
		_type = type;
	}

	public int getId()
	{
		return _itemId;
	}

	public String getName()
	{
		return _name;
	}

	public int getWeight()
	{
		return _weight;
	}

	public int getPrice()
	{
		return _price;
	}

	public String getSellable()
	{
		return _sellable;
	}

	public String getType()
	{
		return _type;
	}
}

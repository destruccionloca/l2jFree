package elayne.templates;

public class L2Weapon
{
	private final int _id;
	private final String _name;
	private final int _weight;
	private final int _price;
	private final String _sellable;
	private final String _type;

	public L2Weapon(int id, String name, int weight, int price, String sellable, String type)
	{
		_id = id;
		_name = name;
		_weight = weight;
		_price = price;
		_sellable = sellable;
		_type = type;
	}

	public int getId()
	{
		return _id;
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

	public String sellable()
	{
		return _sellable;
	}

	public String getType()
	{
		return _type;
	}
}

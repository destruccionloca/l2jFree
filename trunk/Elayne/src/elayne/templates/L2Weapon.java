package elayne.templates;

public class L2Weapon
{
	private int id;
	private String name;
	private int weight;
	private int price;
	private String sellable;
	private String type;

	public L2Weapon(int id, String name, int weight, int price, String sellable, String type)
	{
		this.id = id;
		this.name = name;
		this.weight = weight;
		this.price = price;
		this.sellable = sellable;
		this.type = type;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public int getWeight()
	{
		return weight;
	}

	public int getPrice()
	{
		return price;
	}

	public String sellable()
	{
		return sellable;
	}

	public String getType()
	{
		return type;
	}
}

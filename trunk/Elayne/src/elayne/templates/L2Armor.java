package elayne.templates;

public class L2Armor
{
	private int itemId;
	private String name;
	private int weight;
	private int price;
	private String sellable;
	private String type;

	public L2Armor(int itemId, String name, int weight, int price, String sellable, String type)
	{
		this.itemId = itemId;
		this.name = name;
		this.weight = weight;
		this.price = price;
		this.sellable = sellable;
		this.type = type;
	}

	public int getId()
	{
		return itemId;
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

	public String getSellable()
	{
		return sellable;
	}

	public String getType()
	{
		return type;
	}
}

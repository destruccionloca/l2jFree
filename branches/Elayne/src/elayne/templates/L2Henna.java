package elayne.templates;

import elayne.IImageKeys;
import elayne.datatables.HennaTable;

public class L2Henna
{
	private final int dyeAmount;
	private final int dyeId;
	private String name;
	private String picLoc;
	private final int price;
	private final int statCON;
	private final int statDEX;
	private final int statINT;
	private final int statMEM;
	private final int statSTR;
	private final int statWIT;
	private final int symbolId;
	private final String symbolName;
	private final int typeplus;
	private final int typesub;
	private final int valueplus;
	private final int valuesub;

	public L2Henna(int symbolId, String symbolName, int dyeId, int dyeAmount, int price, int statINT, int statSTR, int statCON, int statMEM, int statDEX, int statWIT, int typeplus, int valueplus,
							int typesub, int valuesub)
	{
		this.symbolId = symbolId;
		this.symbolName = symbolName;
		this.dyeId = dyeId;
		this.dyeAmount = dyeAmount;
		this.price = price;
		this.statINT = statINT;
		this.statSTR = statSTR;
		this.statCON = statCON;
		this.statMEM = statMEM;
		this.statDEX = statDEX;
		this.statWIT = statWIT;
		this.typeplus = typeplus;
		this.valueplus = valueplus;
		this.typesub = typesub;
		this.valuesub = valuesub;
		setName();
	}

	public int getDyeAmount()
	{
		return dyeAmount;
	}

	public int getDyeId()
	{
		return dyeId;
	}

	public String getName()
	{
		return name;
	}

	public String getPicLoc()
	{
		return picLoc;
	}

	public int getPrice()
	{
		return price;
	}

	public int getStatCON()
	{
		return statCON;
	}

	public int getStatDEX()
	{
		return statDEX;
	}

	public int getStatInt()
	{
		return statINT;
	}

	public int getStatINT()
	{
		return statINT;
	}

	public int getStatMEM()
	{
		return statMEM;
	}

	public int getStatSTR()
	{
		return statSTR;
	}

	public int getStatWIT()
	{
		return statWIT;
	}

	public int getSymbolId()
	{
		return symbolId;
	}

	public String getSymbolName()
	{
		return symbolName;
	}

	private void setName()
	{
		String plusString = "";
		if (typeplus == HennaTable.TYPE_HENNA_CON)
		{
			picLoc = IImageKeys.HENNA_CON;
			plusString = "CON";
		}
		else if (typeplus == HennaTable.TYPE_HENNA_DEX)
		{
			picLoc = IImageKeys.HENNA_DEX;
			plusString = "DEX";
		}
		else if (typeplus == HennaTable.TYPE_HENNA_INT)
		{
			picLoc = IImageKeys.HENNA_INT;
			plusString = "INT";
		}
		else if (typeplus == HennaTable.TYPE_HENNA_MEN)
		{
			picLoc = IImageKeys.HENNA_MEN;
			plusString = "MEN";
		}
		else if (typeplus == HennaTable.TYPE_HENNA_STR)
		{
			picLoc = IImageKeys.HENNA_STR;
			plusString = "STR";
		}
		else if (typeplus == HennaTable.TYPE_HENNA_WIT)
		{
			picLoc = IImageKeys.HENNA_WIT;
			plusString = "WIT";
		}

		String subString = "";
		if (typesub == HennaTable.TYPE_HENNA_CON)
			subString = "CON";
		else if (typesub == HennaTable.TYPE_HENNA_DEX)
			subString = "DEX";
		else if (typesub == HennaTable.TYPE_HENNA_INT)
			subString = "INT";
		else if (typesub == HennaTable.TYPE_HENNA_MEN)
			subString = "MEN";
		else if (typesub == HennaTable.TYPE_HENNA_STR)
			subString = "STR";
		else if (typesub == HennaTable.TYPE_HENNA_WIT)
			subString = "WIT";

		name = plusString + " +" + valueplus + " - " + subString + " " + valuesub;
	}
}

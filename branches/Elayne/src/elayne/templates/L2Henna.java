package elayne.templates;

import elayne.IImageKeys;
import elayne.datatables.HennaTable;

public class L2Henna
{
	private final int _dyeAmount;
	private final int _dyeId;
	private String _name;
	private String _picLoc;
	private final int _price;
	private final int _statCON;
	private final int _statDEX;
	private final int _statINT;
	private final int _statMEM;
	private final int _statSTR;
	private final int _statWIT;
	private final int _symbolId;
	private final String _symbolName;
	private final int _typeplus;
	private final int _typesub;
	private final int _valueplus;
	private final int _valuesub;

	public L2Henna(int symbolId, String symbolName, int dyeId, int dyeAmount, int price, int statINT, int statSTR, int statCON, int statMEM, int statDEX, int statWIT, int typeplus, int valueplus, int typesub, int valuesub)
	{
		_symbolId = symbolId;
		_symbolName = symbolName;
		_dyeId = dyeId;
		_dyeAmount = dyeAmount;
		_price = price;
		_statINT = statINT;
		_statSTR = statSTR;
		_statCON = statCON;
		_statMEM = statMEM;
		_statDEX = statDEX;
		_statWIT = statWIT;
		_typeplus = typeplus;
		_valueplus = valueplus;
		_typesub = typesub;
		_valuesub = valuesub;
		setName();
	}

	public int getDyeAmount()
	{
		return _dyeAmount;
	}

	public int getDyeId()
	{
		return _dyeId;
	}

	public String getName()
	{
		return _name;
	}

	public String getPicLoc()
	{
		return _picLoc;
	}

	public int getPrice()
	{
		return _price;
	}

	public int getStatCON()
	{
		return _statCON;
	}

	public int getStatDEX()
	{
		return _statDEX;
	}

	public int getStatInt()
	{
		return _statINT;
	}

	public int getStatINT()
	{
		return _statINT;
	}

	public int getStatMEM()
	{
		return _statMEM;
	}

	public int getStatSTR()
	{
		return _statSTR;
	}

	public int getStatWIT()
	{
		return _statWIT;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public String getSymbolName()
	{
		return _symbolName;
	}

	private void setName()
	{
		String plusString = "";
		if (_typeplus == HennaTable.TYPE_HENNA_CON)
		{
			_picLoc = IImageKeys.HENNA_CON;
			plusString = "CON";
		}
		else if (_typeplus == HennaTable.TYPE_HENNA_DEX)
		{
			_picLoc = IImageKeys.HENNA_DEX;
			plusString = "DEX";
		}
		else if (_typeplus == HennaTable.TYPE_HENNA_INT)
		{
			_picLoc = IImageKeys.HENNA_INT;
			plusString = "INT";
		}
		else if (_typeplus == HennaTable.TYPE_HENNA_MEN)
		{
			_picLoc = IImageKeys.HENNA_MEN;
			plusString = "MEN";
		}
		else if (_typeplus == HennaTable.TYPE_HENNA_STR)
		{
			_picLoc = IImageKeys.HENNA_STR;
			plusString = "STR";
		}
		else if (_typeplus == HennaTable.TYPE_HENNA_WIT)
		{
			_picLoc = IImageKeys.HENNA_WIT;
			plusString = "WIT";
		}

		String subString = "";
		if (_typesub == HennaTable.TYPE_HENNA_CON)
			subString = "CON";
		else if (_typesub == HennaTable.TYPE_HENNA_DEX)
			subString = "DEX";
		else if (_typesub == HennaTable.TYPE_HENNA_INT)
			subString = "INT";
		else if (_typesub == HennaTable.TYPE_HENNA_MEN)
			subString = "MEN";
		else if (_typesub == HennaTable.TYPE_HENNA_STR)
			subString = "STR";
		else if (_typesub == HennaTable.TYPE_HENNA_WIT)
			subString = "WIT";

		_name = plusString + " +" + _valueplus + " - " + subString + " " + _valuesub;
	}
}

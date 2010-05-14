package elayne.templates;

public class L2CharacterTemplate
{
	private final int _classId;
	private final String _className;
	private int _raceId;
	private int _str;
	private int _con;
	private int _dex;
	private int _int_;
	private int _wit;
	private int _men;

	public L2CharacterTemplate(int classId, String className)
	{
		_classId = classId;
		_className = className;
	}

	public void setParameter(String name, String val)
	{
		if (name.equals("RaceId"))
			_raceId = Integer.parseInt(val);
		else if (name.equals("STR"))
			_str = Integer.parseInt(val);
		else if (name.equals("CON"))
			_con = Integer.parseInt(val);
		else if (name.equals("DEX"))
			_dex = Integer.parseInt(val);
		else if (name.equals("_INT"))
			_int_ = Integer.parseInt(val);
		else if (name.equals("WIT"))
			_wit = Integer.parseInt(val);
		else if (name.equals("MEN"))
			_men = Integer.parseInt(val);
	}

	public int getClassId()
	{
		return _classId;
	}

	public String getClassName()
	{
		return _className;
	}

	public int getRaceId()
	{
		return _raceId;
	}

	public int getStr()
	{
		return _str;
	}

	public int getCon()
	{
		return _con;
	}

	public int getDex()
	{
		return _dex;
	}

	public int getInt()
	{
		return _int_;
	}

	public int getWit()
	{
		return _wit;
	}

	public int getMen()
	{
		return _men;
	}
}

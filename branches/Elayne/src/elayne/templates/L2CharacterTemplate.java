package elayne.templates;

public class L2CharacterTemplate
{
	private int classId;
	private String className;
	private int raceId;
	private int str;
	private int con;
	private int dex;
	private int int_;
	private int wit;
	private int men;

	public L2CharacterTemplate(int classId, String className)
	{
		this.classId = classId;
		this.className = className;
	}

	public void setParameter(String name, String val)
	{
		if (name.equals("RaceId"))
			raceId = Integer.parseInt(val);
		if (name.equals("STR"))
			str = Integer.parseInt(val);
		if (name.equals("CON"))
			con = Integer.parseInt(val);
		if (name.equals("DEX"))
			dex = Integer.parseInt(val);
		if (name.equals("_INT"))
			int_ = Integer.parseInt(val);
		if (name.equals("WIT"))
			wit = Integer.parseInt(val);
		if (name.equals("MEN"))
			men = Integer.parseInt(val);
	}

	public int getClassId()
	{
		return classId;
	}

	public String getClassName()
	{
		return className;
	}

	public int getRaceId()
	{
		return raceId;
	}

	public int getStr()
	{
		return str;
	}

	public int getCon()
	{
		return con;
	}

	public int getDex()
	{
		return dex;
	}

	public int getInt()
	{
		return int_;
	}

	public int getWit()
	{
		return wit;
	}

	public int getMen()
	{
		return men;
	}
}

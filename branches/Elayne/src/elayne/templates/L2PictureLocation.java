package elayne.templates;

public class L2PictureLocation
{
	private final int _itemId;
	private final String _name;
	private final String _grade;
	private final String _pic_loc;

	public L2PictureLocation(int itemId, String name, String grade, String pic_loc)
	{
		_itemId = itemId;
		_name = name;
		_grade = grade;
		_pic_loc = pic_loc;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public String getName()
	{
		return _name;
	}

	public String getGrade()
	{
		return _grade;
	}

	public String getPicLocation()
	{
		return _pic_loc;
	}
}

package elayne.templates;

public class L2PictureLocation
{
	private int itemId;
	private String name;
	private String grade;
	private String pic_loc;

	public L2PictureLocation(int itemId, String name, String grade, String pic_loc)
	{
		this.itemId = itemId;
		this.name = name;
		this.grade = grade;
		this.pic_loc = pic_loc;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getName()
	{
		return name;
	}

	public String getGrade()
	{
		return grade;
	}

	public String getPicLocation()
	{
		return pic_loc;
	}
}

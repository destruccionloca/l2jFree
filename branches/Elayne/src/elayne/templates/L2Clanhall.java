package elayne.templates;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2Clanhall
{
	private final int _clanhallId;
	private final String _name;
	private final int _ownerId;
	private final int _lease;
	private final String _location;
	private final int _grade;
	private final String _desc;

	public L2Clanhall(int clanhallId, String name, int ownerId, int lease, String desc, String location, int grade)
	{
		_clanhallId = clanhallId;
		_name = name;
		_ownerId = ownerId;
		_lease = lease;
		_desc = desc;
		_location = location;
		_grade = grade;
	}

	public int getClanhallId()
	{
		return _clanhallId;
	}

	public String getName()
	{
		return _name;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public int getLease()
	{
		return _lease;
	}

	public String getLocation()
	{
		return _location;
	}

	public int getGrade()
	{
		return _grade;
	}

	public String getDesc()
	{
		return _desc;
	}
}

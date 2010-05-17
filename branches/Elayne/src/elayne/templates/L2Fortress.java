package elayne.templates;

import java.util.Calendar;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2Fortress
{
	private final int _id;
	private final String _name;
	private Calendar _time;
	private final int _owner;
	private final int _type;
	private String _state;
	private final int _castleId;
	private final String _castleName;

	public L2Fortress(int id, String name, long time, int owner, int type, int state, int castleId, String castleName)
	{
		_id = id;
		_name = name;
		_owner = owner;
		_type = type;
		_castleId = castleId;
		_castleName = castleName;

		setTime(time);
		setState(state);
	}

	private void setTime(long time)
	{
		_time = Calendar.getInstance();
		_time.setTimeInMillis(time);
	}

	private void setState(int state)
	{
		switch (state)
		{
			case 1:
				_state = "Independant";
				break;
			case 2:
				_state = "Contracted";
				break;
			default:
				_state = "Unknown";
		}
	}

	public int getFortId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getOwner()
	{
		return _owner;
	}

	public int getType()
	{
		return _type;
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public String getState()
	{
		return _state;
	}

	public String getTime()
	{
		return String.valueOf(_time.getTime());
	}

	public String getOwningCastleName()
	{
		return _castleName;
	}
}

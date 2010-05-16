package elayne.templates;

import java.util.Calendar;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2Castle
{
	private final int _id;
	private final String _name;
	private final int _tax;
	private final double _treasury;
	private Calendar _siegeDate;
	private int _owner  = 0;

	public L2Castle(int id, String name, int tax, double treasury, long siegeDate)
	{
		_id = id;
		_name = name;
		_tax = tax;
		_treasury = treasury;
		setSiegeDate(siegeDate);
	}

	private void setSiegeDate(long time)
	{
		_siegeDate = Calendar.getInstance();
		_siegeDate.setTimeInMillis(time);
	}

	public int getCastleId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getTax()
	{
		return _tax;
	}

	public double getTreasury()
	{
		return _treasury;
	}

	public String getSiegeDate()
	{
		return String.valueOf(_siegeDate.getTime());
	}

	public void setOwner(int clanId)
	{
		_owner = clanId;
	}

	public int getOwner()
	{
		return _owner;
	}
}

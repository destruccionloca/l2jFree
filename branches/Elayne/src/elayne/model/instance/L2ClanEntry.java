package elayne.model.instance;

import elayne.model.L2Character;

/**
 * Represents a label that is a children of a Clan.
 * @author polbat02
 */
public class L2ClanEntry extends L2Character
{
	// ================================
	// DATA FIELD
	private String _field;
	private String _name;
	private L2Clan _parent;

	// ================================
	// CONSTRUCTOR
	/**
	 * Constructor. Define and save this labels information.
	 * @param parent
	 * @param name
	 * @param field
	 */
	public L2ClanEntry(L2Clan parent, String name, String field)
	{
		super();
		_parent = parent;
		_name = name;
		_field = field;
	}

	@Override
	public String getName()
	{
		return _name + ": " + _field;
	}

	@Override
	public L2Clan getParent()
	{
		return _parent;
	}

	public void setField(String field)
	{
		_field = field;
	}
}

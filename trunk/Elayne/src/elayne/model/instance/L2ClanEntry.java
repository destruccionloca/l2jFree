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
	private String field;
	private String name;
	private L2Clan parent;

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
		this.parent = parent;
		this.name = name;
		this.field = field;
	}

	@Override
	public String getName()
	{
		return name + ": " + field;
	}

	@Override
	public L2Clan getParent()
	{
		return parent;
	}

	public void setField(String field)
	{
		this.field = field;
	}
}

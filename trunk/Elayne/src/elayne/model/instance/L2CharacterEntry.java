package elayne.model.instance;

import org.eclipse.jface.viewers.TreeViewer;

import elayne.model.L2Character;

/**
 * This class is the basic class for any L2Character entry. This entries are
 * only labels in a {@link TreeViewer}, but they contain important information
 * for the user. This class contains methods that ease the process of changing a
 * label.
 * @author polbat02
 */
public class L2CharacterEntry extends L2Character
{
	private static final int TYPE_INT = 2;
	private static final int TYPE_LONG = 1;
	private static final int TYPE_STRING = 3;
	// =======================================
	// DATA FIELD
	private String field = null;
	private final L2GroupEntry group;
	private int ifield;
	private long lfield;
	private String name;
	private int type = 0;

	// =======================================
	// CONSTRUCTOR
	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, int field)
	{
		this.group = group;
		this.name = name;
		this.ifield = field;
		type = TYPE_INT;
	}

	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, long field)
	{
		this.group = group;
		this.name = name;
		this.lfield = field;
		type = TYPE_LONG;
	}

	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, String field)
	{
		this.group = group;
		this.name = name;
		this.field = field;
		type = TYPE_STRING;
	}

	// =======================================
	// METHOD PUBLIC
	@Override
	public String getName()
	{
		if (type == TYPE_LONG)
			return name + " " + lfield;
		if (type == TYPE_INT)
			return name + " " + ifield;
		return name + " " + field;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return group;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(int field)
	{
		this.ifield = field;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(long field)
	{
		this.lfield = field;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(String field)
	{
		this.field = field;
	}
}

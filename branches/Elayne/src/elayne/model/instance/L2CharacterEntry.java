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
	private static final int TYPE_DOUBLE = 4;
	// =======================================
	// DATA FIELD
	private String _field = null;
	private final L2GroupEntry _group;
	private int _ifield;
	private long _lfield;
	private double _dfield;
	private String _name;
	private int _type = 0;

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
		_group = group;
		_name = name;
		_ifield = field;
		_type = TYPE_INT;
	}

	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, long field)
	{
		_group = group;
		_name = name;
		_lfield = field;
		_type = TYPE_LONG;
	}

	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, String field)
	{
		_group = group;
		_name = name;
		_field = field;
		_type = TYPE_STRING;
	}

	/**
	 * Defines a new {@link L2CharacterEntry} and saves it's basic information.
	 * @param group
	 * @param name
	 * @param field
	 */
	public L2CharacterEntry(L2GroupEntry group, String name, double field)
	{
		_group = group;
		_name = name;
		_dfield = field;
		_type = TYPE_DOUBLE;
	}

	// =======================================
	// METHOD PUBLIC
	@Override
	public String getName()
	{
		if (_type == TYPE_LONG)
			return _name + " " + _lfield;
		else if (_type == TYPE_INT)
			return _name + " " + _ifield;
		else if (_type == TYPE_DOUBLE)
			return _name + " " + _dfield;
		return _name + " " + _field;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _group;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(int field)
	{
		_ifield = field;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(long field)
	{
		_lfield = field;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(String field)
	{
		_field = field;
	}

	/**
	 * Refreshes a field.
	 * @param field
	 */
	public void setField(double field)
	{
		_dfield = field;
	}
}

package elayne.model.instance;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

import elayne.model.L2Character;

/**
 * Parent class of any Group in ELAYNE. Contains methods to add and remove
 * entries, identify parents and children. The parent will always be a
 * {@link L2GroupEntry} or a sub class, while an entry can be made out of any
 * instance that is a subclass of {@link L2Character}.
 * @author polbat02
 */
public abstract class L2GroupEntry extends L2Character
{
	// ====================================
	// DATA FIELD
	protected List<L2Character> _entries;
	protected String _name;
	protected L2GroupEntry _parent;

	// ====================================
	// CONSTRUCTOR
	/**
	 * CONSTRUCTOR:<br>
	 * Defines a new Group entry that can hold children.<br>
	 * @param parent: The group entry that is the parent of this entry.
	 * @param name: The name of this entry.
	 */
	public L2GroupEntry(L2GroupEntry parent, String name)
	{
		_name = name;
		_parent = parent;
	}

	// ====================================
	// METHOD - PUBLIC
	/**
	 * Adds an entry to this group. If the entries group is null, it will create
	 * a new {@link ArrayList} containing that will contain all the entries of
	 * this group.
	 * @param entry -> The entry that needs to be added into this group.
	 */
	public void addEntry(L2Character entry)
	{
		if (_entries == null)
			_entries = new ArrayList<L2Character>(5);
		_entries.add(entry);
	}

	/**
	 * Removes all the entries of this Group and sets the entries {@link List}
	 * to null.
	 */
	public void clearEntries()
	{
		if (_entries != null && !_entries.isEmpty())
		{
			_entries.clear();
			_entries = null;
		}
	}

	public final String getClearName()
	{
		return _name;
	}

	/**
	 * @return A representation of all the entries that have this group as a
	 * parent. Even if they don't explicitly call this group their parent they
	 * will be listed here as they are part of the entries {@link List}.
	 */
	public L2Character[] getEntries()
	{
		if (_entries != null)
			return _entries.toArray(new L2Character[_entries.size()]);
		return new L2Character[0];
	}

	/**
	 * This method gets and returns an {@link ImageDescriptor} that will define
	 * a group visually.
	 */
	public abstract ImageDescriptor getImageDescriptor();

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _parent;
	}

	/**
	 * Removes an entry from this group. The group may not contain this entry
	 * but that doesn't matter to the void. Once removed, if the entries
	 * {@link List} is empty we'll set the list as null.
	 * @param entry
	 */
	public void removeEntry(L2Character entry)
	{
		if (_entries != null)
		{
			_entries.remove(entry);
			if (_entries.isEmpty())
				_entries = null;
		}
	}

	/**
	 * Rename this Group.<br>
	 * Special care needs to be taken with this method as sometimes the name
	 * shown in a {@link TreeViewer} does not correspond with the name that the
	 * group has.
	 * @param newName
	 */
	public void setName(String name)
	{
		_name = name;
	}
}

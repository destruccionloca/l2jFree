package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2ClanhallGroup extends L2GroupEntry
{

	private static String ID_LABEL = "Clanhall id";
	private static String NAME_LABEL = "Name";
	private static String LEASE_LABEL = "Lease";
	private static String DESC_LABEL = "Description";
	private static String LOCATION_LABEL = "Location";
	private static String GRADE_LABEL = "Grade";

	// ====================================
	// DATA FIELD
	private int _id;
	private L2ClanEntry _idEntry;
	private String _name;
	private L2ClanEntry _nameEntry;
	private int _lease;
	private L2ClanEntry _leaseEntry;
	private String _desc;
	private L2ClanEntry _descEntry;
	private String _location;
	private L2ClanEntry _locationEntry;
	private int _grade;
	private L2ClanEntry _gradeEntry;

	public L2ClanhallGroup(L2Clan parent, int id, String name, int lease, String desc, String location, int grade)
	{
		super(parent, "Clanhall");
		_id = id;
		_name = name;
		_lease = lease;
		_desc = desc;
		_location = location;
		_grade = grade;

		fillEntries();
	}

	/**
	 * Attempts to change a label that is a children of this instance.<br>
	 * @param labelId --> The label id that is going to be changed.
	 * @param newValue --> The new value that's gonna be set.
	 */
	public void changeLabel(String labelId, String newValue)
	{
		if (labelId.equals(ID_LABEL))
		{
			_id = Integer.valueOf(newValue);
			_idEntry.setField(newValue);
		}
		else if (labelId.equals(NAME_LABEL))
		{
			_name = newValue;
			_nameEntry.setField(newValue);
		}
		else if (labelId.equals(LEASE_LABEL))
		{
			_lease = Integer.valueOf(newValue);
			_leaseEntry.setField(newValue);
		}
		else if (labelId.equals(DESC_LABEL))
		{
			_desc = newValue;
			_descEntry.setField(newValue);
		}
		else if (labelId.equals(LOCATION_LABEL))
		{
			_location = newValue;
			_locationEntry.setField(newValue);
		}
		else if (labelId.equals(GRADE_LABEL))
		{
			_grade = Integer.valueOf(newValue);
			_gradeEntry.setField(newValue);
		}
	}

	/**
	 * Fill this group's entries in human readable labels. This labels can be
	 * later modified using the method <code>changeCode(int labelId)</code>.
	 */
	private void fillEntries()
	{
		_idEntry = new L2ClanEntry(getParent(), ID_LABEL, String.valueOf(_id));
		addEntry(_idEntry);
		_nameEntry = new L2ClanEntry(getParent(), NAME_LABEL, _name);
		addEntry(_nameEntry);
		_leaseEntry = new L2ClanEntry(getParent(), LEASE_LABEL, String.valueOf(_lease));
		addEntry(_leaseEntry);
		_descEntry = new L2ClanEntry(getParent(), DESC_LABEL, _desc);
		addEntry(_descEntry);
		_locationEntry = new L2ClanEntry(getParent(), LOCATION_LABEL, _location);
		addEntry(_locationEntry);
		_gradeEntry = new L2ClanEntry(getParent(), GRADE_LABEL, String.valueOf(_grade));
		addEntry(_gradeEntry);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLANHALL);
	}

	@Override
	public String getName()
	{
		return "Clanhall";
	}

	@Override
	public L2Clan getParent()
	{
		return (L2Clan) _parent;
	}
}

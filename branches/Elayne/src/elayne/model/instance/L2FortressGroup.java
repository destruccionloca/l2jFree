package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2FortressGroup extends L2GroupEntry
{

	private static String ID_LABEL = "Fortress id";
	private static String NAME_LABEL = "Name";
	private static String TIME_LABEL = "Owned since";
	private static String TYPE_LABEL = "Type";
	private static String CASTLE_LABEL = "Owning castle";
	private static String STATE_LABEL = "Status";

	// ====================================
	// DATA FIELD
	private int _id;
	private L2ClanEntry _idEntry;
	private String _name;
	private L2ClanEntry _nameEntry;
	private String _time;
	private L2ClanEntry _timeEntry;
	private int _type;
	private L2ClanEntry _typeEntry;
	private String _state;
	private L2ClanEntry _stateEntry;
	private String _castleName;
	private L2ClanEntry _castleNameEntry;

	public L2FortressGroup(L2Clan parent, int id, String name, String time, int type, String state, String castleName)
	{
		super(parent, "Fortress");
		_id = id;
		_name = name;
		_time = time;
		_type = type;
		_state = state;
		_castleName = castleName;

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
		else if (labelId.equals(TIME_LABEL))
		{
			_time = newValue;
			_timeEntry.setField(newValue);
		}
		else if (labelId.equals(TYPE_LABEL))
		{
			_type = Integer.valueOf(newValue);
			_typeEntry.setField(newValue);
		}
		else if (labelId.equals(STATE_LABEL))
		{
			_state = newValue;
			_stateEntry.setField(newValue);
		}
		else if (labelId.equals(CASTLE_LABEL))
		{
			_castleName = newValue;
			_castleNameEntry.setField(newValue);
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
		_timeEntry = new L2ClanEntry(getParent(), TIME_LABEL, _time);
		addEntry(_timeEntry);
		_typeEntry = new L2ClanEntry(getParent(), TYPE_LABEL, String.valueOf(_type));
		addEntry(_typeEntry);
		_stateEntry = new L2ClanEntry(getParent(), STATE_LABEL, _state);
		addEntry(_stateEntry);
		_castleNameEntry = new L2ClanEntry(getParent(), CASTLE_LABEL, _castleName);
		addEntry(_castleNameEntry);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.FORTRESS);
	}

	@Override
	public String getName()
	{
		return "Fortress";
	}

	@Override
	public L2Clan getParent()
	{
		return (L2Clan) _parent;
	}
}

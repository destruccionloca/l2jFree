package elayne.model.instance;

import java.util.Calendar;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

/**
 * Defines a new Castle instance. A castle instance is capable of holding
 * children and will always be a children of an {@link L2Clan}.<br>
 * This class contains methods that contain information about a castle. This it
 * also implements methods to modify it's entries.
 * @author polbat02
 */
public class L2CastleGroup extends L2GroupEntry
{

	private static String SIEGE_DATE = "Siege Date";
	private static String TAX_RATE_LABEL = "Tax Rate";
	private static String TREASURY_LABEL = "Treasury";

	// ====================================
	// DATA FIELD
	private Calendar _siegeDate;
	private L2ClanEntry _siegeDateEntry;
	private int _taxPercent;
	private L2ClanEntry _taxRateEntry;
	private int _treasury;
	private L2ClanEntry _treasuryEntry;

	/**
	 * Constructor:<br>
	 * Defines a new Clan instance while saving it's values. This constructor
	 * must implicitly call an {@link L2Clan} that will be the parent of this
	 * Group.
	 * @param parent --> The parent {@link L2Clan} of this Group.
	 * @param name --> The name of this Castle (a zone).
	 * @param taxPercent --> The tax percent that will be added to everything
	 * bought in a special zone.
	 * @param treasury --> Amount of ADENAS that the castle vault has.
	 * @param siegeDate --> The date of the siege.
	 * @param siegeDayOfWeek --> Day of the siege in a human readable label.
	 * @param siegeHourOfDay --> Hour of the siege in a human readable label.
	 */
	public L2CastleGroup(L2Clan parent, String name, int taxPercent, int treasury, long siegeDate)
	{
		super(parent, name);
		_taxPercent = taxPercent;
		_treasury = treasury;
		_siegeDate = Calendar.getInstance();
		_siegeDate.setTimeInMillis(siegeDate);

		fillEntries();
	}

	/**
	 * Attempts to change a label that is a children of this instance.<br>
	 * @param labelId --> The label id that is going to be changed.
	 * @param newValue --> The new value that's gonna be set.
	 */
	public void changeLabel(String labelId, String newValue)
	{
		if (labelId.equals(TAX_RATE_LABEL))
		{
			_taxPercent = Integer.valueOf(newValue);
			_taxRateEntry.setField(newValue);
		}
		else if (labelId.equals(TREASURY_LABEL))
		{
			_treasury = Integer.valueOf(newValue);
			_treasuryEntry.setField(newValue);
		}
		else if (labelId.equals(SIEGE_DATE))
		{
			_siegeDate.setTimeInMillis(Integer.valueOf(newValue));
			_siegeDateEntry.setField(String.valueOf(_siegeDate.getTime()));
		}
	}

	/**
	 * Fill this group's entries in human readable labels. This labels can be
	 * later modified using the method <code>changeCode(int labelId)</code>.
	 */
	private void fillEntries()
	{
		_taxRateEntry = new L2ClanEntry(getParent(), TAX_RATE_LABEL, String.valueOf(_taxPercent));
		addEntry(_taxRateEntry);
		_treasuryEntry = new L2ClanEntry(getParent(), TREASURY_LABEL, String.valueOf(_treasury) + " Adenas");
		addEntry(_treasuryEntry);
		_siegeDateEntry = new L2ClanEntry(getParent(), SIEGE_DATE, String.valueOf(_siegeDate.getTime()));
		addEntry(_siegeDateEntry);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CASTLE);
	}

	@Override
	public String getName()
	{
		return _name + " Castle";
	}

	@Override
	public L2Clan getParent()
	{
		return (L2Clan) _parent;
	}
}

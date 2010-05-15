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
	private Calendar siegeDate;
	private L2ClanEntry siegeDateEntry;
	private int taxPercent;
	private L2ClanEntry taxRateEntry;
	private int treasury;
	private L2ClanEntry treasuryEntry;

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
	public L2CastleGroup(L2Clan parent, String name, int taxPercent, int treasury, long siegeDate, int siegeDayOfWeek, int siegeHourOfDay)
	{
		super(parent, name);
		this.taxPercent = taxPercent;
		this.treasury = treasury;
		this.siegeDate = Calendar.getInstance();
		this.siegeDate.setTimeInMillis(siegeDate);

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
			taxPercent = Integer.valueOf(newValue);
			taxRateEntry.setField(newValue);
		}
		else if (labelId.equals(TREASURY_LABEL))
		{
			treasury = Integer.valueOf(newValue);
			treasuryEntry.setField(newValue);
		}
		else if (labelId.equals(SIEGE_DATE))
		{
			siegeDate.setTimeInMillis(Integer.valueOf(newValue));
			siegeDateEntry.setField(String.valueOf(siegeDate.getTime()));
		}
	}

	/**
	 * Fill this group's entries in human readable labels. This labels can be
	 * later modified using the method <code>changeCode(int labelId)</code>.
	 */
	private void fillEntries()
	{
		taxRateEntry = new L2ClanEntry(getParent(), TAX_RATE_LABEL, String.valueOf(taxPercent));
		addEntry(taxRateEntry);
		treasuryEntry = new L2ClanEntry(getParent(), TREASURY_LABEL, String.valueOf(treasury) + " Adenas");
		addEntry(treasuryEntry);
		siegeDateEntry = new L2ClanEntry(getParent(), SIEGE_DATE, String.valueOf(siegeDate.getTime()));
		addEntry(siegeDateEntry);
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

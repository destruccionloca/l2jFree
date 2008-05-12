package elayne.model.instance;

import org.eclipse.jface.viewers.TreeViewer;

import elayne.model.L2Character;
import elayne.views.OnlinePlayersView;

/**
 * This class represents a "non-complete" player.<br>
 * This class is only an entry and can not have children. This does not mean
 * that this class contains no information.<br>
 * Some basic information that is used to expand this player's information is
 * stored in this class. When created, this class doesn't have to define a
 * parent implicitly. A parent can be later defined and called. When calling a
 * parent of this class we always need to make sure it isn't null.<br>
 * This class doesn't define a parent because this class is used in a great
 * variety of cases, some of which are not shown in a {@link TreeViewer}, an
 * example of this is the Search View.
 * @author polbat02
 */
public class L2CharacterBriefEntry extends L2Character
{
	// ===================================
	// DATA FIELD
	private int accesslevel;
	private String account;
	private int clanId;
	private boolean isOnline = false;
	private int level;
	private String name;
	private int objectId;
	private L2GroupEntry parent;
	private int sex;

	// ===================================
	// CONSTRUCTOR
	/**
	 * Define a new Brief Character Entry and store some basic information about
	 * it.
	 * @param objectId --> The object Id of this player.
	 * @param level --> Player level.
	 * @param name --> Player Name.
	 * @param account --> Account of this player. Used to expand this players
	 * information if needed.
	 * @param online --> Is this player online?
	 * @param accesslevel --> Player access level (used in
	 * {@link OnlinePlayersView}.
	 * @param sex --> The sex of this player.
	 */
	public L2CharacterBriefEntry(int objectId, int level, String name, String account, int online, int accesslevel, int sex, int clanId)
	{
		this.objectId = objectId;
		this.level = level;
		this.name = name;
		this.account = account;
		if (online == 1)
			isOnline = true;
		this.accesslevel = accesslevel;
		this.sex = sex;
		this.clanId = clanId;
	}

	// ===================================
	// METHOD - PUBLIC
	/** Returns the access level of this playerEntry */
	public int getAccessLevel()
	{
		return accesslevel;
	}

	/** Returns the account of this playerEntry */
	public String getAccount()
	{
		return account;
	}

	public int getClanId()
	{
		return clanId;
	}

	/** Returns the level of this playerEntry */
	public int getLevel()
	{
		return level;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return parent;
	}

	/** Returns the sex of this playerEntry */
	public int getSex()
	{
		return sex;
	}

	/**
	 * Returns true if the player was online when this entry was created
	 */
	public boolean isOnline()
	{
		return isOnline;
	}

	/** Defines a new parent for this playerEntry */
	public void setParent(L2GroupEntry parent)
	{
		this.parent = parent;
	}
}

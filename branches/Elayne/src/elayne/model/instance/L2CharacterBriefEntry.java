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
	private int _accesslevel;
	private String _account;
	private int _clanId;
	private boolean _isOnline = false;
	private int _level;
	private String _name;
	private int _objectId;
	private L2GroupEntry _parent;
	private int _sex;
	private boolean _isLeader;

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
	public L2CharacterBriefEntry(int objectId, int level, String name, String account, int online, int accesslevel, int sex, int clanId, boolean isLeader)
	{
		_objectId = objectId;
		_level = level;
		_name = name;
		_account = account;
		if (online == 1)
			_isOnline = true;
		_accesslevel = accesslevel;
		_sex = sex;
		_clanId = clanId;
		_isLeader = isLeader;
	}

	// ===================================
	// METHOD - PUBLIC
	/** Returns the access level of this playerEntry */
	public int getAccessLevel()
	{
		return _accesslevel;
	}

	/** Returns the account of this playerEntry */
	public String getAccount()
	{
		return _account;
	}

	public int getClanId()
	{
		return _clanId;
	}

	/** Returns the level of this playerEntry */
	public int getLevel()
	{
		return _level;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _parent;
	}

	/** Returns the sex of this playerEntry */
	public int getSex()
	{
		return _sex;
	}

	/**
	 * Returns true if the player was online when this entry was created
	 */
	public boolean isOnline()
	{
		return _isOnline;
	}

	public boolean isLeader()
	{
		return _isLeader;
	}

	/** Defines a new parent for this playerEntry */
	public void setParent(L2GroupEntry parent)
	{
		_parent = parent;
	}
}

package elayne.model.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.util.connector.ServerDB;

/**
 * This Class represents a Clan. It is capable of having children and can have a
 * player or a Group entry as a parent.
 * @author polbat02
 */
public class L2Clan extends L2GroupEntry
{
	// ==================================================
	// DATA FIELD
	/** The Clan's Ally Crest Id */
	private int _allyCrestId;
	/** The Clan's Ally Id */
	private int _allyId;
	/** The Clan's Ally Name */
	private String _allyName;
	/** The Clan's Ally Penalty EXPIRY Time */
	private int _allyPenaltyExpiryTime;
	/** The Clan's Ally Penalty Type */
	private String _allyPenaltyType;
	/** The Clan's auction bid */
	private int _auctionBidAt;
	/** The castle of the clan if any */
	private L2CastleGroup _castle = null;
	/** The Clan's Char penalty EXPIRY time */
	private long _charPenaltyExpiryTime;
	/** The Clan ObjectId */
	private int _clanId;
	/** The Clan Level */
	private int _clanLevel;
	/** The Clan Reputation Score */
	private int _clanReputationScore;
	/** List of members that this clan has */
	private FastList<L2CharacterBriefEntry> _clanMembers = new FastList<L2CharacterBriefEntry>();
	/** The group containing the clan members of this clan */
	private L2RegularGroup _clanMembersGroup;
	/** The Clan's Crest Id */
	private int _crestId;
	/** The Clan's Large Crest Id */
	private int _crestLargeId;
	/** The Clan's Dissolving EXPIRY time */
	private int _dissolvingExpiryTime;
	/**
	 * Id of the castle that this clan owns. If = 0, the clan has no Castle.
	 */
	private int _hasCastle;
	/** The Clan's Leader Id */
	private int _leaderId;
	/** The group containing this clan's skills */
	private L2ClanSkillsGroup _skillsGroup;

	/**
	 * Constructor that defines a new clan.
	 * @param clan_id
	 * @param name
	 * @param parent
	 */
	public L2Clan(int clanId, String name, L2GroupEntry parent)
	{
		super(parent, name);
		_clanId = clanId;
		fillEntries();
	}

	/**
	 * Restores a clan and fills this class with all the required children.
	 */
	private void fillEntries()
	{
		restore();
		getClanMembers();

		// Add the Clan Name
		addEntry(new L2ClanEntry(this, "Clan Name", getName()));

		// Add the Clan Level
		addEntry(new L2ClanEntry(this, "Clan Level", String.valueOf(getLevel())));

		// Add The Clan Reputation Score.
		addEntry(new L2ClanEntry(this, "Reputation Score", String.valueOf(getClanReputationScore())));

		// Add The Number Of Members.
		addEntry(new L2ClanEntry(this, "Total Members", String.valueOf(getClanMembers().size())));

		// If this clan has a castle, restore it and show
		// it.
		if (getHasCastle())
		{
			restoreCastle();
		}

		restoreSkills();

		// Add a group of players that will contain the
		// Clan Members...
		_clanMembersGroup = new L2RegularGroup(this, "Clan Members");
		addEntry(_clanMembersGroup);
		for (L2CharacterBriefEntry member : getClanMembers())
		{
			member.setParent(_clanMembersGroup);
			_clanMembersGroup.addEntry(member);
		}
	}

	/** The Clan's Ally Crest Id */
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}

	/** The Clan's Ally Id */
	public int getAllyId()
	{
		return _allyId;
	}

	/** The Clan's Ally Name */
	public String getAllyName()
	{
		return _allyName;
	}

	/** The Clan's Ally Penalty EXPIRY Time */
	public int getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}

	/** The Clan's Ally Penalty Type */
	public String getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}

	/** The Clan's auction bid */
	public int getAuctionBidAt()
	{
		return _auctionBidAt;
	}

	/** Returns the castle of this can. May be null. */
	public L2CastleGroup getCastle()
	{
		return _castle;
	}

	/** The Clan's Char penalty EXPIRY time */
	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}

	/**
	 * Restores all the members from a clan.
	 */
	public FastList<L2CharacterBriefEntry> getClanMembers()
	{
		if (!_clanMembers.isEmpty())
			return _clanMembers;
		FastList<L2CharacterBriefEntry> members = new FastList<L2CharacterBriefEntry>();

		try
		{
			String sql = "SELECT charId, account_name, char_name, online, level, accesslevel, sex, clanid FROM `characters` WHERE `clanid` =?";
			java.sql.Connection con = null;
			try
			{
				con = ServerDB.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(sql);
				statement.setInt(1, getId());
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					int objId = rset.getInt("charId");
					int level = rset.getInt("level");
					String name = rset.getString("char_name");
					String account = rset.getString("account_name");
					int online = rset.getInt("online");
					int accessLevel = rset.getInt("accesslevel");
					int sex = rset.getInt("sex");
					int clanId = rset.getInt("clanid");
					L2CharacterBriefEntry member = new L2CharacterBriefEntry(objId, level, name, account, online, accessLevel, sex, clanId);
					members.add(member);
				}
				rset.close();
				statement.close();
				System.out.println("L2Clan: " + members.size() + " members found in the clan " + getId() + ".");
			}
			catch (Exception e)
			{
				System.out.println("L2Clan: Exception while getting clan members:" + e.toString());
			}
			if (con != null)
				con.close();
			// SAVE THE MEMBERS TO NOT HAVE TO CONNECT TO
			// DATABASE AGAIN.
			_clanMembers = members;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return members;
	}

	public L2RegularGroup getClanMembersGroup()
	{
		return _clanMembersGroup;
	}

	/** The Clan Reputation Score */
	public int getClanReputationScore()
	{
		return _clanReputationScore;
	}

	/** The Clan's Crest Id */
	public int getCrestId()
	{
		return _crestId;
	}

	/** The Clan's Large Crest Id */
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}

	/** The Clan's Dissolving EXPIRY time */
	public int getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}

	/**
	 * If Has Castle = 0, the Clan doesn't have a Castle. If Has Castle = 1, the
	 * Clan does have a Castle.
	 */
	public boolean getHasCastle()
	{
		return (_hasCastle != 0);
	}

	/**
	 * @return the id of the clan that first started the clan information
	 * methods.
	 */
	public int getId()
	{
		return _clanId;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLAN_GROUP);
	}

	/** The Clan's Leader Id */
	public int getLeaderId()
	{
		return _leaderId;
	}

	/** The Clan Level */
	public int getLevel()
	{
		return _clanLevel;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _parent;
	}

	/**
	 * Restores all the information from a clan.
	 * @throws SQLException
	 */
	private void restore()
	{
		String sql = "SELECT clan_id, clan_name, clan_level, reputation_score, hasCastle, ally_id, ally_name, leader_id, crest_id, crest_large_id, ally_crest_id, auction_bid_at, ally_penalty_expiry_time, ally_penalty_type, char_penalty_expiry_time, dissolving_expiry_time FROM `clan_data` WHERE clan_id=?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, _clanId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				setName(rset.getString("clan_name"));
				_clanLevel = rset.getInt("clan_level");
				_clanReputationScore = rset.getInt("reputation_score");
				_hasCastle = rset.getInt("hasCastle");
				_allyId = rset.getInt("ally_id");
				_allyName = rset.getString("ally_name");
				_leaderId = rset.getInt("leader_id");
				_crestId = rset.getInt("crest_id");
				_crestLargeId = rset.getInt("crest_large_id");
				_allyCrestId = rset.getInt("ally_crest_id");
				_auctionBidAt = rset.getInt("auction_bid_at");
				_allyPenaltyExpiryTime = rset.getInt("ally_penalty_expiry_time");
				_allyPenaltyType = rset.getString("ally_penalty_type");
				_charPenaltyExpiryTime = rset.getLong("char_penalty_expiry_time");
				_dissolvingExpiryTime = rset.getInt("dissolving_expiry_time");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (con != null)
				try
				{
					con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
	}

	/**
	 * Restores a Castle for this clan and adds the proper children to this
	 * class.
	 */
	public void restoreCastle()
	{
		String sql = "SELECT id, name, taxPercent, treasury, siegeDate FROM `castle` WHERE `id` =?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, _hasCastle);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String castleName = rset.getString("name");
				int taxPercent = rset.getInt("taxPercent");
				int treasury = rset.getInt("treasury");
				long siegeDate = rset.getLong("siegeDate");
				_castle = new L2CastleGroup(this, castleName, taxPercent, treasury, siegeDate);
				addEntry(_castle);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (con != null)
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
	}

	/**
	 * Gets this clan's skills and adds the proper children to this class.
	 */
	public void restoreSkills()
	{
		_skillsGroup = new L2ClanSkillsGroup(this, "Clan Skills");
		addEntry(_skillsGroup);
		String sql = "SELECT skill_id, skill_level, skill_name FROM `clan_skills` WHERE `clan_id` =?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, _clanId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				String skillName = rset.getString("skill_name");
				_skillsGroup.addEntry(new L2ClanSkillEntry(skillId, skillLevel, skillName, _skillsGroup));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (con != null)
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		if (_skillsGroup.getEntries().length == 0)
		{
			removeEntry(_skillsGroup);
			System.out.println("The Clan " + getName() + " has no Clan Skills.");
		}
	}

	public void setParent(L2GroupEntry parent)
	{
		_parent = parent;
	}
}

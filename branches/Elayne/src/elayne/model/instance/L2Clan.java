package elayne.model.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.CastleTable;
import elayne.datatables.ClanhallTable;
import elayne.datatables.FortressTable;
import elayne.templates.L2Castle;
import elayne.templates.L2Clanhall;
import elayne.templates.L2Fortress;
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
	/** The Clan's Leader Id */
	private int _leaderId;
	/** The group containing this clan's skills */
	private L2ClanSkillsGroup _skillsGroup;

	private L2ClanhallGroup _clanhall = null;
	private int _clanhallId = 0;

	private L2FortressGroup _fortress = null;
	private int _fortressId = 0;

	private boolean _hasCastle = false;
	private L2CastleGroup _castle = null;
	private int _castleId = 0;

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

		addEntry(new L2ClanEntry(this, "Clan Name", getName()));
		addEntry(new L2ClanEntry(this, "Clan Level", String.valueOf(getLevel())));
		addEntry(new L2ClanEntry(this, "Clan Leader", getClanLeader()));
		addEntry(new L2ClanEntry(this, "Reputation Score", String.valueOf(getClanReputationScore())));
		addEntry(new L2ClanEntry(this, "Total Members", String.valueOf(getClanMembers().size())));

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

		if (getHasCastle())
		{
			System.out.println("L2Clan: Clan "+_clanId+" has a castle.");
			L2Castle castle = CastleTable.getInstance().getCastle(_castleId);
			setCastle(new L2CastleGroup(this, castle.getName(), castle.getTax(), castle.getTreasury(), castle.getSiegeDate()));
			addEntry(_castle);
		}

		_clanhallId = ClanhallTable.getInstance().getClanCH(_clanId);
		if (_clanhallId > 0)
		{
			System.out.println("L2Clan: Clan "+_clanId+" has a clanhall.");
			L2Clanhall clanhall = ClanhallTable.getInstance().getClanhall(_clanhallId);
			setClanhall(new L2ClanhallGroup(this, clanhall.getClanhallId(), clanhall.getName(), clanhall.getLease(), clanhall.getDesc(), clanhall.getLocation(), clanhall.getGrade()));
			addEntry(_clanhall);
		}

		_fortressId = FortressTable.getInstance().getClanFort(_clanId);
		if (_fortressId > 0)
		{
			System.out.println("L2Clan: Clan "+_clanId+" has a fortress.");
			L2Fortress fort = FortressTable.getInstance().getFortress(_fortressId);
			setFortress(new L2FortressGroup(this, fort.getFortId(), fort.getName(), fort.getTime(), fort.getType(), fort.getState(), fort.getOwningCastleName()));
			addEntry(_fortress);
		}
	}

	private String getClanLeader()
	{
		for (L2CharacterBriefEntry character : _clanMembers)
		{
			if (character.getObjectId() == _leaderId)
				return character.getName();
		}
		return "Unknown";
	}

	private void setCastle(L2CastleGroup castle)
	{
		_castle = castle;
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

		try
		{
			String sql = "SELECT charId, account_name, char_name, online, level, accesslevel, sex, clanid FROM `characters` WHERE `clanid` =? ORDER BY char_name";
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
					_clanMembers.add(new L2CharacterBriefEntry(objId, level, name, account, online, accessLevel, sex, clanId));
				}
				rset.close();
				statement.close();
				System.out.println("L2Clan: " + _clanMembers.size() + " members found in the clan " + getId() + ".");
			}
			catch (Exception e)
			{
				System.out.println("L2Clan: Exception while getting clan members:" + e.toString());
			}
			if (con != null)
				con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return _clanMembers;
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
		return _hasCastle;
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
				_castleId = rset.getInt("hasCastle");
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

				if (_castleId > 0)
				{
					_hasCastle = true;
					L2Castle castle = CastleTable.getInstance().getCastle(_castleId);
					if (castle != null)
						castle.setOwner(_clanId);
				}
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

	public void setClanhall(L2ClanhallGroup clanhall)
	{
		_clanhall = clanhall;
	}

	public L2ClanhallGroup getClanhall()
	{
		return _clanhall;
	}

	public void setFortress(L2FortressGroup fortress)
	{
		_fortress = fortress;
	}

	public L2FortressGroup getFortress()
	{
		return _fortress;
	}
}

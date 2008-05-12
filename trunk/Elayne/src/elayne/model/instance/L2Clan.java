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
	private int ally_crest_id;
	/** The Clan's Ally Id */
	private int ally_id;
	/** The Clan's Ally Name */
	private String ally_name;
	/** The Clan's Ally Penalty EXPIRY Time */
	private int ally_penalty_expiry_time;
	/** The Clan's Ally Penalty Type */
	private String ally_penalty_type;
	/** The Clan's auction bid */
	private int auction_bid_at;
	/** The castle of the clan if any */
	private L2CastleGroup castle = null;
	/** The Clan's Char penalty EXPIRY time */
	private int char_penalty_expiry_time;
	/** The Clan ObjectId */
	private int clan_id;
	/** The Clan Level */
	private int clan_level;
	/** The Clan Reputation Score */
	private int clan_reputation_score;
	/** List of members that this clan has */
	private FastList<L2CharacterBriefEntry> clanMembers = new FastList<L2CharacterBriefEntry>();
	/** The group containing the clan members of this clan */
	private L2RegularGroup clanMembersGroup;
	/** The Clan's Crest Id */
	private int crest_id;
	/** The Clan's Large Crest Id */
	private int crest_large_id;
	/** The Clan's Dissolving EXPIRY time */
	private int dissolving_expiry_time;
	/**
	 * Id of the castle that this clan owns. If = 0, the clan has no Castle.
	 */
	private int has_castle;
	/** The Clan's Leader Id */
	private int leader_id;
	/** The group containing this clan's skills */
	private L2ClanSkillsGroup skillsGroup;

	/**
	 * Constructor that defines a new clan.
	 * @param clan_id
	 * @param name
	 * @param parent
	 */
	public L2Clan(int clan_id, String name, L2GroupEntry parent)
	{
		super(parent, name);
		this.clan_id = clan_id;
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
		clanMembersGroup = new L2RegularGroup(this, "Clan Members");
		addEntry(clanMembersGroup);
		for (L2CharacterBriefEntry member : getClanMembers())
		{
			member.setParent(clanMembersGroup);
			clanMembersGroup.addEntry(member);
		}
	}

	/** The Clan's Ally Crest Id */
	public int getAllyCrestId()
	{
		return ally_crest_id;
	}

	/** The Clan's Ally Id */
	public int getAllyId()
	{
		return ally_id;
	}

	/** The Clan's Ally Name */
	public String getAllyName()
	{
		return ally_name;
	}

	/** The Clan's Ally Penalty EXPIRY Time */
	public int getAllyPenaltyExpiryTime()
	{
		return ally_penalty_expiry_time;
	}

	/** The Clan's Ally Penalty Type */
	public String getAllyPenaltyType()
	{
		return ally_penalty_type;
	}

	/** The Clan's auction bid */
	public int getAuctionBidAt()
	{
		return auction_bid_at;
	}

	/** Returns the castle of this can. May be null. */
	public L2CastleGroup getCastle()
	{
		return castle;
	}

	/** The Clan's Char penalty EXPIRY time */
	public int getCharPenaltyExpiryTime()
	{
		return char_penalty_expiry_time;
	}

	/**
	 * Restores all the members from a clan.
	 */
	public FastList<L2CharacterBriefEntry> getClanMembers()
	{
		if (!clanMembers.isEmpty())
			return clanMembers;
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
			clanMembers = members;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return members;
	}

	public L2RegularGroup getClanMembersGroup()
	{
		return clanMembersGroup;
	}

	/** The Clan Reputation Score */
	public int getClanReputationScore()
	{
		return clan_reputation_score;
	}

	/** The Clan's Crest Id */
	public int getCrestId()
	{
		return crest_id;
	}

	/** The Clan's Large Crest Id */
	public int getCrestLargeId()
	{
		return crest_large_id;
	}

	/** The Clan's Dissolving EXPIRY time */
	public int getDissolvingExpiryTime()
	{
		return dissolving_expiry_time;
	}

	/**
	 * If Has Castle = 0, the Clan doesn't have a Castle. If Has Castle = 1, the
	 * Clan does have a Castle.
	 */
	public boolean getHasCastle()
	{
		return (has_castle != 0);
	}

	/**
	 * @return the id of the clan that first started the clan information
	 * methods.
	 */
	public int getId()
	{
		return clan_id;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLAN_GROUP);
	}

	/** The Clan's Leader Id */
	public int getLeaderId()
	{
		return leader_id;
	}

	/** The Clan Level */
	public int getLevel()
	{
		return clan_level;
	}

	@Override
	public L2GroupEntry getParent()
	{
		return parent;
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
			statement.setInt(1, clan_id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				// Get the Clan Name
				setName(rset.getString("clan_name"));
				// Get the Clan Level
				clan_level = rset.getInt("clan_level");
				// Get the Reputation Score.
				clan_reputation_score = rset.getInt("reputation_score");
				// Does the clan have a Castle?
				has_castle = rset.getInt("hasCastle");
				// Get the Ally Id
				ally_id = rset.getInt("ally_id");
				// Get the Ally Name
				ally_name = rset.getString("ally_name");
				// Get the Leader Id
				leader_id = rset.getInt("leader_id");
				// Get the Crest id
				crest_id = rset.getInt("crest_id");
				// Get the Crest Large Id
				crest_large_id = rset.getInt("crest_large_id");
				// Get the ally crest id
				ally_crest_id = rset.getInt("ally_crestId");
				// Get the auction bid
				auction_bid_at = rset.getInt("auction_bid_at");
				// Get the Ally EXPIRY time
				ally_penalty_expiry_time = rset.getInt("ally_penalty_expiry_time");
				// Get the Ally penalty type
				ally_penalty_type = rset.getString("ally_penalty_type");
				// Get the Penalty EXPIRY Time from a character (kicking and such...)
				char_penalty_expiry_time = rset.getInt("char_penalty_expiry_time");
				// Get the Dissolving EXPIRY time
				dissolving_expiry_time = rset.getInt("dissolving_expiry_time");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
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
		String sql = "SELECT id, name, taxPercent, treasury,  siegeDate, siegeDayOfWeek, siegeHourOfDay FROM `castle` WHERE `id` =?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, has_castle);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String castle_name = rset.getString("name");
				int taxPercent = rset.getInt("taxPercent");
				int treasury = rset.getInt("treasury");
				long siegeDate = rset.getLong("siegeDate");
				int siegeDayOfWeek = rset.getInt("siegeDayOfWeek");
				int siegeHourOfDay = rset.getInt("siegeHourOfDay");
				castle = new L2CastleGroup(this, castle_name, taxPercent, treasury, siegeDate, siegeDayOfWeek, siegeHourOfDay);
				this.addEntry(castle);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
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
		skillsGroup = new L2ClanSkillsGroup(this, "Clan Skills");
		this.addEntry(skillsGroup);
		String sql = "SELECT skill_id, skill_level, skill_name FROM `clan_skills` WHERE `clan_id` =?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, clan_id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				String skillName = rset.getString("skill_name");
				skillsGroup.addEntry(new L2ClanSkillEntry(skillId, skillLevel, skillName, skillsGroup));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
		if (con != null)
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		if (skillsGroup.getEntries().length == 0)
		{
			removeEntry(skillsGroup);
			System.out.println("The Clan " + getName() + " has no Clan Skills.");
		}
	}

	public void setParent(L2GroupEntry parent)
	{
		this.parent = parent;
	}
}

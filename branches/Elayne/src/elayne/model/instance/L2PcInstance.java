package elayne.model.instance;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.SkillsTable;
import elayne.instancemanager.ClansManager;
import elayne.model.L2CharacterPresence;
import elayne.util.connector.ServerDB;

public class L2PcInstance extends L2GroupEntry
{
	private static final String RESTORE_CHARACTER = "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, fame, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, banchat_timer, newbie, nobless, pledge_rank, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time, clan_create_expiry_time, charViP, death_penalty_level, trust_level, vitality_points, bookmarkslot FROM characters WHERE char_name=?";
	private static final String RESTORE_CHARACTER_HENNAS = "SELECT symbol_id, slot, class_index FROM `character_hennas` WHERE `charId` =?";

	// =========================================================
	// METHOD - STATIC
	@SuppressWarnings("unused")
	private static String getClanName(int clanId) throws SQLException
	{
		String str = "";
		String sql = "SELECT clan_name FROM `clan_data` WHERE clan_id=" + clanId;
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				str = rset.getString("clan_name");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
		if (con != null)
			con.close();
		return str;
	}

	/**
	 * Return if a player exists or not in the characters table.
	 */
	public static boolean isRealPlayer(String player)
	{
		java.sql.Connection con = null;
		int foundObjects = 0;
		try
		{
			con = ServerDB.getInstance().getConnection();
			final String previous = "SELECT COUNT(*) FROM characters WHERE `char_name` =?";
			PreparedStatement statement = con.prepareStatement(previous);
			statement.setString(1, player);
			ResultSet prset = statement.executeQuery();
			try
			{
				if (prset.next())
				{
					foundObjects = prset.getInt(1);
					System.out.println("L2PcInstance: " + foundObjects + " Players found for the name pattern: " + player + ".");
					prset.close();
				}
			}
			catch (Exception excep)
			{
				excep.printStackTrace();
			}
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (foundObjects == 0)
		{
			String er = "Your search produced no results. Try again.";
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openInformation(shell, "Player Info", er);
			return false;
		}
		return true;
	}

	// =========================================================
	// Data Field
	private int accesLevel = 0;
	private L2CharacterEntry accessLevelLabel;
	private String account = "";
	private L2AccountInfoGroup accountInformation = null;
	private L2CharacterEntry accountLabel;
	private int activeClassId = 0;
	private L2CharacterEntry actualClassLabel;
	@SuppressWarnings("unused")
	private int allianceWithVarkaKetra = 0;
	@SuppressWarnings("unused")
	private int apprentice = 0;
	private int baseClass = 0;
	private L2CharacterEntry baseClassLabel;
	/** The clan of the player */
	private L2Clan clan = null;
	private long clanCreateExpTime = 0;
	private int clanId = 0;
	private long clanJoinExpTime = 0;
	private double maxCp = 0;
	private double currentCp = 0;
	private double maxHp = 0;
	private double currentHp = 0;
	private double maxMp = 0;
	private double currentMp = 0;
	private L2CharacterEntry cpLabel;
	private L2CharacterEntry hpLabel;
	private L2CharacterEntry mpLabel;
	private int deathPenaltyBuffLevel = 0;
	private L2CharacterEntry deathPenaltyLabel;
	@SuppressWarnings("unused")
	private long deleteTime = 0;
	@SuppressWarnings("unused")
	private long expBeforeDeath = 0;
	private long experience = 0;
	private L2CharacterEntry expLabel;
	@SuppressWarnings("unused")
	private byte face = 0;
	private int gender = 0;
	@SuppressWarnings("unused")
	private byte hairColor = 0;
	@SuppressWarnings("unused")
	private byte hairStyle = 0;
	private boolean hasNewClan;
	@SuppressWarnings("unused")
	private int heading = 0;
	private FastMap<Integer, L2HennaGroup> hennaMap = new FastMap<Integer, L2HennaGroup>();
	private boolean inJail = false;
	/** The Inventory of the player */
	private L2Inventory inventory;
	@SuppressWarnings("unused")
	private boolean isIn7sDungeon = false;
	public long jailTimer = 0;
	private int karma = 0;
	private L2CharacterEntry karmaLabel;
	@SuppressWarnings("unused")
	private long lastAccess = 0;
	@SuppressWarnings("unused")
	private long lastRecomDate = 0;
	private byte level = 0;
	private L2CharacterEntry levelLabel;
	@SuppressWarnings("unused")
	private int lvlJoinedAcademy = 0;
	private int noble = 0;
	private int objectId = 0;
	private int online;
	private long onlineTime = 0;
	private L2CharacterEntry onlineTimeLabel;
	private L2CharacterEntry pkLabel;
	private int pks = 0;
	private L2CharacterPresence presence;
	private L2CharacterEntry pvpLabel;
	private int pvps = 0;
	private int race = 0;
	@SuppressWarnings("unused")
	private int recHave = 0;
	@SuppressWarnings("unused")
	private int recLeft = 0;
	private L2SkillsGroup skills = null;
	private FastMap<Integer, FastList<L2SkillEntry>> skillsMap = new FastMap<Integer, FastList<L2SkillEntry>>();
	private int sp = 0;
	private L2CharacterEntry spLabel;
	@SuppressWarnings("unused")
	private int sponsor = 0;
	private L2SubClassGroup subclasses = null;
	@SuppressWarnings("unused")
	private int subPledge = 0;
	private FastList<L2SubClass> subs = new FastList<L2SubClass>();
	@SuppressWarnings("unused")
	private String title = "";
	@SuppressWarnings("unused")
	private int wantsPeace = 0;
	@SuppressWarnings("unused")
	private int x = 0;
	@SuppressWarnings("unused")
	private int y = 0;
	@SuppressWarnings("unused")
	private int z = 0;

	/**
	 * Constructor - Defines a new player.
	 * @param parent
	 * @param name
	 * @param monitor
	 */
	public L2PcInstance(L2GroupEntry parent, String name)
	{
		super(parent, name);
		ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
		progress.setCancelable(true);
		try
		{
			progress.run(true, true, new IRunnableWithProgress()
			{
				public void run(IProgressMonitor monitor) throws InvocationTargetException
				{
					try
					{
						monitor.beginTask("Looking for player...", IProgressMonitor.UNKNOWN);
						monitor.subTask("Checking if player exists...");
						monitor.subTask("Loading player...");
						// GET PLAYER INFORMATION
						restore();
						monitor.subTask("Getting extra information...");
						// RESTORE HENNAS
						restoreHennas();
						fillStats(null, true);
						monitor.done();
					}
					catch (Exception e)
					{
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{}
		if (hasNewClan)
		{
			ClansManager.getInstance().addClan(clan);
			hasNewClan = false;
		}
	}

	/**
	 * Adds and saves some player basic labels into this L2PcInstance Group.
	 * @param stats
	 * @param player
	 */
	private void addStatsEntries(L2GroupEntry stats, L2PcInstance player)
	{
		try
		{
			// ACCOUNT:
			accountLabel = new L2CharacterEntry(stats, "Account:", player.getAccount());
			stats.addEntry(accountLabel);

			// LEVEL:
			levelLabel = new L2CharacterEntry(stats, "Level: ", player.getLevel());
			stats.addEntry(levelLabel);

			// EXP:
			expLabel = new L2CharacterEntry(stats, "Exp: ", player.getExperience());
			stats.addEntry(expLabel);

			// SP:
			spLabel = new L2CharacterEntry(stats, "SP: ", player.getSp());
			stats.addEntry(spLabel);

			// CP:
			cpLabel = new L2CharacterEntry(stats, "CP: ", (int)player.getCurrentCp() + "/" + (int)player.getMaxCp());
			stats.addEntry(cpLabel);

			// HP:
			hpLabel = new L2CharacterEntry(stats, "HP: ", (int)player.getCurrentHp() + "/" + (int)player.getMaxHp());
			stats.addEntry(hpLabel);

			// MP:
			mpLabel = new L2CharacterEntry(stats, "MP: ", (int)player.getCurrentMp() + "/" + (int)player.getMaxMp());
			stats.addEntry(mpLabel);

			// KARMA:
			karmaLabel = new L2CharacterEntry(stats, "Karma: ", player.getKarma());
			stats.addEntry(karmaLabel);

			// PVP KILLS:
			pvpLabel = new L2CharacterEntry(stats, "PvPs: ", player.getPvP());
			stats.addEntry(pvpLabel);

			// PK KILLS:
			pkLabel = new L2CharacterEntry(stats, "PKs: ", player.getPks());
			stats.addEntry(pkLabel);

			// ONLINE TIME:
			onlineTimeLabel = new L2CharacterEntry(stats, "Online Time in Hours: ", player.getOnlineTime());
			stats.addEntry(onlineTimeLabel);

			// CLASS:
			actualClassLabel = new L2CharacterEntry(stats, "Active Class:", player.getActiveclass());
			stats.addEntry(actualClassLabel);

			// BASE CLASS:
			baseClassLabel = new L2CharacterEntry(stats, "Base Class:", player.getBaseClass());
			stats.addEntry(baseClassLabel);

			// ACCESSLEVEL:
			accessLevelLabel = new L2CharacterEntry(stats, "Accesslevel:", player.getAccessLevel());
			stats.addEntry(accessLevelLabel);

			// DEATH PENALTY:
			deathPenaltyLabel = new L2CharacterEntry(stats, "Death Penalty:", player.getDeathPenalty());
			stats.addEntry(deathPenaltyLabel);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ===========================================================
	// PUBLIC METHODS
	public void fillStats(L2GroupEntry playerGroup, boolean isUpdate)
	{
		if (!isUpdate && playerGroup != null)
			playerGroup.addEntry(this);

		clearEntries();

		// ADD STATS GROUP
		L2GroupEntry stats = new L2RegularGroup(this, "Stats");
		addEntry(stats);
		// Add STATS Entries
		addStatsEntries(stats, this);

		// ADD ACCOUNT INFO GROUP
		accountInformation = new L2AccountInfoGroup(this);
		accountInformation.restore();

		// ADD INVENTORY GROUP.
		inventory = new L2Inventory(this, "Inventory");
		inventory.restore();

		// ADD SUBS GROUP
		getSubClassesId(isUpdate);
		if (getSubs().size() > 0)
		{
			subclasses = new L2SubClassGroup(this, "Sub Classes");
			subclasses.restore();
		}

		// ADD SKILLS GROUP
		skills = new L2SkillsGroup(this, "Skill", null);
		skills.restore();

		// ADD HENNA GROUP
		if (hennaMap.get(0).getEntries().length > 0)
			addEntry(hennaMap.get(0));

		// ADD CLAN GROUP
		if (getClanId() != 0)
		{
			clan = null;
			if (ClansManager.getInstance().isKnownClan(getClanId()))
				clan = ClansManager.getInstance().getClan(getClanId());
			else
			{
				clan = new L2Clan(getClanId(), "Clan", null);
				hasNewClan = true;
			}
			addEntry(clan);
		}

		setPresence(getRace(), getGenderValue());
	}

	public int getAccessLevel()
	{
		return accesLevel;
	}

	public String getAccount()
	{
		return account;
	}

	public String getActiveclass() throws SQLException
	{
		String str = "";
		String sql = "SELECT ClassName FROM `char_templates` WHERE ClassId=?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, activeClassId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				str = rset.getString("ClassName");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
		if (con != null)
			con.close();
		return str;
	}

	public String getBaseClass() throws SQLException
	{
		String str = "";
		String sql = "SELECT ClassName FROM `char_templates` WHERE ClassId=?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, baseClass);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				str = rset.getString("ClassName");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{}
		if (con != null)
			con.close();
		return str;
	}

	public L2Clan getClan()
	{
		return clan;
	}

	public long getclanCreateExpTime()
	{
		return clanCreateExpTime;
	}

	public int getClanId()
	{
		return clanId;
	}

	public long getClanJoinExpTime()
	{
		return clanJoinExpTime;
	}

	/**
	 * Sets the players death penalty inScreen.
	 */
	public String getDeathPenalty()
	{
		if (getDeathPenaltybuffLevel() != 0)
			return "Yes";
		return "No";
	}

	public int getDeathPenaltybuffLevel()
	{
		return deathPenaltyBuffLevel;
	}

	public long getExperience()
	{
		return experience;
	}

	/**
	 * @return gender of the Player
	 */
	public String getGender()
	{
		String str = "";
		if (gender == 0)
			str = "Male";
		else if (gender == 1)
			str = "Female";
		else
			str = "Error! Warn the developers!";
		return str;
	}

	public int getGenderValue()
	{
		return gender;
	}

	public L2HennaGroup getHennaGroup(int classIndex)
	{
		return hennaMap.get(classIndex);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		String key = presenceToKey(getPresence());
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key);
	}

	public L2Inventory getInventory()
	{
		return inventory;
	}

	public int getKarma()
	{
		return karma;
	}

	public int getLevel()
	{
		return level;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public int getOnline()
	{
		return online;
	}

	public long getOnlineTime()
	{
		long onlinetimeH = Math.round(((double) onlineTime / 60 / 60) - 0.5);
		return onlinetimeH;
	}

	public int getPks()
	{
		return pks;
	}

	public FastList<L2SkillEntry> getPlayerSkillsByClass(int classIndex)
	{
		return skillsMap.get(classIndex);
	}

	public L2CharacterPresence getPresence()
	{
		return presence;
	}

	public int getPvP()
	{
		return pvps;
	}

	public int getRace()
	{
		return race;
	}

	public int getSp()
	{
		return sp;
	}

	public double getCurrentCp()
	{
		return currentCp;
	}

	public double getCurrentHp()
	{
		return currentHp;
	}

	public double getCurrentMp()
	{
		return currentMp;
	}

	public double getMaxCp()
	{
		return maxCp;
	}

	public double getMaxHp()
	{
		return maxHp;
	}

	public double getMaxMp()
	{
		return maxMp;
	}

	private void getSubClassesId(boolean isUpdate)
	{
		if (isUpdate)
			subs.clear();
		if (!subs.isEmpty())
			return;
		String sql = "SELECT class_id, level, class_index FROM `character_subclasses` WHERE charId=?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			if (con != null)
			{
				PreparedStatement statement = con.prepareStatement(sql);
				statement.setInt(1, objectId);
				ResultSet rset = statement.executeQuery();
				int i = 0;
				while (rset.next())
				{
					subs.add(new L2SubClass(rset.getInt("class_id"), rset.getInt("level"), rset.getInt("class_index")));
					i++;
				}
				rset.close();
				statement.close();
			}
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
		return;
	}

	public FastList<L2SubClass> getSubs()
	{
		return subs;
	}

	public boolean isInJail()
	{
		return inJail;
	}

	public int isNoble()
	{
		return noble;
	}

	public boolean isOnline()
	{
		return (online == 1);
	}

	private String presenceToKey(L2CharacterPresence presence)
	{
		if (presence == L2CharacterPresence.DWARF_MALE)
			return IImageKeys.DWARF_MALE;
		else if (presence == L2CharacterPresence.DWARF_FEMALE)
			return IImageKeys.DWARF_FEMALE;
		else if (presence == L2CharacterPresence.DARK_ELF_MALE)
			return IImageKeys.DARK_ELF_MALE;
		else if (presence == L2CharacterPresence.DARK_ELF_FEMALE)
			return IImageKeys.DARK_ELF_FEMALE;
		else if (presence == L2CharacterPresence.HUMAN_MALE)
			return IImageKeys.HUMAN_MALE;
		else if (presence == L2CharacterPresence.HUMAN_FEMALE)
			return IImageKeys.HUMAN_FEMALE;
		else if (presence == L2CharacterPresence.ELF_MALE)
			return IImageKeys.ELF_MALE;
		else if (presence == L2CharacterPresence.ELF_FEMALE)
			return IImageKeys.ELF_FEMALE;
		else if (presence == L2CharacterPresence.ORC_MALE)
			return IImageKeys.ORC_MALE;
		else if (presence == L2CharacterPresence.ORC_FEMALE)
			return IImageKeys.ORC_FEMALE;
		else if (presence == L2CharacterPresence.KAMAEL_MALE)
			return IImageKeys.DWARF_MALE;
		else if (presence == L2CharacterPresence.KAMAEL_FEMALE)
			return IImageKeys.DWARF_FEMALE;
		return IImageKeys.DWARF_MALE; // TODO Fin Kamael images
	}

	/**
	 * Retrieve a Player from the characters table of the database and add Store
	 * all the information of this player.
	 * @param monitor
	 */
	public void restore()
	{
		java.sql.Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				objectId = rset.getInt("charId");
				activeClassId = rset.getInt("classid");
				gender = rset.getInt("sex");
				face = rset.getByte("face");
				hairColor = rset.getByte("hairColor");
				hairStyle = rset.getByte("hairStyle");
				account = rset.getString("account_name");
				name = rset.getString("char_name");
				lastAccess = rset.getLong("lastAccess");
				experience = rset.getLong("exp");
				expBeforeDeath = (rset.getLong("expBeforeDeath"));
				level = (rset.getByte("level"));
				sp = rset.getInt("sp");
				wantsPeace = (rset.getInt("wantspeace"));
				heading = (rset.getInt("heading"));
				karma = (rset.getInt("karma"));
				pvps = (rset.getInt("pvpkills"));
				pks = (rset.getInt("pkkills"));
				onlineTime = (rset.getLong("onlinetime"));
				noble = (rset.getInt("nobless"));
				clanJoinExpTime = (rset.getLong("clan_join_expiry_time"));
				if (clanJoinExpTime < System.currentTimeMillis())
				{
					clanJoinExpTime = 0;
				}

				clanCreateExpTime = (rset.getLong("clan_create_expiry_time"));
				if (clanCreateExpTime < System.currentTimeMillis())
				{
					clanCreateExpTime = 0;
				}
				clanId = rset.getInt("clanid");
				subPledge = (rset.getInt("subpledge"));
				race = rset.getInt("race");
				online = rset.getInt("online");
				deleteTime = (rset.getLong("deletetime"));
				title = (rset.getString("title"));
				accesLevel = (rset.getInt("accesslevel"));
				maxHp = rset.getDouble("maxHp");
				maxCp = rset.getDouble("maxCp");
				maxMp = rset.getDouble("maxMp");
				currentHp = rset.getDouble("curHp");
				currentCp = rset.getDouble("curCp");
				currentMp = rset.getDouble("curMp");
				baseClass = (rset.getInt("base_class"));
				apprentice = (rset.getInt("apprentice"));
				sponsor = (rset.getInt("sponsor"));
				lvlJoinedAcademy = (rset.getInt("lvl_joined_academy"));
				isIn7sDungeon = ((rset.getInt("isin7sdungeon") == 1) ? true : false);
				inJail = ((rset.getInt("in_jail") == 1) ? true : false);
				jailTimer = 0;
				if (inJail)
					jailTimer = (rset.getLong("jail_timer"));

				allianceWithVarkaKetra = (rset.getInt("varka_ketra_ally"));

				deathPenaltyBuffLevel = (rset.getInt("death_penalty_level"));
				x = rset.getInt("x");
				y = rset.getInt("y");
				z = rset.getInt("z");
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Could not restore char data: " + e);
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
				System.out.println("L2PcInstance: Player " + name + " imported correctly.");
			}
			catch (Exception e)
			{
				System.out.println("L2PcInstance: Exception while closing connection. " + e.getMessage());
			}
		}
	}

	public void restoreHennas()
	{
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER_HENNAS);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			L2HennaGroup group0 = new L2HennaGroup(this, "Hennas");
			hennaMap.put(0, group0);
			L2HennaGroup group1 = new L2HennaGroup(this, "Hennas");
			hennaMap.put(1, group1);
			L2HennaGroup group2 = new L2HennaGroup(this, "Hennas");
			hennaMap.put(2, group2);
			L2HennaGroup group3 = new L2HennaGroup(this, "Hennas");
			hennaMap.put(3, group3);
			while (rset.next())
			{
				int symbolId = rset.getInt("symbol_id");
				int slot = rset.getInt("slot");
				int classIndex = rset.getInt("class_index");
				hennaMap.get(classIndex).addEntry(new L2HennaEntry(hennaMap.get(classIndex), symbolId, slot, classIndex));
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
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setAccessLevel(int accessLevel)
	{
		this.accesLevel = accessLevel;
		accessLevelLabel.setField(accessLevel);
	}

	/**
	 * Changes the actual account and refreshes the account label.
	 * @param account
	 */
	public void setAccount(String account)
	{
		this.account = account;
		accountLabel.setField(account);
	}

	public void setClan(L2Clan clan)
	{
		this.clan = clan;
	}

	public void setClanId(int i)
	{
		this.clanId = i;
	}

	public void setInJail(boolean inJail)
	{
		this.inJail = inJail;
	}

	/**
	 * Changes a player's KARMA level and refreshes the level of the KARMA
	 * entry.
	 * @param karma
	 */
	public void setKarma(int karma)
	{
		this.karma = karma;
		karmaLabel.setField(karma);
	}

	public void setNoble(int noble)
	{
		this.noble = noble;
	}

	public void setOnline(boolean isOnline)
	{
		if (!isOnline)
			this.online = 0;
		else
			this.online = 1;
	}

	public void setParent(L2GroupEntry parentGroup)
	{
		this.parent = parentGroup;
	}

	public void setPlayerSkills(int class_index, L2SkillsGroup folder)
	{
		if (getPlayerSkillsByClass(class_index) != null && !getPlayerSkillsByClass(class_index).isEmpty())
			return;

		final String SQL = "SELECT skill_id, skill_level FROM character_skills WHERE charId=? AND class_index=?";
		java.sql.Connection con = null;
		FastList<L2SkillEntry> skills = new FastList<L2SkillEntry>();
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, getObjectId());
			statement.setInt(2, class_index);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skill_level = rset.getInt("skill_level");

				String skill_name = SkillsTable.getInstance().getSkillName(skillId);

				skills.add(new L2SkillEntry(skillId, skill_level, skill_name, class_index, folder));
			}
			skillsMap.put(class_index, skills);
			rset.close();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void setPresence(int race, int gender)
	{
		if ((gender == 0) && (race == 4))
		{
			presence = L2CharacterPresence.DWARF_MALE;
		}
		else if ((gender == 1) && (race == 4))
		{
			presence = L2CharacterPresence.DWARF_FEMALE;
		}
		else if ((gender == 0) && (race == 2))
		{
			presence = L2CharacterPresence.DARK_ELF_MALE;
		}
		else if ((gender == 1) && (race == 2))
		{
			presence = L2CharacterPresence.DARK_ELF_FEMALE;
		}
		else if ((gender == 0) && (race == 0))
		{
			presence = L2CharacterPresence.HUMAN_MALE;
		}
		else if ((gender == 1) && (race == 0))
		{
			presence = L2CharacterPresence.HUMAN_FEMALE;
		}
		else if ((gender == 0) && (race == 1))
		{
			presence = L2CharacterPresence.ELF_MALE;
		}
		else if ((gender == 1) && (race == 1))
		{
			presence = L2CharacterPresence.ELF_FEMALE;
		}
		else if ((gender == 0) && (race == 3))
		{
			presence = L2CharacterPresence.ORC_MALE;
		}
		else if ((gender == 1) && (race == 3))
		{
			presence = L2CharacterPresence.ORC_FEMALE;
		}
		else if ((gender == 0) && (race == 5))
		{
			presence = L2CharacterPresence.KAMAEL_MALE;
		}
		else if ((gender == 1) && (race == 5))
		{
			presence = L2CharacterPresence.KAMAEL_FEMALE;
		}
	}

	/**
	 * @return The {@link L2AccountInfoGroup} for this {@link L2PcInstance}.
	 */
	public L2AccountInfoGroup getAccountInformation()
	{
		return accountInformation;
	}

	/**
	 * Sets a new {@link L2AccountInfoGroup} to this {@link L2PcInstance}.
	 * @param accountInformation
	 */
	public void setAccountInformation(L2AccountInfoGroup accountInformation)
	{
		this.accountInformation = accountInformation;
	}
}

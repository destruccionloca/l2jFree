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
		{
			e.printStackTrace();
		}
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
	private int								_accesLevel 				= 0;
	private L2CharacterEntry 				_accessLevelLabel;
	private String							_account 					= "";
	private L2AccountInfoGroup 				_accountInformation 		= null;
	private L2CharacterEntry 				_accountLabel;
	private int 							_activeClassId 				= 0;
	private L2CharacterEntry 				_actualClassLabel;
	private int 							_baseClass 					= 0;
	private L2CharacterEntry 				_baseClassLabel;
	/** The clan of the player */
	private L2Clan 							_clan 						= null;
	private long 							_clanCreateExpTime 			= 0;
	private int 							_clanId 					= 0;
	private long 							_clanJoinExpTime 			= 0;
	private double 							_maxCp 						= 0;
	private double 							_currentCp 					= 0;
	private double 							_maxHp 						= 0;
	private double 							_currentHp 					= 0;
	private double 							_maxMp 						= 0;
	private double 							_currentMp 					= 0;
	private L2CharacterEntry 				_cpLabel;
	private L2CharacterEntry 				_hpLabel;
	private L2CharacterEntry 				_mpLabel;
	private int 							_deathPenaltyBuffLevel 		= 0;
	private L2CharacterEntry 				_deathPenaltyLabel;
	private long 							_experience 				= 0;
	private L2CharacterEntry 				_expLabel;
	private int 							_gender 					= 0;
	private boolean 						_hasNewClan;
	private FastMap<Integer, L2HennaGroup> 	_hennaMap 					= new FastMap<Integer, L2HennaGroup>();
	private boolean 						_inJail 					= false;
	/** The Inventory of the player */
	private L2Inventory 					_inventory;
	public long 							_jailTimer 					= 0;
	private int 							_karma 						= 0;
	private L2CharacterEntry 				_karmaLabel;
	private byte 							_level 						= 0;
	private L2CharacterEntry 				_levelLabel;
	private int 							_noble 						= 0;
	private int 							_objectId 					= 0;
	private int 							_online;
	private long 							_onlineTime 				= 0;
	private L2CharacterEntry 				_onlineTimeLabel;
	private L2CharacterEntry 				_pkLabel;
	private int 							_pks 						= 0;
	private L2CharacterPresence 			_presence;
	private L2CharacterEntry 				_pvpLabel;
	private int 							_pvps 						= 0;
	private int 							_race 						= 0;
	private L2SkillsGroup 					_skills 					= null;
	private FastMap<Integer, FastList<L2SkillEntry>> _skillsMap 		= new FastMap<Integer, FastList<L2SkillEntry>>();
	private int 							_sp 						= 0;
	private L2CharacterEntry 				_spLabel;
	private L2SubClassGroup 				_subclasses 				= null;
	private FastList<L2SubClass> 			_subs 						= new FastList<L2SubClass>();


	// Following variables are not yet used.
	@SuppressWarnings("unused")
	private int 							_allianceWithVarkaKetra 	= 0;
	@SuppressWarnings("unused")
	private int 							_apprentice 				= 0;
	@SuppressWarnings("unused")
	private long 							_deleteTime 				= 0;
	@SuppressWarnings("unused")
	private long 							_expBeforeDeath 			= 0;
	@SuppressWarnings("unused")
	private byte 							_face 						= 0;
	@SuppressWarnings("unused")
	private byte 							_hairColor 					= 0;
	@SuppressWarnings("unused")
	private byte 							_hairStyle 					= 0;
	@SuppressWarnings("unused")
	private boolean 						_isIn7sDungeon 				= false;
	@SuppressWarnings("unused")
	private long 							_lastAccess 				= 0;
	@SuppressWarnings("unused")
	private long 							_lastRecomDate 				= 0;
	@SuppressWarnings("unused")
	private int 							_lvlJoinedAcademy 			= 0;
	@SuppressWarnings("unused")
	private int 							_recHave 					= 0;
	@SuppressWarnings("unused")
	private int 							_recLeft 					= 0;
	@SuppressWarnings("unused")
	private int 							_sponsor 					= 0;
	@SuppressWarnings("unused")
	private int 							_subPledge 					= 0;	
	@SuppressWarnings("unused")
	private String 							_title 						= "";
	@SuppressWarnings("unused")
	private int 							_wantsPeace 				= 0;
	@SuppressWarnings("unused")
	private int 							_x 							= 0;
	@SuppressWarnings("unused")
	private int 							_y 							= 0;
	@SuppressWarnings("unused")
	private int 							_z 							= 0;
	@SuppressWarnings("unused")
	private int 							_heading 					= 0;

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
						e.printStackTrace();
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
		if (_hasNewClan)
		{
			ClansManager.getInstance().addClan(_clan);
			_hasNewClan = false;
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
			_accountLabel = new L2CharacterEntry(stats, "Account:", player.getAccount());
			stats.addEntry(_accountLabel);

			// LEVEL:
			_levelLabel = new L2CharacterEntry(stats, "Level: ", player.getLevel());
			stats.addEntry(_levelLabel);

			// EXP:
			_expLabel = new L2CharacterEntry(stats, "Exp: ", player.getExperience());
			stats.addEntry(_expLabel);

			// SP:
			_spLabel = new L2CharacterEntry(stats, "SP: ", player.getSp());
			stats.addEntry(_spLabel);

			// CP:
			_cpLabel = new L2CharacterEntry(stats, "CP: ", (int)player.getCurrentCp() + "/" + (int)player.getMaxCp());
			stats.addEntry(_cpLabel);

			// HP:
			_hpLabel = new L2CharacterEntry(stats, "HP: ", (int)player.getCurrentHp() + "/" + (int)player.getMaxHp());
			stats.addEntry(_hpLabel);

			// MP:
			_mpLabel = new L2CharacterEntry(stats, "MP: ", (int)player.getCurrentMp() + "/" + (int)player.getMaxMp());
			stats.addEntry(_mpLabel);

			// KARMA:
			_karmaLabel = new L2CharacterEntry(stats, "Karma: ", player.getKarma());
			stats.addEntry(_karmaLabel);

			// PVP KILLS:
			_pvpLabel = new L2CharacterEntry(stats, "PvPs: ", player.getPvP());
			stats.addEntry(_pvpLabel);

			// PK KILLS:
			_pkLabel = new L2CharacterEntry(stats, "PKs: ", player.getPks());
			stats.addEntry(_pkLabel);

			// ONLINE TIME:
			_onlineTimeLabel = new L2CharacterEntry(stats, "Online Time in Hours: ", player.getOnlineTime());
			stats.addEntry(_onlineTimeLabel);

			// CLASS:
			_actualClassLabel = new L2CharacterEntry(stats, "Active Class:", player.getActiveclass());
			stats.addEntry(_actualClassLabel);

			// BASE CLASS:
			_baseClassLabel = new L2CharacterEntry(stats, "Base Class:", player.getBaseClass());
			stats.addEntry(_baseClassLabel);

			// ACCESSLEVEL:
			_accessLevelLabel = new L2CharacterEntry(stats, "Accesslevel:", player.getAccessLevel());
			stats.addEntry(_accessLevelLabel);

			// DEATH PENALTY:
			_deathPenaltyLabel = new L2CharacterEntry(stats, "Death Penalty:", player.getDeathPenalty());
			stats.addEntry(_deathPenaltyLabel);
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
		_accountInformation = new L2AccountInfoGroup(this);
		_accountInformation.restore();

		// ADD INVENTORY GROUP.
		_inventory = new L2Inventory(this, "Inventory");
		_inventory.restore();

		// ADD SUBS GROUP
		getSubClassesId(isUpdate);
		if (getSubs().size() > 0)
		{
			_subclasses = new L2SubClassGroup(this, "Sub Classes");
			_subclasses.restore();
		}

		// ADD SKILLS GROUP
		_skills = new L2SkillsGroup(this, "Skill", null);
		_skills.restore();

		// ADD HENNA GROUP
		if (_hennaMap.get(0).getEntries().length > 0)
			addEntry(_hennaMap.get(0));

		// ADD CLAN GROUP
		if (getClanId() != 0)
		{
			_clan = null;
			if (ClansManager.getInstance().isKnownClan(getClanId()))
				_clan = ClansManager.getInstance().getClan(getClanId());
			else
			{
				_clan = new L2Clan(getClanId(), "Clan", null);
				_hasNewClan = true;
			}
			addEntry(_clan);
		}

		setPresence(getRace(), getGenderValue());
	}

	public int getAccessLevel()
	{
		return _accesLevel;
	}

	public String getAccount()
	{
		return _account;
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
			statement.setInt(1, _activeClassId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				str = rset.getString("ClassName");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
			statement.setInt(1, _baseClass);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				str = rset.getString("ClassName");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (con != null)
			con.close();
		return str;
	}

	public L2Clan getClan()
	{
		return _clan;
	}

	public long getclanCreateExpTime()
	{
		return _clanCreateExpTime;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public long getClanJoinExpTime()
	{
		return _clanJoinExpTime;
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
		return _deathPenaltyBuffLevel;
	}

	public long getExperience()
	{
		return _experience;
	}

	/**
	 * @return gender of the Player
	 */
	public String getGender()
	{
		String str = "";
		if (_gender == 0)
			str = "Male";
		else if (_gender == 1)
			str = "Female";
		else
			str = "Error! Warn the developers!";
		return str;
	}

	public int getGenderValue()
	{
		return _gender;
	}

	public L2HennaGroup getHennaGroup(int classIndex)
	{
		return _hennaMap.get(classIndex);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		String key = presenceToKey(getPresence());
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key);
	}

	public L2Inventory getInventory()
	{
		return _inventory;
	}

	public int getKarma()
	{
		return _karma;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getOnline()
	{
		return _online;
	}

	public long getOnlineTime()
	{
		long onlinetimeH = Math.round(((double) _onlineTime / 60 / 60) - 0.5);
		return onlinetimeH;
	}

	public int getPks()
	{
		return _pks;
	}

	public FastList<L2SkillEntry> getPlayerSkillsByClass(int classIndex)
	{
		return _skillsMap.get(classIndex);
	}

	public L2CharacterPresence getPresence()
	{
		return _presence;
	}

	public int getPvP()
	{
		return _pvps;
	}

	public int getRace()
	{
		return _race;
	}

	public int getSp()
	{
		return _sp;
	}

	public double getCurrentCp()
	{
		return _currentCp;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public double getMaxCp()
	{
		return _maxCp;
	}

	public double getMaxHp()
	{
		return _maxHp;
	}

	public double getMaxMp()
	{
		return _maxMp;
	}

	private void getSubClassesId(boolean isUpdate)
	{
		if (isUpdate)
			_subs.clear();
		if (!_subs.isEmpty())
			return;
		String sql = "SELECT class_id, level, class_index FROM `character_subclasses` WHERE charId=?";
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			if (con != null)
			{
				PreparedStatement statement = con.prepareStatement(sql);
				statement.setInt(1, _objectId);
				ResultSet rset = statement.executeQuery();
				int i = 0;
				while (rset.next())
				{
					_subs.add(new L2SubClass(rset.getInt("class_id"), rset.getInt("level"), rset.getInt("class_index")));
					i++;
				}
				rset.close();
				statement.close();
			}
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
		return;
	}

	public FastList<L2SubClass> getSubs()
	{
		return _subs;
	}

	public boolean isInJail()
	{
		return _inJail;
	}

	public int isNoble()
	{
		return _noble;
	}

	public boolean isOnline()
	{
		return (_online == 1);
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
		return IImageKeys.DWARF_MALE; // TODO Find Kamael images
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
			statement.setString(1, _name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_objectId = rset.getInt("charId");
				_activeClassId = rset.getInt("classid");
				_gender = rset.getInt("sex");
				_face = rset.getByte("face");
				_hairColor = rset.getByte("hairColor");
				_hairStyle = rset.getByte("hairStyle");
				_account = rset.getString("account_name");
				_lastAccess = rset.getLong("lastAccess");
				_experience = rset.getLong("exp");
				_expBeforeDeath = (rset.getLong("expBeforeDeath"));
				_level = (rset.getByte("level"));
				_sp = rset.getInt("sp");
				_wantsPeace = (rset.getInt("wantspeace"));
				_heading = (rset.getInt("heading"));
				_karma = (rset.getInt("karma"));
				_pvps = (rset.getInt("pvpkills"));
				_pks = (rset.getInt("pkkills"));
				_onlineTime = (rset.getLong("onlinetime"));
				_noble = (rset.getInt("nobless"));
				_clanJoinExpTime = (rset.getLong("clan_join_expiry_time"));
				if (_clanJoinExpTime < System.currentTimeMillis())
				{
					_clanJoinExpTime = 0;
				}

				_clanCreateExpTime = (rset.getLong("clan_create_expiry_time"));
				if (_clanCreateExpTime < System.currentTimeMillis())
				{
					_clanCreateExpTime = 0;
				}

				_clanId = rset.getInt("clanid");
				_subPledge = (rset.getInt("subpledge"));
				_race = rset.getInt("race");
				_online = rset.getInt("online");
				_deleteTime = (rset.getLong("deletetime"));
				_title = (rset.getString("title"));
				_accesLevel = (rset.getInt("accesslevel"));
				_maxHp = rset.getDouble("maxHp");
				_maxCp = rset.getDouble("maxCp");
				_maxMp = rset.getDouble("maxMp");
				_currentHp = rset.getDouble("curHp");
				_currentCp = rset.getDouble("curCp");
				_currentMp = rset.getDouble("curMp");
				_baseClass = (rset.getInt("base_class"));
				_apprentice = (rset.getInt("apprentice"));
				_sponsor = (rset.getInt("sponsor"));
				_lvlJoinedAcademy = (rset.getInt("lvl_joined_academy"));
				_isIn7sDungeon = ((rset.getInt("isin7sdungeon") == 1) ? true : false);
				_inJail = ((rset.getInt("in_jail") == 1) ? true : false);
				_jailTimer = 0;
				if (_inJail)
					_jailTimer = (rset.getLong("jail_timer"));

				_allianceWithVarkaKetra = (rset.getInt("varka_ketra_ally"));

				_deathPenaltyBuffLevel = (rset.getInt("death_penalty_level"));
				_x = rset.getInt("x");
				_y = rset.getInt("y");
				_z = rset.getInt("z");
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
				System.out.println("L2PcInstance: Player " + _name + " imported correctly.");
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
			_hennaMap.put(0, group0);
			L2HennaGroup group1 = new L2HennaGroup(this, "Hennas");
			_hennaMap.put(1, group1);
			L2HennaGroup group2 = new L2HennaGroup(this, "Hennas");
			_hennaMap.put(2, group2);
			L2HennaGroup group3 = new L2HennaGroup(this, "Hennas");
			_hennaMap.put(3, group3);
			while (rset.next())
			{
				int symbolId = rset.getInt("symbol_id");
				int slot = rset.getInt("slot");
				int classIndex = rset.getInt("class_index");
				_hennaMap.get(classIndex).addEntry(new L2HennaEntry(_hennaMap.get(classIndex), symbolId, slot, classIndex));
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
		_accesLevel = accessLevel;
		_accessLevelLabel.setField(accessLevel);
	}

	/**
	 * Changes the actual account and refreshes the account label.
	 * @param account
	 */
	public void setAccount(String account)
	{
		_account = account;
		_accountLabel.setField(account);
	}

	public void setClan(L2Clan clan)
	{
		_clan = clan;
	}

	public void setClanId(int i)
	{
		_clanId = i;
	}

	public void setInJail(boolean inJail)
	{
		_inJail = inJail;
	}

	/**
	 * Changes a player's KARMA level and refreshes the level of the KARMA
	 * entry.
	 * @param karma
	 */
	public void setKarma(int karma)
	{
		_karma = karma;
		_karmaLabel.setField(karma);
	}

	public void setNoble(int noble)
	{
		_noble = noble;
	}

	public void setOnline(boolean isOnline)
	{
		if (!isOnline)
			_online = 0;
		else
			_online = 1;
	}

	public void setParent(L2GroupEntry parentGroup)
	{
		_parent = parentGroup;
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
			_skillsMap.put(class_index, skills);
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
			_presence = L2CharacterPresence.DWARF_MALE;
		}
		else if ((gender == 1) && (race == 4))
		{
			_presence = L2CharacterPresence.DWARF_FEMALE;
		}
		else if ((gender == 0) && (race == 2))
		{
			_presence = L2CharacterPresence.DARK_ELF_MALE;
		}
		else if ((gender == 1) && (race == 2))
		{
			_presence = L2CharacterPresence.DARK_ELF_FEMALE;
		}
		else if ((gender == 0) && (race == 0))
		{
			_presence = L2CharacterPresence.HUMAN_MALE;
		}
		else if ((gender == 1) && (race == 0))
		{
			_presence = L2CharacterPresence.HUMAN_FEMALE;
		}
		else if ((gender == 0) && (race == 1))
		{
			_presence = L2CharacterPresence.ELF_MALE;
		}
		else if ((gender == 1) && (race == 1))
		{
			_presence = L2CharacterPresence.ELF_FEMALE;
		}
		else if ((gender == 0) && (race == 3))
		{
			_presence = L2CharacterPresence.ORC_MALE;
		}
		else if ((gender == 1) && (race == 3))
		{
			_presence = L2CharacterPresence.ORC_FEMALE;
		}
		else if ((gender == 0) && (race == 5))
		{
			_presence = L2CharacterPresence.KAMAEL_MALE;
		}
		else if ((gender == 1) && (race == 5))
		{
			_presence = L2CharacterPresence.KAMAEL_FEMALE;
		}
	}

	/**
	 * @return The {@link L2AccountInfoGroup} for this {@link L2PcInstance}.
	 */
	public L2AccountInfoGroup getAccountInformation()
	{
		return _accountInformation;
	}

	/**
	 * Sets a new {@link L2AccountInfoGroup} to this {@link L2PcInstance}.
	 * @param accountInformation
	 */
	public void setAccountInformation(L2AccountInfoGroup accountInformation)
	{
		_accountInformation = accountInformation;
	}
}

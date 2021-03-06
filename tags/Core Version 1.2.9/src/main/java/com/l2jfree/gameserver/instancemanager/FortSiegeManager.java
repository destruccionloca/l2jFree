/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.config.L2Properties;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.CombatFlag;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.geoserver.model.Location;

public class FortSiegeManager
{
	protected static final Log		_log	= LogFactory.getLog(FortSiegeManager.class.getName());

	// =========================================================
	private static FortSiegeManager	_instance;

	public static final FortSiegeManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing FortSiegeManager");
			_instance = new FortSiegeManager();
			_instance.loadCommandersFlags();
		}
		return _instance;
	}

	// =========================================================
	// Data Field
	//FIXME: private int										_attackerMaxClans			= 500;		// Max number of clans
	//FIXME: private int										_attackerRespawnDelay		= 20000;	// Time in ms. Changeable in siege.config
	//FIXME: private int										_defenderMaxClans			= 500;		// Max number of clans
	//FIXME: private int										_defenderRespawnDelay		= 10000;	// Time in ms. Changeable in siege.config

	// Fort Siege settings
	private FastMap<Integer, FastList<SiegeSpawn>>	_commanderSpawnList;
	private FastMap<Integer, FastList<CombatFlag>>	_flagList;

	//FIXME: private int										_controlTowerLosePenalty	= 20000;	// Time in ms. Changeable in siege.config
	//FIXME: private int										_flagMaxCount				= 1;		// Changeable in siege.config
	private int										_siegeClanMinLevel			= 4;		// Changeable in siege.config
	private int										_siegeLength				= 120;		// Time in minute. Changeable in siege.config
	private List<FortSiege>							_sieges;

	// =========================================================
	// Constructor
	private FortSiegeManager()
	{
	}

	// =========================================================
	// Method - Public
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}

	/**
	 * Return true if character summon<BR><BR>
	 * @param activeChar The L2Character of the character can summon
	 */
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;
		Fort fort = FortManager.getInstance().getFort(player);

		if (fort == null || fort.getFortId() <= 0)
			sm.addString("You must be on fort ground to summon this");
		else if (!fort.getSiege().getIsInProgress())
			sm.addString("You can only summon this during a siege.");
		else if (player.getClanId() != 0 && fort.getSiege().getAttackerClan(player.getClanId()) == null)
			sm.addString("You can only summon this as a registered attacker.");
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	/**
	 * Return true if the clan is registered or owner of a fort<BR><BR>
	 * @param clan The L2Clan of the player
	 */
	public final boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if (clan == null)
			return false;

		Connection con = null;
		boolean register = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, fortid);
			ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				register = true;
				break;
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: checkIsRegistered(): " + e.getMessage());
		}
        finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

        return register;
	}

	public final void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}

	// =========================================================
	// Method - Private
	private final void loadCommandersFlags()
	{
		try
		{
			Properties siegeSettings = new L2Properties(Config.FORTSIEGE_CONFIGURATION_FILE);

			// Siege spawns settings
			_commanderSpawnList = new FastMap<Integer, FastList<SiegeSpawn>>();
			_flagList = new FastMap<Integer, FastList<CombatFlag>>();

			for (Fort fort : FortManager.getInstance().getForts())
			{
				FastList<SiegeSpawn> _commanderSpawns = new FastList<SiegeSpawn>();
				FastList<CombatFlag> _flagSpawns = new FastList<CombatFlag>();

				for (int i = 1; i < 5; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName() + "Commander" + Integer.toString(i), "");

					if (_spawnParams.length() == 0)
						break;

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());

						_commanderSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						_log.warn("Error while loading commander(s) for " + fort.getName() + " fort.");
					}
				}

				_commanderSpawnList.put(fort.getFortId(), _commanderSpawns);

				for (int i = 1; i < 4; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName() + "Flag" + Integer.toString(i), "");

					if (_spawnParams.length() == 0)
						break;

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int flag_id = Integer.parseInt(st.nextToken());

						_flagSpawns.add(new CombatFlag(fort.getFortId(), x, y, z, 0, flag_id));
					}
					catch (Exception e)
					{
						_log.warn("Error while loading flag(s) for " + fort.getName() + " fort.");
					}
				}
				_flagList.put(fort.getFortId(), _flagSpawns);

			}

		}
		catch (Exception e)
		{
			//_initialized = false;
			_log.error("Error while loading fortsiege data.", e);
		}
	}

	public final void reload()
	{
		_flagList.clear();
		_commanderSpawnList.clear();
		Config.loadFortSiegeConfig();
		loadCommandersFlags();
	}

	// =========================================================
	// Property - Public
	public final FastList<SiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		if (_commanderSpawnList.containsKey(_fortId))
			return _commanderSpawnList.get(_fortId);

		return null;
	}

	public final FastList<CombatFlag> getFlagList(int _fortId)
	{
		if (_flagList.containsKey(_fortId))
			return _flagList.get(_fortId);

		return null;
	}

	public final FortSiege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
			if (fort.getSiege().checkIfInZone(x, y, z))
				return fort.getSiege();
		return null;
	}

	/** * get active siege for clan ** */
	public final FortSiege getSiege(L2Clan clan)
	{
		if (clan == null)
			return null;
		for (Fort fort : FortManager.getInstance().getForts())
		{
			FortSiege siege = fort.getSiege();
			if (siege.getIsInProgress() && (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan)))
				return siege;
		}
		return null;
	}

	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}

	public final int getSiegeLength()
	{
		return _siegeLength;
	}

	public final List<FortSiege> getSieges()
	{
		if (_sieges == null)
			_sieges = new FastList<FortSiege>();
		return _sieges;
	}

	public final void addSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
			_sieges = new FastList<FortSiege>();
		_sieges.add(fortSiege);
	}

	public boolean isCombat(int itemId)
	{
		return (itemId == 9819);
	}

	public void activateCombatFlag(L2PcInstance player, L2ItemInstance item)
	{
		if (!checkIfCanPickup(player))
			return;

		Fort fort = FortManager.getInstance().getFort(player);

		FastList<CombatFlag> fcf = _flagList.get(fort.getFortId());
		for (CombatFlag cf : fcf)
		{
			if (cf.itemInstance == item)
			{
				cf.activate(player, item);
			}
		}
	}

	public boolean checkIfCanPickup(L2PcInstance player)
	{
		// Cannot own 2 combat flag
		if (player.isCombatFlagEquipped())
		{
			player.sendMessage("You already have the combat flag");
			return false;
		}

		// Here check if is siege is in progress
		// Here check if is siege is attacker
		Fort fort = FortManager.getInstance().getFort(player);

		if (fort == null || fort.getFortId() <= 0)
		{
			player.sendMessage("You must be on fort ground to pickup Combat Flag");
			return false;
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			player.sendMessage("You can only pickup Combat Flag during a siege.");
			return false;
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			player.sendMessage("You must be an attacker to pickup Combat Flag");
			return false;
		}

		return true;
	}

	public static boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;

		// Get siege battleground
		FortSiege siege = FortSiegeManager.getInstance().getSiege(player);

		if (siege == null)
			sm.addString("You must be on fort ground to use strider siege assault");
		else if (!siege.getIsInProgress())
			sm.addString("You can only use strider siege assault during a siege.");
		else if (!(player.getTarget() instanceof L2DoorInstance))
			sm.addString("You can only use strider siege assault on doors and walls.");
		else if (!player.isRidingStrider())
			sm.addString("You can only use strider siege assault when on strider.");
		else
			return true;

		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;

		// Get siege battleground
		FortSiege siege = FortSiegeManager.getInstance().getSiege(player);

		if (siege == null)
			sm.addString("You must be on fort ground to place a flag");
		else if (!siege.getIsInProgress())
			sm.addString("You can only place a flag during a siege.");
		else if (siege.getAttackerClan(player.getClan()) == null)
			sm.addString("You must be an attacker to place a flag");
		else if (player.getClan() == null || !player.isClanLeader())
			sm.addString("You must be a clan leader to place a flag");
		else if (siege.getAttackerClan(player.getClan()).getNumFlags() >= Config.FORTSIEGE_FLAG_MAX_COUNT)
			sm.addString("You have already placed the maximum number of flags possible");
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public void dropCombatFlag(L2PcInstance player)
	{
		Fort fort = FortManager.getInstance().getFort(player);
		FastList<CombatFlag> fcf = _flagList.get(fort.getFortId());
		for (CombatFlag cf : fcf)
		{
			if (cf.playerId == player.getObjectId())
			{
				_log.info("found cf ");
				cf.dropIt();
				cf.spawnMe();
			}
		}

	}

	public class SiegeSpawn
	{
		Location	_location;
		private int	_npcId;
		private int	_heading;
		private int	_fortId;
		private int	_hp;

		public SiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id)
		{
			_fortId = fort_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}

		public SiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_fortId = fort_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}

		public int getFortId()
		{
			return _fortId;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getHp()
		{
			return _hp;
		}

		public Location getLocation()
		{
			return _location;
		}
	}
}
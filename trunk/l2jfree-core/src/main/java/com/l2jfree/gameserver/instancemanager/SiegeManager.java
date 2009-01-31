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
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.model.Location;

public class SiegeManager
{
	protected static Log		_log	= LogFactory.getLog(SiegeManager.class.getName());
	private static SiegeManager	_instance;

	public static final SiegeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SiegeManager();
			_instance.loadTowerArtefacts();
		}
		return _instance;
	}

	private FastMap<Integer, FastList<SiegeSpawn>>	_artefactSpawnList;

	private FastMap<Integer, FastList<SiegeSpawn>>	_controlTowerSpawnList;

	private SiegeManager()
	{
	}

	public final void addSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble()))
			character.addSkill(sk, false);
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(L2Object obj)
	{
		return (getSiege(obj) != null);
	}

	/** Return true if object is inside zone */
	public final boolean checkIfInZone(int x, int y, int z)
	{
		return (getSiege(x, y, z) != null);
	}

	/**
	 * Return true if character can place a flag<BR><BR>
	 * 
	 * @param player
	 *            The L2PcInstance of the character placing the flag
	 * @param isCheckOnly
	 *            if false, it will send a notification to the player telling
	 *            him why it failed
	 */
	public static boolean checkIfOkToPlaceFlag(L2PcInstance player, boolean isCheckOnly)
	{
		// Get siege battleground
		L2Clan clan = player.getClan();
		Siege siege = SiegeManager.getInstance().getSiege(player);
		Castle castle = (siege == null) ? null : siege.getCastle();

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);

		if (clan == null || clan.getLeaderId() != player.getObjectId())
			sm.addString("You must be a clan leader to place a flag.");
		else if (siege == null || !siege.getIsInProgress())
			sm.addString("You can only place a flag during a siege.");
		else if (castle == null || !castle.checkIfInZoneHeadQuarters(player))
			sm.addString("You must be on castle ground to place a flag.");
		else if (siege.getAttackerClan(clan) == null)
			sm.addString("You must be an attacker to place a flag.");
		else if (castle.getSiege().getAttackerClan(clan).getNumFlags() >= Config.SIEGE_FLAG_MAX_COUNT)
			sm.addString("You have already placed the maximum number of flags possible.");
		else
			return true;

		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}

	/**
	 * Return true if character can summon<BR><BR>
	 * 
	 * @param player
	 *            The L2PcInstance of the character can summon
	 */
	public final boolean checkIfOkToSummon(L2PcInstance player, boolean isCheckOnly)
	{
		// Get siege battleground
		Siege siege = SiegeManager.getInstance().getSiege(player);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);

		if (siege == null)
			sm.addString("You must be on castle ground to summon this.");
		else if (!siege.getIsInProgress())
			sm.addString("You can only summon this during a siege.");
		else if (player.getClanId() != 0 && siege.getAttackerClan(player.getClanId()) == null)
			sm.addString("You can only summon this as a registered attacker.");
		else
			return true;

		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}

	/**
	 * Return true if character can use Strider Siege Assault skill <BR><BR>
	 * 
	 * @param activeChar
	 *            The L2Character of the character placing the flag
	 * @param isCheckOnly
	 *            if false, it will send a notification to the player telling
	 *            him why it failed
	 */
	public static boolean checkIfOkToUseStriderSiegeAssault(L2PcInstance player, boolean isCheckOnly)
	{
		// Get siege battleground
		Siege siege = SiegeManager.getInstance().getSiege(player);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1);

		if (siege == null)
			sm.addString("You must be on castle ground to use strider siege assault.");
		else if (!siege.getIsInProgress())
			sm.addString("You can only use strider siege assault during a siege.");
		else if (!(player.getTarget() instanceof L2DoorInstance))
			sm.addString("You can only use strider siege assault on doors and walls.");
		else if (!player.isRidingStrider() && !player.isRidingRedStrider())
			sm.addString("You can only use strider siege assault when on strider.");
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	/**
	 * Return true if the clan is registered or owner of a castle<BR>
	 * <BR>
	 * 
	 * @param clan
	 *            The L2Clan of the player
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
			return false;

		if (clan.getHasCastle() > 0)
			return true;

		Connection con = null;
		boolean register = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
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
			_log.error("Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
        finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

        return register;
	}

	public final void removeSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble()))
			character.removeSkill(sk);
	}

	// =========================================================
	// Method - Private
	private final void loadTowerArtefacts()
	{
		try
		{
			Properties siegeSettings = new L2Properties(Config.SIEGE_CONFIGURATION_FILE);

			// Siege spawns settings
			_controlTowerSpawnList = new FastMap<Integer, FastList<SiegeSpawn>>();
			_artefactSpawnList = new FastMap<Integer, FastList<SiegeSpawn>>();

			for (Castle castle : CastleManager.getInstance().getCastles().values())
			{
				FastList<SiegeSpawn> _controlTowersSpawns = new FastList<SiegeSpawn>();

				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");

					if (_spawnParams.length() == 0)
						break;

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						int hp = Integer.parseInt(st.nextToken());

						_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
					}
					catch (Exception e)
					{
						_log.error("Error while loading control tower(s) for " + castle.getName() + " castle.", e);
					}
				}

				FastList<SiegeSpawn> _artefactSpawns = new FastList<SiegeSpawn>();

				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");

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

						_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						_log.error("Error while loading artefact(s) for " + castle.getName() + " castle.", e);
					}
				}

				_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
				_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);

				_log.info("SiegeManager: loaded controltowers[" + Integer.toString(_controlTowersSpawns.size()) + "] artefacts["
						+ Integer.toString(_artefactSpawns.size()) + "] castle[" + castle.getName() + "]");
			}
		}
		catch (Exception e)
		{
			// _initialized = false;
			_log.error("Error while loading siege data.", e);
		}
	}

	public final void reload()
	{
		_artefactSpawnList.clear();
		_controlTowerSpawnList.clear();
		Config.loadSiegeConfig();
		loadTowerArtefacts();
	}

	public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		if (_artefactSpawnList.containsKey(_castleId))
			return _artefactSpawnList.get(_castleId);
		return null;
	}

	public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		if (_controlTowerSpawnList.containsKey(_castleId))
			return _controlTowerSpawnList.get(_castleId);
		return null;
	}

	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	/** * get active siege for clan ** */
	public final Siege getSiege(L2Clan clan)
	{
		if (clan == null)
			return null;
		for (Castle castle : CastleManager.getInstance().getCastles().values())
		{
			Siege siege = castle.getSiege();
			if (siege.getIsInProgress() && (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan)))
				return siege;
		}
		return null;
	}

	public final Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles().values())
			if (castle.getSiege().checkIfInZone(x, y, z))
				return castle.getSiege();
		return null;
	}

	public final List<Siege> getSieges()
	{
		FastList<Siege> sieges = new FastList<Siege>();
		for (Castle castle : CastleManager.getInstance().getCastles().values())
			sieges.add(castle.getSiege());
		return sieges;
	}

	public class SiegeSpawn
	{
		Location	_location;
		private int	_npcId;
		private int	_heading;
		private int	_castleId;
		private int	_hp;

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}

		public int getCastleId()
		{
			return _castleId;
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
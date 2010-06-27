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
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.SpawnTable;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.tools.random.Rnd;

/**
 * @author Psycho(killer1888) / L2jfree
 */

public final class SeedOfDestructionManager
{
	private static final Log 	_log = LogFactory.getLog(SeedOfDestructionManager.class);
	private final Map<String,Long> _variables = new FastMap<String,Long>();
	private final int[] 		DOORLIST = {12240001,12240002,12240003,12240004,12240005,12240006,12240007,12240008,12240009,12240010,12240011,12240012,12240013,12240014,12240015,12240016,12240017,12240018,12240019,12240020,12240021,12240022,12240023,12240024,12240025,12240026,12240027,12240028,12240029,12240030,12240031};
	private final int					_tiatKilled = 0;
	private final FastList<L2Npc> _energySeeds = new FastList<L2Npc>();
	private final L2Npc _tiat = null;
	private final FastList<L2Npc> _dimensionMovingDevices = new FastList<L2Npc>();

	private final int[]			ENERGY_SEEDS = {18678,18679,18680,18681,18682,18683};

	private final int[][]		ENERGY_SEED_SPAWN = {
			{-242237, 217879, -12394},
			{-241649, 217719, -12394},
			{-241922, 218489, -12397},
			{-240746, 217860, -12397},
			{-240417, 217458, -12397},
			{-240534, 218653, -12388},
			{-239173, 216830, -12649},
			{-238906, 216518, -12649},
			{-238206, 216701, -12649},
			{-238999, 215447, -12652},
			{-238486, 215135, -12649},
			{-238280, 215474, -12649},
			{-239105, 213439, -12780},
			{-238834, 212822, -12778},
			{-238244, 213128, -12781},
			{-239111, 211958, -12779},
			{-239132, 211597, -12781},
			{-238547, 211706, -12778},
			{-243172, 214171, -12520},
			{-242598, 213733, -12523},
			{-242247, 214327, -12511},
			{-243366, 212458, -12511},
			{-242614, 212720, -12520},
			{-242753, 212436, -12520},
			{-243154, 210528, -12650},
			{-242614, 210159, -12653},
			{-242395, 210647, -12650},
			{-242233, 209131, -12650},
			{-242459, 208834, -12650},
			{-243070, 209040, -12653},
			{-241010, 207471, -12906},
			{-240632, 207724, -12906},
			{-240356, 208202, -12909},
			{-239338, 207599, -12909},
			{-239201, 208046, -12907},
			{-239305, 208410, -12900},
			{-245476, 212895, -12397},
			{-246056, 212524, -12394},
			{-245536, 212288, -12397},
			{-245328, 211162, -12388},
			{-245718, 211189, -12394},
			{-246451, 211172, -12385},
			{-245559, 209314, -12523},
			{-245344, 208535, -12523},
			{-246454, 208931, -12523},
			{-245341, 207582, -12514},
			{-245509, 207446, -12523},
			{-246289, 207672, -12526},
			{-244079, 207004, -12773},
			{-243477, 207016, -12773},
			{-243761, 206029, -12782},
			{-242350, 207001, -12770},
			{-242200, 206167, -12779},
			{-242565, 205946, -12779},
			{-248606, 220630, -12449},
			{-248992, 220236, -12458},
			{-248843, 219642, -12458},
			{-250334, 220636, -12449},
			{-250283, 219882, -12458},
			{-250528, 219342, -12461},
			{-252245, 218144, -12341},
			{-252746, 217314, -12302},
			{-251663, 217265, -12302},
			{-250115, 216490, -12260},
			{-250370, 215831, -12216},
			{-252856, 216348, -12258}};

	public static SeedOfDestructionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private SeedOfDestructionManager()
	{
		loadVariables();
		String state = "";

		if (_variables.get("sod_state") == 1)
			state = "attack";
		else if (_variables.get("sod_state") == 2)
		{
			state = "defense";
			startDefenseMode();
		}
		else
		{
			state = "hunting ground";
			startHuntingGround();
		}

		_log.info("Seed of Destruction: initialized (currently in "+state+" state).");
		_log.info("Seed of Destruction: Tiat killed "+_variables.get("sod_tiatKills")+" times.");
	}

	public void startDefenseMode()
	{
		_dimensionMovingDevices.add(spawnNpc(18702, -251433, 218130, -12336, 48643, 60, 60, 60));
		_dimensionMovingDevices.add(spawnNpc(18702, -252047, 218130, -12336, 48643, 60, 60, 60));
		_dimensionMovingDevices.add(spawnNpc(18702, -250819, 218130, -12336, 48643, 60, 60, 60));
	}

	protected class SwitchToDefenseMode implements Runnable
	{
		public void run()
		{
			_log.info("Seed of Destruction: Defense mode started.");

			setTiatKilled(0);
			setHuntingGroundTimer(0);
			setState(2);
			startDefenseMode();

			for (L2Npc seed : _energySeeds)
			{
				seed.getSpawn().stopRespawn();
				seed.deleteMe();
			}

			for (int doorId : DOORLIST)
			{
				DoorTable.getInstance().getDoor(doorId).openMe();
			}

			_energySeeds.clear();
		}
	}

	private L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int respawnMinDelay, int respawnMaxDelay)
	{
		final L2Spawn spawnDat = new L2Spawn(npcId);
		spawnDat.setLocx(x);
		spawnDat.setLocy(y);
		spawnDat.setLocz(z);
		spawnDat.setAmount(1);
		spawnDat.setHeading(0);
		spawnDat.setRespawnDelay(heading);
		spawnDat.setRespawnMinDelay(respawnMinDelay);
		spawnDat.setRespawnMaxDelay(respawnMaxDelay);
		spawnDat.startRespawn();
		spawnDat.setInstanceId(0);
		
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		
		return spawnDat.doSpawn();
	}

	private final class SaveToDB implements Runnable
	{
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("REPLACE INTO gracia_instances VALUES (?,?)");

				for (String key: _variables.keySet())
				{
					statement.setString(1, key);
					statement.setLong(2, _variables.get(key));
					statement.execute();
				}
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Could not save Seed of Destruction variables.", e);
				return;
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private void loadVariables()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT * FROM gracia_instances WHERE variable like ?");
			statement.setString(1, "sod_%");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				_variables.put(rset.getString("variable"), rset.getLong("value"));
			}
			
			rset.close();
		}
		catch (Exception e)
		{
			_log.error("Could not load Seed of Destruction variables", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public long getState()
	{
		return _variables.get("sod_state");
	}

	public long getTiatKilled()
	{
		return _variables.get("sod_tiatKills");
	}

	public void setState(long state)
	{
		//1 = attack, 2 = defense, else Hunting ground
		_variables.put("sod_state", state);
	}

	public void increaseTiatKills()
	{
		_variables.put("sod_tiatKills", _variables.get("sod_tiatKills") + 1);

		if (_variables.get("sod_tiatKills") >= 10)
			startHuntingGround();
	}

	public void setTiatKilled(int kills)
	{
		_variables.put("sod_tiatKills", _variables.get("sod_tiatKills") + kills);

		if (_variables.get("sod_tiatKills") >= 10)
			startHuntingGround();
	}

	public void setHuntingGroundTimer(long time)
	{
		_variables.put("sod_defenseSwitch", time);
	}

	public long getHuntingGroundTimer()
	{
		return _variables.get("sod_defenseSwitch");
	}
	
	public void saveToDB()
	{
		new SaveToDB().run();
	}

	public void runDefenseMode()
	{
		new SwitchToDefenseMode().run();
	}

	public void startHuntingGround()
	{
		_log.info("Seed of Destruction: Hunting ground started.");
		setState(3);

		long timer = 0;
		if (getHuntingGroundTimer() == 0)
		{
			timer = 12*60*60*1000;
			setHuntingGroundTimer(System.currentTimeMillis() + timer);
		}
		else
			timer = getHuntingGroundTimer() - System.currentTimeMillis();

		for (int doorId : DOORLIST)
		{
			DoorTable.getInstance().getDoor(doorId).openMe();
		}

		for (int[] spawnLocs : ENERGY_SEED_SPAWN)
		{
			_energySeeds.add(spawnNpc(ENERGY_SEEDS[Rnd.get(ENERGY_SEEDS.length)], spawnLocs[0], spawnLocs[1], spawnLocs[2], 0, 60, 60, 7200));
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new SwitchToDefenseMode(), timer);

		return;
	}
	
	private static final class SingletonHolder
	{
		public static final SeedOfDestructionManager INSTANCE = new SeedOfDestructionManager();
	}
}

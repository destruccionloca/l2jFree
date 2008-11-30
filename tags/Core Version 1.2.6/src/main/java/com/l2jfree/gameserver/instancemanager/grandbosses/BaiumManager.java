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
package com.l2jfree.gameserver.instancemanager.grandbosses;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SpawnTable;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.GrandBossState;
import com.l2jfree.gameserver.network.serverpackets.Earthquake;
import com.l2jfree.gameserver.network.serverpackets.SocialAction;
import com.l2jfree.gameserver.templates.L2NpcTemplate;
import com.l2jfree.tools.random.Rnd;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * 
 * This class ...
 * control for sequence of fight against Baium.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class BaiumManager extends BossLair
{
	private static BaiumManager		_instance;

	public final static int			BAIUM_NPC = 29025;
	public final static int			BAIUM = 29020;
	public final static int			ARCHANGEL = 29021;
	public final static int			TELEPORT_CUBE = 29055;

	public final static int			STATUE_LOCATION[]		=	{ 115996, 17417, 10106, 41740 };
	protected L2Spawn				_statueSpawn			= null;

	// location of arcangels.
	public final static int			ANGEL_LOCATION[][]			=
																{
																{ 113004, 16209, 10076, 60242 },
																{ 114053, 16642, 10076, 4411 },
																{ 114563, 17184, 10076, 49241 },
																{ 116356, 16402, 10076, 31109 },
																{ 115015, 16393, 10076, 32760 },
																{ 115481, 15335, 10076, 16241 },
																{ 114680, 15407, 10051, 32485 },
																{ 114886, 14437, 10076, 16868 },
																{ 115391, 17593, 10076, 55346 },
																{ 115245, 17558, 10076, 35536 } };
	protected List<L2Spawn>					_angelSpawns				= new FastList<L2Spawn>();
	protected List<L2NpcInstance>			_angels						= new FastList<L2NpcInstance>();

	// location of teleport cube.
	public final static int			CUBE_LOCATION[]				= { 115203, 16620, 10078, 0 };
	protected L2Spawn				_teleportCubeSpawn			= null;
	protected L2NpcInstance			_teleportCube				= null;

	// instance of statue of Baium.
	protected L2NpcInstance			_npcBaium;

	// spawn data of monsters.
	protected Map<Integer, L2Spawn>	_monsterSpawn				= new FastMap<Integer, L2Spawn>();

	// instance of monsters.
	protected List<L2NpcInstance>	_monsters					= new FastList<L2NpcInstance>();

	// tasks.
	protected ScheduledFuture<?>	_cubeSpawnTask				= null;
	protected ScheduledFuture<?>	_monsterSpawnTask			= null;
	protected ScheduledFuture<?>	_intervalEndTask			= null;
	protected ScheduledFuture<?>	_activityTimeEndTask		= null;
	protected ScheduledFuture<?>	_socialTask					= null;
	protected ScheduledFuture<?>	_mobiliseTask				= null;
	protected ScheduledFuture<?>	_moveAtRandomTask			= null;
	protected ScheduledFuture<?>	_socialTask2				= null;
	protected ScheduledFuture<?>	_killPcTask					= null;
	protected ScheduledFuture<?>	_callAngelTask				= null;
	protected ScheduledFuture<?>	_sleepCheckTask				= null;
	protected ScheduledFuture<?>	_speakTask					= null;

	// status in lair.
	protected long					_lastAttackTime				= 0;
	protected String				_words						= "Don't obstruct my sleep! Die!";

	public BaiumManager()
	{
		_questName = "baium";
		_state = new GrandBossState(BAIUM);
	}

	public static BaiumManager getInstance()
	{
		if (_instance == null)
			_instance = new BaiumManager();
		return _instance;
	}

	// initialize
	@Override
	public void init()
	{
		// setting spawn data of monsters.
		try
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;

			//Statue of Baium  
			template1 = NpcTable.getInstance().getTemplate(BAIUM_NPC);
			_statueSpawn = new L2Spawn(template1);
			_statueSpawn.setAmount(1);
			_statueSpawn.setLocx(STATUE_LOCATION[0]);
			_statueSpawn.setLocy(STATUE_LOCATION[1]);
			_statueSpawn.setLocz(STATUE_LOCATION[2]);
			_statueSpawn.setHeading(STATUE_LOCATION[3]);
			_statueSpawn.setRespawnDelay(Config.FWB_ACTIVITYTIMEOFBAIUM * 2);
			_statueSpawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(_statueSpawn, false);

			// Baium.
			template1 = NpcTable.getInstance().getTemplate(BAIUM);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(Config.FWB_ACTIVITYTIMEOFBAIUM * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(BAIUM, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(TELEPORT_CUBE);
			L2Spawn spawnDat = new L2Spawn(Cube);
			spawnDat.setAmount(1);
			spawnDat.setLocx(CUBE_LOCATION[0]);
			spawnDat.setLocy(CUBE_LOCATION[1]);
			spawnDat.setLocz(CUBE_LOCATION[2]);
			spawnDat.setHeading(CUBE_LOCATION[3]);
			spawnDat.setRespawnDelay(60);
			spawnDat.setLocation(0);
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
			_teleportCubeSpawn = spawnDat;
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of archangels.
		try
		{
			L2NpcTemplate angel = NpcTable.getInstance().getTemplate(ARCHANGEL);
			L2Spawn spawnDat;
			_angelSpawns.clear();

			// 5 random numbers of 10, no duplicates
			FastList<Integer> random = new FastList<Integer>();
			for (int i = 0; i < 5; i++)
			{
				int r = -1;
				while (r == -1 || random.contains(r))
					r = Rnd.get(10);
				random.add(r);
			}

			for (int i : random)
			{
				spawnDat = new L2Spawn(angel);
				spawnDat.setAmount(1);
				spawnDat.setLocx(ANGEL_LOCATION[i][0]);
				spawnDat.setLocy(ANGEL_LOCATION[i][1]);
				spawnDat.setLocz(ANGEL_LOCATION[i][2]);
				spawnDat.setHeading(ANGEL_LOCATION[i][3]);
				spawnDat.setRespawnDelay(300000);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_angelSpawns.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		_log.info("BaiumManager : State of Baium is " + _state.getState() + ".");
		if (_state.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
			_statueSpawn.doSpawn();
		else if (_state.getState().equals(GrandBossState.StateEnum.ALIVE))
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
			_statueSpawn.doSpawn();
		}
		else if (_state.getState().equals(GrandBossState.StateEnum.INTERVAL) || _state.getState().equals(GrandBossState.StateEnum.DEAD))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		_log.info("BaiumManager : Next spawn date of Baium is " + dt + ".");
		_log.info("BaiumManager : Init BaiumManager.");
	}

	// Archangel advent.
	protected void spawnArchangels()
	{
		for (L2Spawn spawn : _angelSpawns)
		{
			_angels.add(spawn.doSpawn());
		}
	}

	// Archangel ascension.
	public void deleteArchangels()
	{
		for (L2NpcInstance angel : _angels)
		{
			angel.getSpawn().stopRespawn();
			angel.deleteMe();
		}
		_angels.clear();
	}

	// do spawn Baium.
	public void spawnBaium(L2NpcInstance NpcBaium)
	{
		_npcBaium = NpcBaium;

		// get target from statue,to kill a player of make Baium awake.
		L2PcInstance target = (L2PcInstance) _npcBaium.getTarget();

		// do spawn.
		L2Spawn baiumSpawn = _monsterSpawn.get(BAIUM);
		baiumSpawn.setLocx(_npcBaium.getX());
		baiumSpawn.setLocy(_npcBaium.getY());
		baiumSpawn.setLocz(_npcBaium.getZ());
		baiumSpawn.setHeading(_npcBaium.getHeading());

		//delete statue.  
		_npcBaium.deleteMe();

		final L2GrandBossInstance baium = (L2GrandBossInstance) baiumSpawn.doSpawn();
		_monsters.add(baium);

		_state.setRespawnDate(Rnd.get(Config.FWB_FIXINTERVALOFBAIUM, Config.FWB_FIXINTERVALOFBAIUM + Config.FWB_RANDOMINTERVALOFBAIUM)
				+ Config.FWB_ACTIVITYTIMEOFBAIUM);
		_state.setState(GrandBossState.StateEnum.ALIVE);
		_state.update();

		// set last attack time.
		setLastAttackTime();

		// stop respawn of statue.
		_npcBaium.getSpawn().stopRespawn();

		// do social.
		baium.setIsImmobilized(true);
		baium.setIsInSocialAction(true);



		SocialAction sa = new SocialAction(baium.getObjectId(), 2);
		baium.broadcastPacket(sa);

		_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 3), 15000);

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				Earthquake eq = new Earthquake(baium.getX(), baium.getY(), baium.getZ(), 40, 5);
				baium.broadcastPacket(eq);
			}
		}, 25000);

		_socialTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 1), 25000);

		_killPcTask = ThreadPoolManager.getInstance().scheduleGeneral(new KillPc(target, baium), 26000);

		_callAngelTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallArchAngel(), 35000);

		_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(baium), 35500);

		// move at random.
		if (Config.FWB_MOVEATRANDOM)
		{
			L2CharPosition pos = new L2CharPosition(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
			_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(baium, pos), 36000);
		}

		// set delete task.
		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(), Config.FWB_ACTIVITYTIMEOFBAIUM);
		_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 60000);
	}

	// at end of activity time.
	private class ActivityTimeEnd implements Runnable
	{
		public void run()
		{
			if (_state.getState().equals(GrandBossState.StateEnum.DEAD))
				setIntervalEndTask();
			else
				sleepBaium();
		}
	}

	// clean Baium's lair.
	@Override
	public void setUnspawn()
	{
		// eliminate players.
		banishForeigners();

		// delete monsters.
		deleteArchangels();
		for (L2NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// delete teleport cube.
		if (_teleportCube != null)
		{
			_teleportCube.getSpawn().stopRespawn();
			_teleportCube.deleteMe();
			_teleportCube = null;
		}

		// not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_intervalEndTask != null)
		{
			_intervalEndTask.cancel(true);
			_intervalEndTask = null;
		}
		if (_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(true);
			_activityTimeEndTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}
		if (_socialTask2 != null)
		{
			_socialTask2.cancel(true);
			_socialTask2 = null;
		}
		if (_killPcTask != null)
		{
			_killPcTask.cancel(true);
			_killPcTask = null;
		}
		if (_callAngelTask != null)
		{
			_callAngelTask.cancel(true);
			_callAngelTask = null;
		}
		if (_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(true);
			_sleepCheckTask = null;
		}
		if (_speakTask != null)
		{
			_speakTask.cancel(true);
			_speakTask = null;
		}
	}

	// do spawn teleport cube.
	public void spawnCube()
	{
		_teleportCube = _teleportCubeSpawn.doSpawn();
	}

	// start interval.
	public void setIntervalEndTask()
	{
		setUnspawn();

		//init state of Baium's lair.  
		if (!_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
		{
			_state.setRespawnDate(Rnd.get(Config.FWB_FIXINTERVALOFBAIUM, Config.FWB_FIXINTERVALOFBAIUM + Config.FWB_RANDOMINTERVALOFBAIUM));
			_state.setState(GrandBossState.StateEnum.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	// at end of interval.
	private class IntervalEnd implements Runnable
	{
		public void run()
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();

			// statue of Baium respawn.
			_statueSpawn.doSpawn();
		}
	}

	// setting teleport cube spawn task.
	public void setCubeSpawn()
	{
		_state.setState(GrandBossState.StateEnum.DEAD);
		_state.update();

		deleteArchangels();

		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);
	}

	// do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		public void run()
		{
			spawnCube();
		}
	}

	// do social.
	private class Social implements Runnable
	{
		private int				_action;
		private L2NpcInstance	_npc;

		public Social(L2NpcInstance npc, int actionId)
		{
			_npc = npc;
			_action = actionId;
		}

		public void run()
		{
			SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
			_npc.broadcastPacket(sa);
		}
	}

	// action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private L2GrandBossInstance	_boss;

		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}

		public void run()
		{
			_boss.setIsImmobilized(false);
			_boss.setIsInSocialAction(false);

			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}

	// Move at random on after Baium appears.
	private class MoveAtRandom implements Runnable
	{
		private L2NpcInstance	_npc;
		private L2CharPosition	_pos;

		public MoveAtRandom(L2NpcInstance npc, L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}

		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}

	// call Arcangels
	private class CallArchAngel implements Runnable
	{
		public void run()
		{
			spawnArchangels();
		}
	}

	// kill pc
	private class KillPc implements Runnable
	{
		private L2PcInstance		_target;
		private L2GrandBossInstance	_boss;

		public KillPc(L2PcInstance target, L2GrandBossInstance boss)
		{
			_target = target;
			_boss = boss;
		}

		public void run()
		{
			L2Skill skill = SkillTable.getInstance().getInfo(4136, 1);
			if (_target != null && skill != null)
			{
				_boss.setTarget(_target);
				_boss.doCast(skill);
			}
		}
	}

	// Baium sleeps if not attacked for 30 minutes.
	public void sleepBaium()
	{
		setUnspawn();
		_state.setState(GrandBossState.StateEnum.NOTSPAWN);
		_state.update();

		// statue of Baium respawn.
		_statueSpawn.doSpawn();
	}

	public void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	private class CheckLastAttack implements Runnable
	{
		public void run()
		{
			if (_state.getState().equals(GrandBossState.StateEnum.ALIVE))
			{
				if (_lastAttackTime + Config.FWB_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleepBaium();
				else
				{
					if (_sleepCheckTask != null)
					{
						_sleepCheckTask.cancel(true);
						_sleepCheckTask = null;
					}
					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 60000);
				}
			}
		}
	}
}
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
import com.l2jfree.gameserver.datatables.SpawnTable;
import com.l2jfree.gameserver.model.L2CharPosition;
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

	//location of Statue of Baium  
	private final int				_StatueofBaiumId			= 29025;
	private final int				_StatueofBaiumLocation[]	=
																{ 115996, 17417, 10106, 41740 };
	protected L2Spawn				_StatueofBaiumSpawn			= null;

	// location of arcangels.
	private final int				_angelLocation[][]			=
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
	protected List<L2Spawn>					_angelSpawn1				= new FastList<L2Spawn>();
	protected List<L2Spawn>					_angelSpawn2				= new FastList<L2Spawn>();
	protected Map<Integer, List<L2Spawn>>	_angelSpawn					= new FastMap<Integer, List<L2Spawn>>();
	protected List<L2NpcInstance>			_angels						= new FastList<L2NpcInstance>();

	// location of teleport cube.
	protected final int				_teleportCubeId				= 29055;
	protected final int				_teleportCubeLocation[][]	=
																{
																{ 115203, 16620, 10078, 0 } };
	protected List<L2Spawn>			_teleportCubeSpawn			= new FastList<L2Spawn>();
	protected List<L2NpcInstance>	_teleportCube				= new FastList<L2NpcInstance>();

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
		_state = new GrandBossState(29020);
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
			template1 = NpcTable.getInstance().getTemplate(_StatueofBaiumId);
			_StatueofBaiumSpawn = new L2Spawn(template1);
			_StatueofBaiumSpawn.setAmount(1);
			_StatueofBaiumSpawn.setLocx(_StatueofBaiumLocation[0]);
			_StatueofBaiumSpawn.setLocy(_StatueofBaiumLocation[1]);
			_StatueofBaiumSpawn.setLocz(_StatueofBaiumLocation[2]);
			_StatueofBaiumSpawn.setHeading(_StatueofBaiumLocation[3]);
			_StatueofBaiumSpawn.setRespawnDelay(Config.FWB_ACTIVITYTIMEOFBAIUM * 2);
			_StatueofBaiumSpawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(_StatueofBaiumSpawn, false);

			// Baium.
			template1 = NpcTable.getInstance().getTemplate(29020);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(Config.FWB_ACTIVITYTIMEOFBAIUM * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29020, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of arcangels.
		try
		{
			L2NpcTemplate angel = NpcTable.getInstance().getTemplate(29021);
			L2Spawn spawnDat;
			_angelSpawn.clear();
			_angelSpawn1.clear();
			_angelSpawn2.clear();

			// 5 in 10 comes.
			for (int i = 0; i < 10; i = i + 2)
			{
				spawnDat = new L2Spawn(angel);
				spawnDat.setAmount(1);
				spawnDat.setLocx(_angelLocation[i][0]);
				spawnDat.setLocy(_angelLocation[i][1]);
				spawnDat.setLocz(_angelLocation[i][2]);
				spawnDat.setHeading(_angelLocation[i][3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_angelSpawn1.add(spawnDat);
			}
			_angelSpawn.put(0, _angelSpawn1);

			for (int i = 1; i < 10; i = i + 2)
			{
				spawnDat = new L2Spawn(angel);
				spawnDat.setAmount(1);
				spawnDat.setLocx(_angelLocation[i][0]);
				spawnDat.setLocy(_angelLocation[i][1]);
				spawnDat.setLocz(_angelLocation[i][2]);
				spawnDat.setHeading(_angelLocation[i][3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_angelSpawn2.add(spawnDat);
			}
			_angelSpawn.put(1, _angelSpawn1);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		_log.info("BaiumManager : State of Baium is " + _state.getState() + ".");
		if (_state.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
			_StatueofBaiumSpawn.doSpawn();
		else if (_state.getState().equals(GrandBossState.StateEnum.ALIVE))
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
			_StatueofBaiumSpawn.doSpawn();
		}
		else if (_state.getState().equals(GrandBossState.StateEnum.INTERVAL) || _state.getState().equals(GrandBossState.StateEnum.DEAD))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		_log.info("BaiumManager : Next spawn date of Baium is " + dt + ".");
		_log.info("BaiumManager : Init BaiumManager.");
	}

	// Arcangel advent.
	protected synchronized void adventArcAngel()
	{
		int i = Rnd.get(2);
		for (L2Spawn spawn : (FastList<L2Spawn>) _angelSpawn.get(i))
		{
			_angels.add(spawn.doSpawn());
		}

		// set invulnerable.
		for (L2NpcInstance angel : _angels)
		{
			angel.setIsInvul(true); // arcangel is invulnerable.
		}
	}

	// Arcangel ascension.
	public void ascensionArcAngel()
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
		L2Spawn baiumSpawn = _monsterSpawn.get(29020);
		baiumSpawn.setLocx(_npcBaium.getX());
		baiumSpawn.setLocy(_npcBaium.getY());
		baiumSpawn.setLocz(_npcBaium.getZ());
		baiumSpawn.setHeading(_npcBaium.getHeading());

		//delete statue.  
		_npcBaium.deleteMe();

		L2GrandBossInstance baium = (L2GrandBossInstance) baiumSpawn.doSpawn();
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

		Earthquake eq = new Earthquake(baium.getX(), baium.getY(), baium.getZ(), 30, 10);
		baium.broadcastPacket(eq);

		SocialAction sa = new SocialAction(baium.getObjectId(), 2);
		baium.broadcastPacket(sa);

		_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 3), 15000);

		_socialTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 1), 25000);

		_killPcTask = ThreadPoolManager.getInstance().scheduleGeneral(new KillPc(target, baium), 26000);

		_callAngelTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallArcAngel(), 35000);

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

		baium = null;
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
		ascensionArcAngel();
		for (L2NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// delete teleport cube.
		for (L2NpcInstance cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();

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
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
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
			_StatueofBaiumSpawn.doSpawn();
		}
	}

	// setting teleport cube spawn task.
	public void setCubeSpawn()
	{
		_state.setState(GrandBossState.StateEnum.DEAD);
		_state.update();

		ascensionArcAngel();

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
	private class CallArcAngel implements Runnable
	{
		public void run()
		{
			adventArcAngel();
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
			if (_target != null)
				_target.reduceCurrentHp(100000 + Rnd.get(_target.getMaxHp() / 2, _target.getMaxHp()), _boss);
		}
	}

	// Baium sleeps if never attacked for 30 minutes.
	public void sleepBaium()
	{
		setUnspawn();
		_state.setState(GrandBossState.StateEnum.NOTSPAWN);
		_state.update();

		// statue of Baium respawn.
		_StatueofBaiumSpawn.doSpawn();
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
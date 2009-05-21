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
package net.sf.l2j.gameserver.instancemanager.grandbosses;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Entity;
import net.sf.l2j.gameserver.model.entity.GrandBossState;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This class ...
 * Management for fight with baylor.
 * @version $Revision: $ $Date: $
 * @author  Umbrella Apocalipce + HanWik
 */
public class BaylorManager extends Entity
{
	private final static Log		_log						= LogFactory.getLog(BaylorManager.class.getName());
	private static BaylorManager	_instance					= new BaylorManager();

	// teleport cube location.
	private final int				_baylorCubeLocation[][]		=
																{
																{ 153569, 142075, -12732, 0 } };
	protected List<L2Spawn>			_baylorCubeSpawn			= new FastList<L2Spawn>();
	protected List<L2NpcInstance>	_baylorCube					= new FastList<L2NpcInstance>();

	// list of players in Baylor's lair.
	protected List<L2PcInstance>	_playersInBaylorLair		= new FastList<L2PcInstance>();

	// spawn data of monsters
	protected L2Spawn				_crystalineSpawn1;																// Crystaline1
	protected L2Spawn				_crystalineSpawn2;																// Crystaline2
	protected L2Spawn				_crystalineSpawn3;																// Crystaline3
	protected L2Spawn				_crystalineSpawn4;																// Crystaline4
	protected L2Spawn				_crystalineSpawn5;																// Crystaline5
	protected L2Spawn				_crystalineSpawn6;																// Crystaline6
	protected L2Spawn				_crystalineSpawn7;																// Crystaline7
	protected L2Spawn				_crystalineSpawn8;																// Crystaline8
	protected L2Spawn				_baylorSapwn;																	// Baylor

	// Instance of monsters
	protected L2NpcInstance			_crystaline1;																	// Crystaline1
	protected L2NpcInstance			_crystaline2;																	// Crystaline2
	protected L2NpcInstance			_crystaline3;																	// Crystaline3
	protected L2NpcInstance			_crystaline4;																	// Crystaline4
	protected L2NpcInstance			_crystaline5;																	// Crystaline5
	protected L2NpcInstance			_crystaline6;																	// Crystaline6
	protected L2NpcInstance			_crystaline7;																	// Crystaline7
	protected L2NpcInstance			_crystaline8;																	// Crystaline8
	protected L2NpcInstance			_baylor;																		// Baylor

	// Tasks
	protected ScheduledFuture<?>	_cubeSpawnTask				= null;
	protected ScheduledFuture<?>	_baylorSpawnTask			= null;
	protected ScheduledFuture<?>	_intervalEndTask			= null;
	protected ScheduledFuture<?>	_activityTimeEndTask0		= null;
	protected ScheduledFuture<?>	_onPartyAnnihilatedTask		= null;
	protected ScheduledFuture<?>	_socialTask					= null;
	protected ScheduledFuture<?>	_socialTask1				= null;
	protected ScheduledFuture<?>	_socialTask2				= null;
	protected ScheduledFuture<?>	_socialTask3				= null;
	protected ScheduledFuture<?>	_socialTask4				= null;
	protected ScheduledFuture<?>	_socialTask5				= null;
	protected ScheduledFuture<?>	_socialTask6				= null;
	protected ScheduledFuture<?>	_socialTask7				= null;
	protected ScheduledFuture<?>	_socialTask8				= null;

	// State of baylor's lair.
	protected GrandBossState		_state						= new GrandBossState(29099);
	protected boolean				_isAlreadyEnteredOtherParty	= false;

	protected String				_questName;

	public static BaylorManager getInstance()
	{
		if (_instance == null)
			_instance = new BaylorManager();

		return _instance;
	}

	// init.
	public void init()
	{
		// init state.
		_playersInBaylorLair.clear();
		_isAlreadyEnteredOtherParty = false;
		_questName = "baylor";

		// setting spawn data of monsters.
		try
		{
			L2NpcTemplate template1;

			// Crystaline1
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn1 = new L2Spawn(template1);
			_crystalineSpawn1.setLocx(154404);
			_crystalineSpawn1.setLocy(140596);
			_crystalineSpawn1.setLocz(-12711);
			_crystalineSpawn1.setHeading(44732);
			_crystalineSpawn1.setAmount(1);
			_crystalineSpawn1.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn1, false);

			// Crystaline2
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn2 = new L2Spawn(template1);
			_crystalineSpawn2.setLocx(153574);
			_crystalineSpawn2.setLocy(140402);
			_crystalineSpawn2.setLocz(-12711);
			_crystalineSpawn2.setHeading(44732);
			_crystalineSpawn2.setAmount(1);
			_crystalineSpawn2.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn2, false);

			// Crystaline3
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn3 = new L2Spawn(template1);
			_crystalineSpawn3.setLocx(152105);
			_crystalineSpawn3.setLocy(141230);
			_crystalineSpawn3.setLocz(-12711);
			_crystalineSpawn3.setHeading(44732);
			_crystalineSpawn3.setAmount(1);
			_crystalineSpawn3.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn3, false);

			// Crystaline4
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn4 = new L2Spawn(template1);
			_crystalineSpawn4.setLocx(151877);
			_crystalineSpawn4.setLocy(142095);
			_crystalineSpawn4.setLocz(-12711);
			_crystalineSpawn4.setHeading(44732);
			_crystalineSpawn4.setAmount(1);
			_crystalineSpawn4.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn4, false);

			// Crystaline5
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn5 = new L2Spawn(template1);
			_crystalineSpawn5.setLocx(152109);
			_crystalineSpawn5.setLocy(142920);
			_crystalineSpawn5.setLocz(-12711);
			_crystalineSpawn5.setHeading(44732);
			_crystalineSpawn5.setAmount(1);
			_crystalineSpawn5.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn5, false);

			// Crystaline6
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn6 = new L2Spawn(template1);
			_crystalineSpawn6.setLocx(152730);
			_crystalineSpawn6.setLocy(143555);
			_crystalineSpawn6.setLocz(-12711);
			_crystalineSpawn6.setHeading(44732);
			_crystalineSpawn6.setAmount(1);
			_crystalineSpawn6.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn6, false);

			// Crystaline7
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn7 = new L2Spawn(template1);
			_crystalineSpawn7.setLocx(154439);
			_crystalineSpawn7.setLocy(143538);
			_crystalineSpawn7.setLocz(-12711);
			_crystalineSpawn7.setHeading(44732);
			_crystalineSpawn7.setAmount(1);
			_crystalineSpawn7.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn7, false);

			// Crystaline8
			template1 = NpcTable.getInstance().getTemplate(29100); //Crystaline
			_crystalineSpawn8 = new L2Spawn(template1);
			_crystalineSpawn8.setLocx(155246);
			_crystalineSpawn8.setLocy(142068);
			_crystalineSpawn8.setLocz(-12711);
			_crystalineSpawn8.setHeading(44732);
			_crystalineSpawn8.setAmount(1);
			_crystalineSpawn8.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2);
			SpawnTable.getInstance().addNewSpawn(_crystalineSpawn8, false);

			// Baylor
			template1 = NpcTable.getInstance().getTemplate(29099); //Baylor
			_baylorSapwn = new L2Spawn(template1);
			_baylorSapwn.setLocx(153569);
			_baylorSapwn.setLocy(142075);
			_baylorSapwn.setLocz(-12732);
			_baylorSapwn.setHeading(59864);
			_baylorSapwn.setAmount(1);
			_baylorSapwn.setRespawnDelay(Config.FWBA_ACTIVITYTIMEOFMOBS * 2 + 30);
			SpawnTable.getInstance().addNewSpawn(_baylorSapwn, false);

		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of teleporte cube.
		try
		{
			L2NpcTemplate cube = NpcTable.getInstance().getTemplate(32273);
			L2Spawn spawnDat;

			for (int[] element : _baylorCubeLocation)
			{
				spawnDat = new L2Spawn(cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_baylorCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		_log.info("BaylorManager : State of Baylor is " + _state.getState() + ".");
		if (!_state.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		_log.info("BaylorManager : Next spawn date of Baylor is " + dt + ".");
		_log.info("BaylorManager : Init BaylorManager.");
	}

	// return Baylor state.  
	public GrandBossState.StateEnum getState()
	{
		return _state.getState();
	}

	// getting list of players in baylor's lair.
	public List<L2PcInstance> getPlayersInLair()
	{
		return _playersInBaylorLair;
	}

	// whether it is permitted to enter the baylor's lair is confirmed. 
	public int canIntoBaylorLair(L2PcInstance pc)
	{
		if ((Config.FWBA_ENABLESINGLEPLAYER == false) && (pc.getParty() == null))
			return 4;
		else if (_isAlreadyEnteredOtherParty)
			return 2;
		else if (_state.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
			return 0;
		else if (_state.getState().equals(GrandBossState.StateEnum.ALIVE) || _state.getState().equals(GrandBossState.StateEnum.DEAD))
			return 1;
		else if (_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
			return 3;
		else
			return 0;

	}

	// set baylor spawn task.
	public void setBaylorSpawnTask(int NpcId)
	{
		if (_baylorSpawnTask == null)
		{
			switch (NpcId)
			{
			case 29100:
				_baylorSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BaylorSpawn(NpcId), 20000);
				break;
			case 29099:
				_baylorSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BaylorSpawn(NpcId), 50000);
				break;
			default:
				break;
			}
		}
	}

	// add player to list of players in baylor's lair.
	public void addPlayerToBaylorLair(L2PcInstance pc)
	{
		if (!_playersInBaylorLair.contains(pc))
			_playersInBaylorLair.add(pc);
	}

	// teleporting player to baylor's lair.
	public void entryToBaylorLair(L2PcInstance pc)
	{
		int driftx;
		int drifty;

		if (canIntoBaylorLair(pc) != 0)
		{
			pc.sendMessage("Entrance was refused because it did not satisfy it.");
			_isAlreadyEnteredOtherParty = false;
			return;
		}

		if (pc.getParty() == null)
		{
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			pc.teleToLocation(153569 + driftx, 142075 + drifty, -12732);
			addPlayerToBaylorLair(pc);
		}
		else
		{
			List<L2PcInstance> members = new FastList<L2PcInstance>(); // list of member of teleport candidate.
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
				// teleporting it within alive and the range of recognition of the leader of the party. 
				if (!mem.isDead() && Util.checkIfInRange(700, pc, mem, true))
				{
					members.add(mem);
				}
			}
			for (L2PcInstance mem : members)
			{
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(153569 + driftx, 142075 + drifty, -12732);
				addPlayerToBaylorLair(mem);
			}
		}
		_isAlreadyEnteredOtherParty = true;
	}

	// whether the party was annihilated is confirmed. 
	public void checkAnnihilated(L2PcInstance pc)
	{
		// It is a teleport later 5 seconds to the port when annihilating.
		if (isPartyAnnihilated(pc))
		{
			_onPartyAnnihilatedTask = ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(pc), 5000);
		}
	}

	// whether the party was annihilated is confirmed.
	public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
	{
		if (pc.getParty() != null)
		{
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
				if (!mem.isDead() && checkIfInZone(pc))
				{
					return false;
				}
			}
			return true;
		}
		return true;
	}

	// when annihilating or limit of time coming, the compulsion movement players from the baylor's lair.
	public void banishesPlayers()
	{
		for (L2PcInstance pc : _playersInBaylorLair)
		{
			if (pc.getQuestState(_questName) != null)
				pc.getQuestState(_questName).exitQuest(true);
			if (checkIfInZone(pc))
			{
				int driftX = Rnd.get(-80, 80);
				int driftY = Rnd.get(-80, 80);
				pc.teleToLocation(10468 + driftX, -24569 + driftY, -3650);
			}
		}
		_playersInBaylorLair.clear();
		_isAlreadyEnteredOtherParty = false;
	}

	// clean up baylor's lair.
	public void setUnspawn()
	{
		// eliminate players.
		banishesPlayers();

		// delete teleport cube.
		for (L2NpcInstance cube : _baylorCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_baylorCube.clear();

		// not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_baylorSpawnTask != null)
		{
			_baylorSpawnTask.cancel(true);
			_baylorSpawnTask = null;
		}
		if (_intervalEndTask != null)
		{
			_intervalEndTask.cancel(true);
			_intervalEndTask = null;
		}
		if (_activityTimeEndTask0 != null)
		{
			_activityTimeEndTask0.cancel(true);
			_activityTimeEndTask0 = null;
		}

		// init state of baylor's lair.
		_crystaline1 = null;
		_crystaline2 = null;
		_crystaline3 = null;
		_crystaline4 = null;
		_crystaline5 = null;
		_crystaline6 = null;
		_crystaline7 = null;
		_crystaline8 = null;
		_baylor = null;

		// interval begin.
		setIntervalEndTask();
	}

	// spawn teleport cube.
	public void spawnCube()
	{
		for (L2Spawn spawnDat : _baylorCubeSpawn)
		{
			_baylorCube.add(spawnDat.doSpawn());
		}
	}

	// task of teleport cube spawn.
	public void setCubeSpawn()
	{
		_state.setState(GrandBossState.StateEnum.DEAD);
		_state.update();

		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);

	}

	// task of interval of baylor spawn.
	public void setIntervalEndTask()
	{
		if (!_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
		{
			_state
					.setRespawnDate(Rnd.get(Config.FWBA_FIXINTERVALOFBAYLORSPAWN, Config.FWBA_FIXINTERVALOFBAYLORSPAWN
							+ Config.FWBA_RANDOMINTERVALOFBAYLORSPAWN));
			_state.setState(GrandBossState.StateEnum.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	// update knownlist.
	protected void updateKnownList(L2NpcInstance boss)
	{
		boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _playersInBaylorLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
	}

	// spawn monster.
	private class BaylorSpawn implements Runnable
	{
		int				_NpcId;
		L2CharPosition	_pos	= new L2CharPosition(153569, 142075, -12711, 44732);

		public BaylorSpawn(int NpcId)
		{
			_NpcId = NpcId;
		}

		public void run()
		{
			switch (_NpcId)
			{
			case 29100:
				_crystaline1 = _crystalineSpawn1.doSpawn();
				_crystaline2 = _crystalineSpawn2.doSpawn();
				_crystaline3 = _crystalineSpawn3.doSpawn();
				_crystaline4 = _crystalineSpawn4.doSpawn();
				_crystaline5 = _crystalineSpawn5.doSpawn();
				_crystaline6 = _crystalineSpawn6.doSpawn();
				_crystaline7 = _crystalineSpawn7.doSpawn();
				_crystaline8 = _crystalineSpawn8.doSpawn();

				DoorTable.getInstance().getDoor(24220001).openMe();
				DoorTable.getInstance().getDoor(24220002).openMe();
				DoorTable.getInstance().getDoor(24220003).openMe();
				DoorTable.getInstance().getDoor(24220004).openMe();
				DoorTable.getInstance().getDoor(24220005).openMe();
				DoorTable.getInstance().getDoor(24220006).openMe();
				DoorTable.getInstance().getDoor(24220007).openMe();
				DoorTable.getInstance().getDoor(24220008).openMe();
				DoorTable.getInstance().getDoor(24220009).openMe();
				DoorTable.getInstance().getDoor(24220010).openMe();
				DoorTable.getInstance().getDoor(24220011).openMe();
				DoorTable.getInstance().getDoor(24220012).openMe();
				DoorTable.getInstance().getDoor(24220013).openMe();
				DoorTable.getInstance().getDoor(24220014).openMe();
				DoorTable.getInstance().getDoor(24220015).openMe();
				DoorTable.getInstance().getDoor(24220016).openMe();
				DoorTable.getInstance().getDoor(24220017).openMe();
				DoorTable.getInstance().getDoor(24220018).openMe();
				DoorTable.getInstance().getDoor(24220019).openMe();
				DoorTable.getInstance().getDoor(24220020).openMe();
				DoorTable.getInstance().getDoor(24220021).openMe();
				DoorTable.getInstance().getDoor(24220022).openMe();
				DoorTable.getInstance().getDoor(24220024).openMe();
				DoorTable.getInstance().getDoor(24220025).openMe();
				DoorTable.getInstance().getDoor(24220026).openMe();

				_crystaline1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
				_crystaline8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);

				_socialTask1 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline1, 2), 10000);
				_socialTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline2, 2), 10000);
				_socialTask3 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline3, 2), 10000);
				_socialTask4 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline4, 2), 10000);
				_socialTask5 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline5, 2), 10000);
				_socialTask6 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline6, 2), 10000);
				_socialTask7 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline7, 2), 10000);
				_socialTask8 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_crystaline8, 2), 10000);

			case 29099:
				_baylor = _baylorSapwn.doSpawn();

				_state.setRespawnDate(Rnd.get(Config.FWBA_FIXINTERVALOFBAYLORSPAWN, Config.FWBA_FIXINTERVALOFBAYLORSPAWN
						+ Config.FWBA_RANDOMINTERVALOFBAYLORSPAWN)
						+ Config.FWBA_ACTIVITYTIMEOFMOBS);
				_state.setState(GrandBossState.StateEnum.ALIVE);
				_state.update();

				if (_socialTask != null)
				{
					_socialTask.cancel(true);
					_socialTask = null;
				}
				_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Social(_baylor, 1), 500);
				if (_activityTimeEndTask0 != null)
				{
					_activityTimeEndTask0.cancel(true);
					_activityTimeEndTask0 = null;
				}
				_activityTimeEndTask0 = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(_baylor), Config.FWBA_ACTIVITYTIMEOFMOBS);

				_crystalineSpawn1.stopRespawn();
				_crystalineSpawn2.stopRespawn();
				_crystalineSpawn3.stopRespawn();
				_crystalineSpawn4.stopRespawn();
				_crystalineSpawn5.stopRespawn();
				_crystalineSpawn6.stopRespawn();
				_crystalineSpawn7.stopRespawn();
				_crystalineSpawn8.stopRespawn();

				break;
			default:
				break;
			}

			if (_baylorSpawnTask != null)
			{
				_baylorSpawnTask.cancel(true);
				_baylorSpawnTask = null;
			}
		}
	}

	// spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		public CubeSpawn()
		{
		}

		public void run()
		{
			spawnCube();
		}
	}

	// limit of time coming.
	private class ActivityTimeEnd implements Runnable
	{
		L2NpcInstance	_mob;

		public ActivityTimeEnd(L2NpcInstance npc)
		{
			_mob = npc;
		}

		public void run()
		{
			if (!_mob.isDead())
			{
				_mob.deleteMe();
				_mob.getSpawn().stopRespawn();
				_mob = null;
			}
			// clean up baylor's lair.
			setUnspawn();
		}
	}

	// interval end.
	private class IntervalEnd implements Runnable
	{
		public IntervalEnd()
		{
		}

		public void run()
		{
			_playersInBaylorLair.clear();
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
		}
	}

	// when annihilating or limit of time coming, the compulsion movement players from the baylor's lair.
	private class OnPartyAnnihilatedTask implements Runnable
	{
		L2PcInstance	_player;

		public OnPartyAnnihilatedTask(L2PcInstance player)
		{
			_player = player;
		}

		public void run()
		{
			setUnspawn();
		}
	}

	// social.
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
			updateKnownList(_npc);

			SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
			_npc.broadcastPacket(sa);
		}
	}
}

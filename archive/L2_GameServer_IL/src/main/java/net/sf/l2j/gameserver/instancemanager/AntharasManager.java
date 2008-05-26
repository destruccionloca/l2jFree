/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.sf.l2j.gameserver.instancemanager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.GrandBossState;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This class ...
 * control for sequence of figth with Antharas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class AntharasManager
{
    private final static Log _log = LogFactory.getLog(AntharasManager.class.getName());
    private static AntharasManager _instance = new AntharasManager();

    // location of teleport cube.
    private final int _teleportCubeId = 31859;
    private final int _teleportCubeLocation[][] =
    	{
    		{177615, 114941, -7709,0}
    	};
    protected List<L2Spawn> _teleportCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _teleportCube = new FastList<L2NpcInstance>();

    // list of intruders.
    protected List<L2PcInstance> _PlayersInLair = new FastList<L2PcInstance>();

    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _monsterspawn = new FastMap<Integer,L2Spawn>();

    // instance of monsters.
    protected List<L2NpcInstance> _monsters = new FastList<L2NpcInstance>();
    
    // tasks.
    protected Future _cubeSpawnTask = null;
    protected Future _monsterSpawnTask = null;
    protected Future _intervalEndTask = null;
    protected Future _activityTimeEndTask = null;
    protected Future _onPlayersAnnihilatedTask = null;
    protected Future _socialTask = null;
    protected Future _mobiliseTask = null;
    protected Future _behemothSpawnTask = null;
    protected Future _bomberSpawnTask = null;
    protected Future _selfDestructionTask = null;
    protected Future _moveAtRandomTask = null;
    protected Future _MovieTsak = null;
    
    // status in lair.
    protected GrandBossState _State = new GrandBossState(29019);
    protected IZone  _zone;
    protected String _zoneName;
    protected String _questName;
    
    // location of banishment
    private final int _banishmentLocation[][] =
    	{
    		{79959, 151774, -3532},
    		{81398, 148055, -3468},
    		{82286, 149113, -3468},
    		{84264, 147427, -3404}
		};
    
    public AntharasManager()
    {
    }

    public static AntharasManager getInstance()
    {
        if (_instance == null) _instance = new AntharasManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// initialize status in lair.
    	_PlayersInLair.clear();
        _zoneName = "Lair of Antharas";
    	_questName = "antharas";

        // setting spawn data of monsters.
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;
            
            // old Antharas.
            template1 = NpcTable.getInstance().getTemplate(29019);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _monsterspawn.put(29019, tempSpawn);
            
            // weak Antharas.
            template1 = NpcTable.getInstance().getTemplate(29066);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _monsterspawn.put(29066, tempSpawn);
            
            // normal Antharas.
            template1 = NpcTable.getInstance().getTemplate(29067);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _monsterspawn.put(29067, tempSpawn);
            
            // strong Antharas.
            template1 = NpcTable.getInstance().getTemplate(29068);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _monsterspawn.put(29068, tempSpawn);
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
            for (int[] element : _teleportCubeLocation) {
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
        
        _log.info("AntharasManager : State of Antharas is " + _State.getState() + ".");  
        if (!_State.getState().equals(GrandBossState.StateEnum.NOTSPAWN))  
                setInetrvalEndTask();  
          
                Date dt = new Date(_State.getRespawnDate());  
        _log.info("AntharasManager : Next spawn date of Antharas is " + dt + ".");  
        _log.info("AntharasManager : Init AntharasManager.");  
    }  

    // return Antaras state.  
    public GrandBossState.StateEnum getState()  
    {  
        return _State.getState();  
    } 


    // return list of intruders.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInLair;
	}
    
    public boolean checkIfInZone(L2PcInstance pc)
    {
    	if ( _zone == null )
    		_zone = ZoneManager.getInstance().getZone(ZoneType.BossDungeon, _zoneName );
    	return _zone.checkIfInZone(pc);
    }
    
    // Whether it lairs is confirmed. 
    public boolean isEnableEnterToLair()
    {
    	if(_State.getState().equals(GrandBossState.StateEnum.NOTSPAWN)) 
    		return true; 
    	else 
    		return false; 
    }

    // update list of intruders.
    public void addPlayerToLair(L2PcInstance pc)
    {
        if (!_PlayersInLair.contains(pc)) _PlayersInLair.add(pc);
    }
    
    // Whether the players was annihilated is confirmed. 
    public synchronized boolean isPlayersAnnihilated()
    {
    	for (L2PcInstance pc : _PlayersInLair)
		{
			// player is must be alive and stay inside of lair.
			if (!pc.isDead()
					&& checkIfInZone(pc))
			{
				return false;
			}
		}
		return true;
    }

    // banishes players from lair.
    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _PlayersInLair)
    	{
    		if(pc.getQuestState(_questName) != null) pc.getQuestState(_questName).exitQuest(true);
    		if(checkIfInZone(pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		int loc = Rnd.get(4);
        		pc.teleToLocation(_banishmentLocation[loc][0] + driftX,_banishmentLocation[loc][1] + driftY,_banishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
    }
    
    // do spawn teleport cube.
    public void spawnCube()
    {
		if(_behemothSpawnTask != null)
		{
			_behemothSpawnTask.cancel(true);
			_behemothSpawnTask = null;
		}
		if(_bomberSpawnTask != null)
		{
			_bomberSpawnTask.cancel(true);
			_bomberSpawnTask = null;
		}
		if(_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
    }
    
	// When the party is annihilated, they are banished.
    public void checkAnnihilated()
    {
    	if(isPlayersAnnihilated())
    	{
    		_onPlayersAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleEffect(new OnPlayersAnnihilatedTask(),5000);    			
    	}
    }

	// When the party is annihilated, they are banished.
	private class OnPlayersAnnihilatedTask implements Runnable
	{
		public OnPlayersAnnihilatedTask()
		{
		}
		
		public void run()
		{
		    // banishes players from lair.
			banishesPlayers();
			
            // clean up task.
            if(_onPlayersAnnihilatedTask != null)
            {
            	_onPlayersAnnihilatedTask.cancel(true);
            	_onPlayersAnnihilatedTask = null;
            }
		}
	}

    // setting Antharas spawn task.
    public void setAntharasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_PlayersInLair.size() >= 1) return;

    	if (_monsterSpawnTask == null)
        {
    		_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(1,null),Config.FWA_APPTIMEOFANTHARAS);
        }
    }
    
    // do spawn Antharas.
    private class AntharasSpawn implements Runnable
    {
    	int _distance = 6502500;
    	int _taskId = 0;
		L2BossInstance _antharas = null;

		AntharasSpawn(int taskId,L2BossInstance antharas)
		{
			_taskId = taskId;
			_antharas = antharas;
		}

		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;
			SocialAction sa = null;

			switch (_taskId)
			{
				case 1: // spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					if (Config.FWA_OLDANTHARAS)
						npcId = 29019; // old
					else if (_PlayersInLair.size() <= Config.FWA_LIMITOFWEAK)
						npcId = 29066; // weak
					else if (_PlayersInLair.size() >= Config.FWA_LIMITOFNORMAL)
						npcId = 29068; // strong
					else
						npcId = 29067; // normal
	
					// do spawn.
					antharasSpawn = _monsterspawn.get(npcId);
					_antharas = (L2BossInstance) antharasSpawn.doSpawn();
					_monsters.add(_antharas);
					_antharas.setIsImobilised(true);
					_antharas.setIsInSocialAction(true);
					
					_State.setRespawnDate(Rnd.get(Config.FWA_FIXINTERVALOFANTHARAS,Config.FWA_FIXINTERVALOFANTHARAS + Config.FWA_RANDOMINTERVALOFANTHARAS) + Config.FWA_ACTIVITYTIMEOFANTHARAS);  
					_State.setState(GrandBossState.StateEnum.ALIVE);  
					_State.update();  

	
					// set KnownList
					updateKnownList(_antharas);
	
					// setting 1st time of minions spawn task.
					if (!Config.FWA_OLDANTHARAS)
					{
						int intervalOfBehemoth;
						int intervalOfBomber;
	
						// Interval of minions is decided by the number of players
						// that invaded the lair.
						if (_PlayersInLair.size() <= Config.FWA_LIMITOFWEAK) // weak
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONWEAK;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONWEAK;
						} else if (_PlayersInLair.size() >= Config.FWA_LIMITOFNORMAL) // strong
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONSTRONG;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONSTRONG;
						} else
						// normal
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONNORMAL;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONNORMAL;
						}
	
						// spawn Behemoth.
						_behemothSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new BehemothSpawn(intervalOfBehemoth),30000);
	
						// spawn Bomber.
						_bomberSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new BomberSpawn(intervalOfBomber),30000);
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(2,_antharas), 16);
	
					break;
	
				case 2:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, -19, 0, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(3,_antharas), 3000);
	
					break;
	
				case 3:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 1);
					_antharas.broadcastPacket(sa);
	
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, 0, 6000, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(4,_antharas), 10000);
	
					break;
	
				case 4:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 3800, 0, -3, 0, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(5,_antharas), 200);
	
					break;
	
				case 5:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 2);
					_antharas.broadcastPacket(sa);
	
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 22000, 11000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(6,_antharas), 10800);
	
					break;
	
				case 6:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 300, 2000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new AntharasSpawn(7,_antharas), 1900);
	
					break;
	
				case 7:
					_antharas.abortCast();		
					// reset camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						pc.leaveMovieMode();
					}

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(_antharas), 16);
	
					// move at random.
					if (Config.FWA_MOVEATRANDOM)
					{
						L2CharPosition pos = new L2CharPosition(Rnd.get(175000,	178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(new MoveAtRandom(_antharas, pos),
										32);
					}
	
					// set delete task.
					_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(),Config.FWA_ACTIVITYTIMEOFANTHARAS);
	
		            if(_socialTask != null)
		            {
		            	_socialTask.cancel(true);
		            	_socialTask = null;
		            }
					break;
			}
		}
	}

    // do spawn Behemoth.
    private class BehemothSpawn implements Runnable
    {
    	private int _interval;
    	
    	public BehemothSpawn(int interval)
    	{
    		_interval = interval;
    	}
    	
    	public void run()
    	{
            L2NpcTemplate template1;
            L2Spawn tempSpawn;

            try
            {
            	// set spawn.
                template1 = NpcTable.getInstance().getTemplate(29069);
                tempSpawn = new L2Spawn(template1);
                // allocates it at random in the lair of Antharas. 
                tempSpawn.setLocx(Rnd.get(175000, 179900));
                tempSpawn.setLocy(Rnd.get(112400, 116000));
                tempSpawn.setLocz(-7709);
                tempSpawn.setHeading(0);
                tempSpawn.setAmount(1);
                tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
            	_monsters.add(tempSpawn.doSpawn());

            }
            catch (Exception e)
            {
                _log.warn(e.getMessage());
            }
            
            if(_behemothSpawnTask != null)
            {
            	_behemothSpawnTask.cancel(true);
            	_behemothSpawnTask = null;
            }
            
            // repeat.
        	_behemothSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new BehemothSpawn(_interval),_interval);

    	}
    }
    
    // do spawn Bomber.
    private class BomberSpawn implements Runnable
    {
    	private int _interval;
    	
    	public BomberSpawn(int interval)
    	{
    		_interval = interval;
    	}

    	public void run()
    	{
    		int npcId = Rnd.get(29070, 29076);
            L2NpcTemplate template1;
            L2Spawn tempSpawn;
            L2NpcInstance bomber = null;
            
            try
            {
            	// set spawn.
                template1 = NpcTable.getInstance().getTemplate(npcId);
                tempSpawn = new L2Spawn(template1);
                // allocates it at random in the lair of Antharas. 
                tempSpawn.setLocx(Rnd.get(175000, 179900));
                tempSpawn.setLocy(Rnd.get(112400, 116000));
                tempSpawn.setLocz(-7709);
                tempSpawn.setHeading(0);
                tempSpawn.setAmount(1);
                tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
                bomber = tempSpawn.doSpawn();
            	_monsters.add(bomber);

            }
            catch (Exception e)
            {
                _log.warn(e.getMessage());
            }
            
            // set self destruction.
            if(bomber != null)
            {
                _selfDestructionTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new SelfDestructionOfBomber(bomber),1000);
            }
            
            if(_bomberSpawnTask != null)
            {
            	_bomberSpawnTask.cancel(true);
            	_bomberSpawnTask = null;
            }

            // repeat.
            _bomberSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new BomberSpawn(_interval),_interval);

    	}
    }

    // do self destruction.
    private class SelfDestructionOfBomber implements Runnable
    {
    	L2NpcInstance _bomber;
    	public SelfDestructionOfBomber(L2NpcInstance bomber)
    	{
    		_bomber = bomber;
    	}
    	
    	public void run()
    	{
    		L2Skill skill = null;
    		switch (_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}
    		
    		_bomber.doCast(skill);

    		if(_selfDestructionTask != null)
            {
    			_selfDestructionTask.cancel(true);
    			_selfDestructionTask = null;
            }
    	}
    }
    
    // at end of activitiy time.
    private class ActivityTimeEnd implements Runnable
    {
    	public ActivityTimeEnd()
    	{
    	}
    	
    	public void run()
    	{
    		setUnspawn();
    		
    		if(_activityTimeEndTask != null)
    		{
    			_activityTimeEndTask.cancel(true);
    			_activityTimeEndTask = null;
    		}
    	}
    }
    
    // clean Antharas's lair.
    public void setUnspawn()
	{
    	// eliminate players.
    	banishesPlayers();

    	// delete monsters.
    	for(L2NpcInstance mob : _monsters)
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
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(true);
			_intervalEndTask = null;
		}
		if(_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(true);
			_activityTimeEndTask = null;
		}
		if(_onPlayersAnnihilatedTask != null)
		{
			_onPlayersAnnihilatedTask.cancel(true);
			_onPlayersAnnihilatedTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if(_behemothSpawnTask != null)
		{
			_behemothSpawnTask.cancel(true);
			_behemothSpawnTask = null;
		}
		if(_bomberSpawnTask != null)
		{
			_bomberSpawnTask.cancel(true);
			_bomberSpawnTask = null;
		}
		if(_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}

		// interval begin.
		setInetrvalEndTask();
	}

    // start interval.
    public void setInetrvalEndTask()
    {
    	//init state of Antharas's lair.  
    	if (!_State.getState().equals(GrandBossState.StateEnum.INTERVAL))  
    	{  
    		_State.setRespawnDate(Rnd.get(Config.FWA_FIXINTERVALOFANTHARAS,Config.FWA_FIXINTERVALOFANTHARAS + Config.FWA_RANDOMINTERVALOFANTHARAS));  
    		_State.setState(GrandBossState.StateEnum.INTERVAL);  
    		_State.update();  
    	} 

    	_intervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(
            	new IntervalEnd(),_State.getInterval());
    }

    // at end of interval.
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}
    	
    	public void run()
    	{
    		_PlayersInLair.clear();  
    		_State.setState(GrandBossState.StateEnum.NOTSPAWN);  
    		_State.update();  

    		if(_intervalEndTask != null)
    		{
    			_intervalEndTask.cancel(true);
    			_intervalEndTask = null;
    		}
    	}
    }
    
    // setting teleport cube spawn task.
    public void setCubeSpawn()
    {
    	_State.setState(GrandBossState.StateEnum.DEAD);  
    	_State.update();  

    	_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new CubeSpawn(),10000); 

    }
    
    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _PlayersInLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
    }

    // do spawn teleport cube.
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
    
    // action is enabled the boss.
    private class SetMobilised implements Runnable
    {
        private L2BossInstance _boss;
        public SetMobilised(L2BossInstance boss)
        {
        	_boss = boss;
        }

        public void run()
        {
        	_boss.setIsImobilised(false);
        	_boss.setIsInSocialAction(false);
            
            // When it is possible to act, a social action is canceled.
            if (_socialTask != null)
            {
            	_socialTask.cancel(true);
                _socialTask = null;
            }
        }
    }
    
    // Move at random on after Antharas appears.
    private class MoveAtRandom implements Runnable
    {
    	private L2NpcInstance _npc;
    	L2CharPosition _pos;
    	
    	public MoveAtRandom(L2NpcInstance npc,L2CharPosition pos)
    	{
    		_npc = npc;
    		_pos = pos;
    	}
    	
    	public void run()
    	{
    		_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
    	}
    }
}

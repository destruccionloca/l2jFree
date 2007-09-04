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

/**
 @author L2J_JP SANDMAN
 **/

package net.sf.l2j.gameserver.instancemanager;

import java.util.concurrent.Future;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2CharPosition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This class ...
 * control for sequence of figth with Valakas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class ValakasManager
{
    private final static Log _log = LogFactory.getLog(ValakasManager.class.getName());
    private static ValakasManager _instance = new ValakasManager();

    // config
    // Interval time of Boss.
    protected int _intervalOfBoss;

    // Delay of appearance time of Boss.
    protected int _appTimeOfBoss;

    // Activity time of Boss.
    protected int _activityTimeOfBoss;

    // Capacity Of Lair Of Valakas.
    protected int _capacity;
    
    // Whether it moves at random after Valakas appears is decided.
    protected boolean _moveAtRandom = true;
    
    // location of teleport cube.
    private final int _teleportCubeId = 31759;
    private final int _teleportCubeLocation[][] =
    	{
    		{214880, -116144, -1644, 0},
    		{213696, -116592, -1644, 0},
    		{212112, -116688, -1644, 0},
    		{211184, -115472, -1664, 0},
    		{210336, -114592, -1644, 0},
    		{211360, -113904, -1644, 0},
    		{213152, -112352, -1644, 0},
    		{214032, -113232, -1644, 0},
    		{214752, -114592, -1644, 0},
    		{209824, -115568, -1421, 0},
    		{210528, -112192, -1403, 0},
    		{213120, -111136, -1408, 0},
    		{215184, -111504, -1392, 0},
    		{215456, -117328, -1392, 0},
    		{213200, -118160, -1424, 0}
    	};
    protected List<L2Spawn> _teleportCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _teleportCube = new FastList<L2NpcInstance>();

    // list of intruders.
    protected List<L2PcInstance> _playersInLair = new FastList<L2PcInstance>();

    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _monsterSpawn = new FastMap<Integer,L2Spawn>();

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
    protected Future _moveAtRandomTask = null;
    
    // status in lair.
    protected boolean _isBossSpawned = false;
    protected boolean _isIntervalForNextSpawn = false;
    protected String _zoneType;
    protected String _questName;
    
    // location of banishment
    private final int _banishmentLocation[][] = 
    	{
    		{150604, -56283, -2980},
    		{144857, -56386, -2980},
    		{147696, -56845, -2780}
    	};
    
    public ValakasManager()
    {
    }

    public static ValakasManager getInstance()
    {
        if (_instance == null) _instance = new ValakasManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// read configuration.
    	_intervalOfBoss = Config.FWV_INTERVALOFVALAKAS;
    	_appTimeOfBoss = Config.FWV_APPTIMEOFVALAKAS;
    	_activityTimeOfBoss = Config.FWV_ACTIVITYTIMEOFVALAKAS;
    	_capacity = Config.FWV_CAPACITYOFLAIR;
    	_moveAtRandom = Config.FWV_MOVEATRANDOM;
    	
    	// initialize status in lair.
    	_isBossSpawned = false;
    	_isIntervalForNextSpawn = false;
    	_playersInLair.clear();
        _zoneType = "LairofValakas";
        _questName = "valakas";

        // setting spawn data of monsters.
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;
            
            // Valakas.
            template1 = NpcTable.getInstance().getTemplate(29028);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(213168);
            tempSpawn.setLocy(-114578);
            tempSpawn.setLocz(-1635);
            tempSpawn.setHeading(22106);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(_intervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _monsterSpawn.put(29028, tempSpawn);
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
            for(int i = 0;i < _teleportCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_teleportCubeLocation[i][0]);
                spawnDat.setLocy(_teleportCubeLocation[i][1]);
                spawnDat.setLocz(_teleportCubeLocation[i][2]);
                spawnDat.setHeading(_teleportCubeLocation[i][3]);
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
        
        _log.info("ValakasManager:Init ValakasManager.");
    }

    // return list of intruders.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _playersInLair;
	}
    
    // Whether it lairs is confirmed. 
    public boolean isEnableEnterToLair()
    {
    	if(_playersInLair.size() >= _capacity) return false;
    	
    	return(_isBossSpawned == false && _isIntervalForNextSpawn == false);
    }

    // update list of intruders.
    public void addPlayerToLair(L2PcInstance pc)
    {
        if (!_playersInLair.contains(pc)) _playersInLair.add(pc);
    }
    
    // Whether the players was annihilated is confirmed. 
    public synchronized boolean isPlayersAnnihilated()
    {
    	for (L2PcInstance pc : _playersInLair)
		{
			// player is must be alive and stay inside of lair.
			if (!pc.isDead() && ZoneManager.getInstance().checkIfInZone(_zoneType, pc))
			{
				return false;
			}
		}
		return true;
    }

    // banishes players from lair.
    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _playersInLair)
    	{
    		if(pc.getQuestState(_questName) != null) pc.getQuestState(_questName).exitQuest(true);
    		if(ZoneManager.getInstance().checkIfInZone(_zoneType, pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		int loc = Rnd.get(3);
        		pc.teleToLocation(_banishmentLocation[loc][0] + driftX,_banishmentLocation[loc][1] + driftY,_banishmentLocation[loc][2]);
    		}
    	}
    	_playersInLair.clear();
    }
    
    // do spawn teleport cube.
    public void spawnCube()
    {
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
    	_isIntervalForNextSpawn = true;
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

    // setting Valakas spawn task.
    public void setValakasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_playersInLair.size() >= 1) return;

    	if (_monsterSpawnTask == null)
        {
        	_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new ValakasSpawn(),_appTimeOfBoss);
        }
    }
    
    // do spawn Valakas.
    private class ValakasSpawn implements Runnable
    {
    	ValakasSpawn()
    	{
    	}
    	
    	public void run()
    	{
        	// do spawn.
        	L2Spawn valakasSpawn = _monsterSpawn.get(29028);
        	L2BossInstance valakas = (L2BossInstance)valakasSpawn.doSpawn();
        	_monsters.add(valakas);
        	
        	updateKnownList(valakas);
        	
        	// do social.
        	valakas.setIsInSocialAction(true);
            SocialAction sa = new SocialAction(valakas.getObjectId(), 3);
            valakas.broadcastPacket(sa);

            _socialTask = ThreadPoolManager.getInstance().scheduleEffect(new Social(valakas,2), 26000);

            _mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(valakas),41000);

            // move at random.
            if(_moveAtRandom)
            {
            	L2CharPosition pos = new L2CharPosition(Rnd.get(211080, 214909),Rnd.get(-115841, -112822),-1662,0);
            	_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new MoveAtRandom(valakas,pos),42000);
            }
            
            // set delete task.
            _activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(),_activityTimeOfBoss);
            
            valakas = null;
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
    
    // clean Valakas's lair.
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
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}

		// init state of Valakas's lair.
    	_isBossSpawned = false;
    	_isIntervalForNextSpawn = true;

		// interval begin.
		setInetrvalEndTask();
	}

    // start interval.
    public void setInetrvalEndTask()
    {
    	_intervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(new IntervalEnd(),_intervalOfBoss);
    }

    // at end of interval.
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}
    	
    	public void run()
    	{
    		_isIntervalForNextSpawn = false;
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
		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new CubeSpawn(),10000);
    }
    
    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _playersInLair)
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
    
    // do social.
    private class Social implements Runnable
    {
        private int _action;
        private L2NpcInstance _npc;

        public Social(L2NpcInstance npc,int actionId)
        {
        	_npc = npc;
            _action = actionId;
        }

        public void run()
        {
        	updateKnownList(_npc);

    		SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);

            if(_socialTask != null)
    		{
    			_socialTask.cancel(true);
    			_socialTask = null;
    		}
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
    
    // Move at random on after Valakas appears.
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

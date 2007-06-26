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
    protected int _IntervalOfBoss;

    // Delay of appearance time of Boss.
    protected int _AppTimeOfBoss;

    // Activity time of Boss.
    protected int _ActivityTimeOfBoss;

    // Capacity Of Lair Of Valakas.
    protected int _Capacity;
    
    // Whether it moves at random after Valakas appears is decided.
    protected boolean _MoveAtRandom = true;
    
    // location of teleport cube.
    private final int _TeleportCubeId = 31759;
    private final int _TeleportCubeLocation[][] =
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
    protected List<L2Spawn> _TeleportCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _TeleportCube = new FastList<L2NpcInstance>();

    // list of intruders.
    protected List<L2PcInstance> _PlayersInLair = new FastList<L2PcInstance>();

    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _MonsterSpawn = new FastMap<Integer,L2Spawn>();

    // instance of monsters.
    protected List<L2NpcInstance> _Monsters = new FastList<L2NpcInstance>();
    
    // tasks.
    protected Future _CubeSpawnTask = null;
    protected Future _MonsterSpawnTask = null;
    protected Future _IntervalEndTask = null;
    protected Future _ActivityTimeEndTask = null;
    protected Future _OnPlayersAnnihilatedTask = null;
    protected Future _SocialTask = null;
    protected Future _MobiliseTask = null;
    protected Future _MoveAtRandomTask = null;
    
    // status in lair.
    protected boolean _IsBossSpawned = false;
    protected boolean _IsIntervalForNextSpawn = false;
    protected String _ZoneType;
    protected String _QuestName;
    
    // location of banishment
    private final int _BanishmentLocation[][] = 
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
    	_IntervalOfBoss = Config.FWV_INTERVALOFVALAKAS;
    	_AppTimeOfBoss = Config.FWV_APPTIMEOFVALAKAS;
    	_ActivityTimeOfBoss = Config.FWV_ACTIVITYTIMEOFVALAKAS;
    	_Capacity = Config.FWV_CAPACITYOFLAIR;
    	_MoveAtRandom = Config.FWV_MOVEATRANDOM;
    	
    	// initialize status in lair.
    	_IsBossSpawned = false;
    	_IsIntervalForNextSpawn = false;
    	_PlayersInLair.clear();
        _ZoneType = "LairofValakas";
        _QuestName = "valakas";

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
            tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29028, tempSpawn);
        }
        catch (Exception e)
        {
            _log.warn(e.getMessage());
        }

        // setting spawn data of teleport cube.
        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_TeleportCubeId);
            L2Spawn spawnDat;
            for(int i = 0;i < _TeleportCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_TeleportCubeLocation[i][0]);
                spawnDat.setLocy(_TeleportCubeLocation[i][1]);
                spawnDat.setLocz(_TeleportCubeLocation[i][2]);
                spawnDat.setHeading(_TeleportCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _TeleportCubeSpawn.add(spawnDat);
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
		return _PlayersInLair;
	}
    
    // Whether it lairs is confirmed. 
    public boolean isEnableEnterToLair()
    {
    	if(_PlayersInLair.size() >= _Capacity) return false;
    	
    	if(_IsBossSpawned == false && _IsIntervalForNextSpawn == false)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
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
					&& ZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
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
    		if(pc.getQuestState(_QuestName) != null) pc.getQuestState(_QuestName).exitQuest(true);
    		if(ZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		int loc = Rnd.get(3);
        		pc.teleToLocation(_BanishmentLocation[loc][0] + driftX,_BanishmentLocation[loc][1] + driftY,_BanishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
    }
    
    // do spawn teleport cube.
    public void spawnCube()
    {
		for (L2Spawn spawnDat : _TeleportCubeSpawn)
		{
			_TeleportCube.add(spawnDat.doSpawn());
		}
    	_IsIntervalForNextSpawn = true;
    }
    
	// When the party is annihilated, they are banished.
    public void checkAnnihilated()
    {
    	if(isPlayersAnnihilated())
    	{
    		_OnPlayersAnnihilatedTask =
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
            if(_OnPlayersAnnihilatedTask != null)
            {
            	_OnPlayersAnnihilatedTask.cancel(true);
            	_OnPlayersAnnihilatedTask = null;
            }
		}
	}

    // setting Valakas spawn task.
    public void setValakasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_PlayersInLair.size() >= 1) return;

    	if (_MonsterSpawnTask == null)
        {
        	_MonsterSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new ValakasSpawn(),_AppTimeOfBoss);
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
        	L2Spawn valakasSpawn = _MonsterSpawn.get(29028);
        	L2BossInstance valakas = (L2BossInstance)valakasSpawn.doSpawn();
        	_Monsters.add(valakas);
        	
        	// do social.
        	valakas.setIsInSocialAction(true);
            SocialAction sa = new SocialAction(valakas.getObjectId(), 3);
            valakas.broadcastPacket(sa);

            _SocialTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new Social(valakas,2), 26000);

            _MobiliseTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(valakas),41000);

            // move at random.
            if(_MoveAtRandom)
            {
            	L2CharPosition pos = new L2CharPosition(Rnd.get(211080, 214909),Rnd.get(-115841, -112822),-1662,0);
            	_MoveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new MoveAtRandom(valakas,pos),42000);
            }
            
            // set delete task.
            _ActivityTimeEndTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(),_ActivityTimeOfBoss);
            
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
    		
    		if(_ActivityTimeEndTask != null)
    		{
    			_ActivityTimeEndTask.cancel(true);
    			_ActivityTimeEndTask = null;
    		}
    	}
    }
    
    // clean Valakas's lair.
    public void setUnspawn()
	{
    	// eliminate players.
    	banishesPlayers();

    	// delete monsters.
    	for(L2NpcInstance mob : _Monsters)
    	{
    		mob.getSpawn().stopRespawn();
    		mob.deleteMe();
    	}
    	_Monsters.clear();
    	
    	// delete teleport cube.
		for (L2NpcInstance cube : _TeleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_TeleportCube.clear();
		
		// not executed tasks is canceled.
		if(_CubeSpawnTask != null)
		{
			_CubeSpawnTask.cancel(true);
			_CubeSpawnTask = null;
		}
		if(_MonsterSpawnTask != null)
		{
			_MonsterSpawnTask.cancel(true);
			_MonsterSpawnTask = null;
		}
		if(_IntervalEndTask != null)
		{
			_IntervalEndTask.cancel(true);
			_IntervalEndTask = null;
		}
		if(_ActivityTimeEndTask != null)
		{
			_ActivityTimeEndTask.cancel(true);
			_ActivityTimeEndTask = null;
		}
		if(_OnPlayersAnnihilatedTask != null)
		{
			_OnPlayersAnnihilatedTask.cancel(true);
			_OnPlayersAnnihilatedTask = null;
		}
		if(_SocialTask != null)
		{
			_SocialTask.cancel(true);
			_SocialTask = null;
		}
		if(_MobiliseTask != null)
		{
			_MobiliseTask.cancel(true);
			_MobiliseTask = null;
		}
		if(_MoveAtRandomTask != null)
		{
			_MoveAtRandomTask.cancel(true);
			_MoveAtRandomTask = null;
		}

		// init state of Valakas's lair.
    	_IsBossSpawned = false;
    	_IsIntervalForNextSpawn = true;

		// interval begin.
		setInetrvalEndTask();
	}

    // start interval.
    public void setInetrvalEndTask()
    {
    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(
            	new IntervalEnd(),_IntervalOfBoss);
    }

    // at end of interval.
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}
    	
    	public void run()
    	{
    		_IsIntervalForNextSpawn = false;
    		if(_IntervalEndTask != null)
    		{
    			_IntervalEndTask.cancel(true);
    			_IntervalEndTask = null;
    		}
    	}
    }
    
    // setting teleport cube spawn task.
    public void setCubeSpawn()
    {
		_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            	new CubeSpawn(),10000);
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
        	_npc.getKnownList().getKnownPlayers().clear();
    		for (L2PcInstance pc : _PlayersInLair)
    		{
    			_npc.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
    		}

    		SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);

            if(_SocialTask != null)
    		{
    			_SocialTask.cancel(true);
    			_SocialTask = null;
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
            if (_SocialTask != null)
            {
            	_SocialTask.cancel(true);
                _SocialTask = null;
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

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
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.GrandBossState;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This class ...
 * control for sequence of figth with Baium.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class BaiumManager
{
    private final static Log _log = LogFactory.getLog(BaiumManager.class.getName());
    private static BaiumManager _instance = new BaiumManager();

    //location of Statue of Baium  
    private final int _StatueofBaiumId = 29025;  
    private final int _StatueofBaiumLocation[] = {116067,17484,10110,41740};  
    protected L2Spawn _StatueofBaiumSpawn = null; 


    // location of arcangels.
    private final int _angelLocation[][] = 
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
			{ 115245, 17558, 10076, 35536 }
		};
    protected List<L2Spawn> _angelSpawn1 = new FastList<L2Spawn>();
    protected List<L2Spawn> _angelSpawn2 = new FastList<L2Spawn>();
    protected Map<Integer,List> _angelSpawn = new FastMap<Integer,List>();
    List<L2NpcInstance> _angels = new FastList<L2NpcInstance>();

    // location of teleport cube.
    private final int _teleportCubeId = 29055;
    private final int _teleportCubeLocation[][] = { {115203,16620,10078,0} };
    protected List<L2Spawn> _teleportCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _teleportCube = new FastList<L2NpcInstance>();
    
    // list of intruders.
    protected List<L2PcInstance> _PlayersInLair = new FastList<L2PcInstance>();

    // instance of statue of Baium.
    protected L2NpcInstance _npcBaium;

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
    protected Future _socialTask2 = null;
    protected Future _recallPcTask = null;
    protected Future _killPcTask = null;
    protected Future _callAngelTask = null;

    // status in lair.
    protected GrandBossState _State = new GrandBossState(29020);
    protected IZone  _zone;
    protected String _zoneName;
    protected String _questName;

    // location of banishment
    private final int _banishmentLocation[][] =
    	{
    		{108784, 16000, -4928},
    		{113824, 10448, -5164},
    		{115488, 22096, -5168}
		};

    public BaiumManager()
    {
    }

    public static BaiumManager getInstance()
    {
        if (_instance == null) _instance = new BaiumManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// initialize status in lair.
    	_PlayersInLair.clear();
        _zoneName = "Lair of Baium";
        _questName = "baium";

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
        
        _log.info("BaiumManager : State of Baium is " + _State.getState() + ".");  
        if (_State.getState().equals(GrandBossState.StateEnum.NOTSPAWN))  
        	_StatueofBaiumSpawn.doSpawn();  
        else if (_State.getState().equals(GrandBossState.StateEnum.INTERVAL) || _State.getState().equals(GrandBossState.StateEnum.DEAD))  
        	setInetrvalEndTask();  

        Date dt = new Date(_State.getRespawnDate());  
        _log.info("BaiumManager : Next spawn date of Baium is " + dt + ".");  
        _log.info("BaiumManager : Init BaiumManager.");  
    }  

    // return Baium state.  
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
    
    // Arcangel advent.
    protected synchronized void adventArcAngel()
    {
    	int i = Rnd.get(2);
    	for(L2Spawn spawn : (FastList<L2Spawn>)_angelSpawn.get(i))
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

     // do spawn baium.
    public void spawnBaium(L2NpcInstance NpcBaium)
    {
        _npcBaium = NpcBaium;

        // get target from statue,to kill a player of make Baium awake.
        L2PcInstance target = (L2PcInstance)_npcBaium.getTarget();
        
        // do spawn.
        L2Spawn baiumSpawn = _monsterSpawn.get(29020);
        baiumSpawn.setLocx(_npcBaium.getX());
        baiumSpawn.setLocy(_npcBaium.getY());
        baiumSpawn.setLocz(_npcBaium.getZ());
        baiumSpawn.setHeading(_npcBaium.getHeading());
        
        //delete statue.  
        _npcBaium.deleteMe();  

        L2BossInstance baium = (L2BossInstance)baiumSpawn.doSpawn();
        _monsters.add(baium);

        _State.setRespawnDate(Rnd.get(Config.FWB_FIXINTERVALOFBAIUM,Config.FWB_FIXINTERVALOFBAIUM + Config.FWB_RANDOMINTERVALOFBAIUM)+ Config.FWB_ACTIVITYTIMEOFBAIUM);  
        _State.setState(GrandBossState.StateEnum.ALIVE);  
        _State.update();
        
        // stop respawn of statue.
        _npcBaium.getSpawn().stopRespawn();
        
        updateKnownList(baium);
    	
        // do social.
        baium.setIsImobilised(true);
        baium.setIsInSocialAction(true);

        Earthquake eq = new Earthquake(baium.getX(), baium.getY(), baium.getZ(), 30, 10);
        baium.broadcastPacket(eq);

        SocialAction sa = new SocialAction(baium.getObjectId(), 2);
        baium.broadcastPacket(sa);

        _socialTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new Social(baium,3), 15000);

        _recallPcTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new RecallPc(target), 20000);
        
        _socialTask2 = 
        	ThreadPoolManager.getInstance().scheduleEffect(new Social(baium,1), 25000);

        _killPcTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new KillPc(target,baium), 26000);

        _callAngelTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new CallArcAngel(),35000);

        _mobiliseTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(baium),35500);

        // move at random.
        if(Config.FWB_MOVEATRANDOM)
        {
        	L2CharPosition pos = new L2CharPosition(Rnd.get(112826, 116241),Rnd.get(15575, 16375),10078,0);
        	_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new MoveAtRandom(baium,pos),36000);
        }
        
        // set delete task.
        _activityTimeEndTask = 
        	ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(),Config.FWB_ACTIVITYTIMEOFBAIUM);

        baium = null;
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
        		int loc = Rnd.get(3);
        		pc.teleToLocation(_banishmentLocation[loc][0] + driftX,_banishmentLocation[loc][1] + driftY,_banishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
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

    // clean Baium's lair.
    public void setUnspawn()
	{
    	// eliminate players.
    	banishesPlayers();

    	// delete monsters.
    	ascensionArcAngel();
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
		if(_socialTask2 != null)
		{
			_socialTask2.cancel(true);
			_socialTask2 = null;
		}
		if(_recallPcTask != null)
		{
			_recallPcTask.cancel(true);
			_recallPcTask = null;
		}
		if(_killPcTask != null)
		{
			_killPcTask.cancel(true);
			_killPcTask = null;
		}
		if(_callAngelTask != null)
		{
			_callAngelTask.cancel(true);
			_callAngelTask = null;
		}

		// interval begin.
		setInetrvalEndTask();		
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
    public void spawnCube()
    {
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
    
    // start interval.
    public void setInetrvalEndTask()
    {
    	//init state of Baium's lair.  
    	if (!_State.getState().equals(GrandBossState.StateEnum.INTERVAL))  
    	{  
    		_State.setRespawnDate(Rnd.get(Config.FWB_FIXINTERVALOFBAIUM,Config.FWB_FIXINTERVALOFBAIUM + Config.FWB_RANDOMINTERVALOFBAIUM));  
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

    		// statue of Baium respawn.  
    		_StatueofBaiumSpawn.doSpawn(); 

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

    	ascensionArcAngel();  

    	_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new CubeSpawn(),10000); 

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

    // call arcangels
    private class CallArcAngel implements Runnable
    {
    	public CallArcAngel()
    	{
    	}

    	public void run()
    	{
    		adventArcAngel();
    		if(_callAngelTask != null)
    		{
        		_callAngelTask.cancel(true);
        		_callAngelTask = null;
    		}
    	}
    }

    // recall pc
    private class RecallPc implements Runnable
    {
    	L2PcInstance _target;
    	public RecallPc(L2PcInstance target)
    	{
    		_target = target;
    	}
    	public void run()
    	{
    		_target.teleToLocation(115831, 17248, 10078);
    	}
    }
    
    // kill pc
    private class KillPc  implements Runnable
    {
    	L2PcInstance _target;
    	L2BossInstance _boss;
    	public KillPc(L2PcInstance target,L2BossInstance boss)
    	{
    		_target = target;
    		_boss = boss;
    	}
    	public void run()
    	{
    		if (_target != null)  
    			_target.reduceCurrentHp(100000 + Rnd.get(_target.getMaxHp()/2,_target.getMaxHp()),_boss);
    	}
    }
}

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
import javolution.util.FastList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This class ...
 * Management for fight with sailren.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class SailrenManager
{
    private final static Log _log = LogFactory.getLog(SailrenManager.class.getName());
    private static SailrenManager _instance = new SailrenManager();

    // config
    // Properties of fight with sailren.
    // Whether to enable the entry of a single player.
    protected static final boolean _enableSinglePlayer = Config.FWS_ENABLESINGLEPLAYER;
    // Interval of spawn of next Sailren.
    protected static final int _intervalOfSailrenSpawn = Config.FWS_INTERVALOFSAILRENSPAWN;
    // Interval of spawn of next monster.
    protected static final int _intervalOfNextMonster = Config.FWS_INTERVALOFNEXTMONSTER;
    // Activity time of monsters.
    protected static final int _activityTimeOfMobs = Config.FWS_ACTIVITYTIMEOFMOBS;
    
    // teleport cube location.
    private final int _sailrenCubeLocation[][] = { {27734,-6838,-1982,0} };
    protected List<L2Spawn> _sailrenCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _sailrenCube = new FastList<L2NpcInstance>();

    // list of players in Sailren's lair.
    protected List<L2PcInstance> _playersInSailrenLair = new FastList<L2PcInstance>();

    // spawn data of monsters
    protected L2Spawn _velociraptorSpawn;	// Velociraptor
    protected L2Spawn _pterosaurSpawn;		// Pterosaur
    protected L2Spawn _tyrannoSpawn;		// Tyrannosaurus
    protected L2Spawn _sailrenSapwn;		// Sailren

    // Instance of monsters
    protected L2NpcInstance _velociraptor;	// Velociraptor
    protected L2NpcInstance _pterosaur;		// Pterosaur
    protected L2NpcInstance _tyranno;		// Tyrannosaurus
    protected L2NpcInstance _sailren;		// Sailren
    
    // Tasks
    protected Future _cubeSpawnTask = null;
    protected Future _sailrenSpawnTask = null;
    protected Future _intervalEndTask = null;
    protected Future _activityTimeEndTask = null;
    protected Future _onPartyAnnihilatedTask = null;
    protected Future _socialTask = null;
    
    // State of sailren's lair.
    protected boolean _isSailrenSpawned = false;
    protected boolean _isAlreadyEnteredOtherParty = false;
    protected boolean _isIntervalForSailrenSpawn = false;
    
    public SailrenManager()
    {
    }

    public static SailrenManager getInstance()
    {
        if (_instance == null) _instance = new SailrenManager();

        return _instance;
    }

    // init.
    public void init()
    {
    	// init state.
    	_isSailrenSpawned = false;
    	_isAlreadyEnteredOtherParty = false;
    	_isIntervalForSailrenSpawn = false;
    	
        // setting spawn data of monsters.
        try
        {
            L2NpcTemplate template1;
            
            // Velociraptor
            template1 = NpcTable.getInstance().getTemplate(22218); //Velociraptor
            _velociraptorSpawn = new L2Spawn(template1);
            _velociraptorSpawn.setLocx(27852);
            _velociraptorSpawn.setLocy(-5536);
            _velociraptorSpawn.setLocz(-1983);
            _velociraptorSpawn.setHeading(44732);
            _velociraptorSpawn.setAmount(1);
            _velociraptorSpawn.setRespawnDelay(_intervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_velociraptorSpawn, false);
            
            // Pterosaur
            template1 = NpcTable.getInstance().getTemplate(22199); //Pterosaur
            _pterosaurSpawn = new L2Spawn(template1);
            _pterosaurSpawn.setLocx(27852);
            _pterosaurSpawn.setLocy(-5536);
            _pterosaurSpawn.setLocz(-1983);
            _pterosaurSpawn.setHeading(44732);
            _pterosaurSpawn.setAmount(1);
            _pterosaurSpawn.setRespawnDelay(_intervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_pterosaurSpawn, false);
            
            // Tyrannosaurus
            template1 = NpcTable.getInstance().getTemplate(22217); //Tyrannosaurus
            _tyrannoSpawn = new L2Spawn(template1);
            _tyrannoSpawn.setLocx(27852);
            _tyrannoSpawn.setLocy(-5536);
            _tyrannoSpawn.setLocz(-1983);
            _tyrannoSpawn.setHeading(44732);
            _tyrannoSpawn.setAmount(1);
            _tyrannoSpawn.setRespawnDelay(_intervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_tyrannoSpawn, false);
            
            // Sailren
            template1 = NpcTable.getInstance().getTemplate(29065); //Sailren
            _sailrenSapwn = new L2Spawn(template1);
            _sailrenSapwn.setLocx(27810);
            _sailrenSapwn.setLocy(-5655);
            _sailrenSapwn.setLocz(-1983);
            _sailrenSapwn.setHeading(44732);
            _sailrenSapwn.setAmount(1);
            _sailrenSapwn.setRespawnDelay(_intervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_sailrenSapwn, false);
            
        }
        catch (Exception e)
        {
            _log.warn(e.getMessage());
        }

        // setting spawn data of teleporte cube.
        try
        {
            L2NpcTemplate cube = NpcTable.getInstance().getTemplate(32107);
            L2Spawn spawnDat;
        	
            for(int i = 0;i < _sailrenCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_sailrenCubeLocation[i][0]);
                spawnDat.setLocy(_sailrenCubeLocation[i][1]);
                spawnDat.setLocz(_sailrenCubeLocation[i][2]);
                spawnDat.setHeading(_sailrenCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _sailrenCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warn(e.getMessage());
        }
        
        _log.info("SailrenManager:Init SailrenManager.");
    }

    // getting list of players in sailren's lair.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _playersInSailrenLair;
	}
    
    // whether it is permitted to enter the sailren's lair is confirmed. 
    public int canIntoSailrenLair(L2PcInstance pc)
    {
    	if (_isSailrenSpawned) return 1;
    	if (_isAlreadyEnteredOtherParty) return 2;
    	if (_isIntervalForSailrenSpawn) return 3;
    	if ((_enableSinglePlayer == false) && (pc.getParty() == null)) return 4;
    	return 0;
    }
    
    // set sailren spawn task.
    public void setSailrenSpawnTask(int NpcId)
    {
    	if ((NpcId == 22218) && (_playersInSailrenLair.size() >= 1)) return;

    	if (_sailrenSpawnTask == null)
        {
        	_sailrenSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new SailrenSpawn(NpcId),_intervalOfNextMonster);
        }
    }

    // add player to list of players in sailren's lair.
    public void addPlayerToSailrenLair(L2PcInstance pc)
    {
        if (!_playersInSailrenLair.contains(pc)) _playersInSailrenLair.add(pc);
    }

    // teleporting player to sailren's lair.
    public void entryToSailrenLair(L2PcInstance pc)
    {
		int driftx;
		int drifty;

		if(canIntoSailrenLair(pc) != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Entrance was refused because it did not satisfy it. ");
			pc.sendPacket(sm);
			_isAlreadyEnteredOtherParty = false;
			return;
		}

		if(pc.getParty() == null)
		{
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			pc.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
			addPlayerToSailrenLair(pc);
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
				mem.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
				addPlayerToSailrenLair(mem);
			}
		}
		_isAlreadyEnteredOtherParty = true;
    }
    
    // whether the party was annihilated is confirmed. 
    public void checkAnnihilated(L2PcInstance pc)
    {
    	// It is a teleport later 5 seconds to the port when annihilating.
    	if(isPartyAnnihilated(pc))
    	{
    		_onPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleEffect(new OnPartyAnnihilatedTask(pc),5000);    			
    	}
    }

    // whether the party was annihilated is confirmed.
    public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
    {
		if(pc.getParty() != null)
		{
			for(L2PcInstance mem:pc.getParty().getPartyMembers())
			{
				if(!mem.isDead() && ZoneManager.getInstance().checkIfInZone("LairofSailren", pc))
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return true;
		}
    }

    // when annihilating or limit of time coming, the compulsion movement players from the sailren's lair.
    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _playersInSailrenLair)
    	{
    		if(pc.getQuestState("sailren") != null) pc.getQuestState("sailren").exitQuest(true);
    		if(ZoneManager.getInstance().checkIfInZone("LairofSailren", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(10468 + driftX,-24569 + driftY,-3650);
    		}
    	}
    	_playersInSailrenLair.clear();
    	_isAlreadyEnteredOtherParty = false;
    }
    
    // clean up sailren's lair.
    public void setUnspawn()
	{
    	// eliminate players.
    	banishesPlayers();
    	
    	// delete teleport cube.
		for (L2NpcInstance cube : _sailrenCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_sailrenCube.clear();
		
		// not executed tasks is canceled.
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if(_sailrenSpawnTask != null)
		{
			_sailrenSpawnTask.cancel(true);
			_sailrenSpawnTask = null;
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

		// init state of sailren's lair.
		_isSailrenSpawned = false;
		_velociraptor = null;
		_pterosaur = null;
		_tyranno = null;
		_sailren = null;

		// interval begin.
		setIntervalEndTask();
	}

    // spawn teleport cube.
    public void spawnCube()
    {
		for (L2Spawn spawnDat : _sailrenCubeSpawn)
		{
			_sailrenCube.add(spawnDat.doSpawn());
		}
    	_isIntervalForSailrenSpawn = true;
    }
    
    // task of teleport cube spawn.
    public void setCubeSpawn()
    {
		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new CubeSpawn(),10000);
    }
    
    // task of interval of sailren spawn.
    public void setIntervalEndTask()
    {
    	_intervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(new IntervalEnd(),_intervalOfSailrenSpawn);
    }
    
    // spawn monster.
    private class SailrenSpawn implements Runnable
    {
    	int _NpcId;
    	L2CharPosition _pos = new L2CharPosition(27628,-6109,-1982,44732);
    	public SailrenSpawn(int NpcId)
    	{
    		_NpcId = NpcId;
    	}
    	
        public void run()
        {
        	_isSailrenSpawned = true;
            switch (_NpcId)
            {
            	case 22218:		// Velociraptor
            		_velociraptor = _velociraptorSpawn.doSpawn();
            		_velociraptor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_socialTask != null)
            		{
            			_socialTask.cancel(true);
            			_socialTask = null;
            		}
            		_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new Social(_velociraptor,2),6000);
            		if(_activityTimeEndTask != null)
            		{
            			_activityTimeEndTask.cancel(true);
            			_activityTimeEndTask = null;
            		}
            		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(_velociraptor),_activityTimeOfMobs);
            		break;
            	case 22199:		// Pterosaur
            		_velociraptorSpawn.stopRespawn();
            		_pterosaur = _pterosaurSpawn.doSpawn();
            		_pterosaur.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_socialTask != null)
            		{
            			_socialTask.cancel(true);
            			_socialTask = null;
            		}
            		_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new Social(_pterosaur,2),6000);
            		if(_activityTimeEndTask != null)
            		{
            			_activityTimeEndTask.cancel(true);
            			_activityTimeEndTask = null;
            		}
            		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(_pterosaur),_activityTimeOfMobs);
            		break;
            	case 22217:		// Tyrannosaurus
            		_pterosaurSpawn.stopRespawn();
            		_tyranno = _tyrannoSpawn.doSpawn();
            		_tyranno.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_socialTask != null)
            		{
            			_socialTask.cancel(true);
            			_socialTask = null;
            		}
            		_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new Social(_tyranno,2),6000);
            		if(_activityTimeEndTask != null)
            		{
            			_activityTimeEndTask.cancel(true);
            			_activityTimeEndTask = null;
            		}
            		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(_tyranno),_activityTimeOfMobs);
            		break;
            	case 29065:		// Sailren
            		_tyrannoSpawn.stopRespawn();
            		_sailren = _sailrenSapwn.doSpawn();
            		_sailren.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_socialTask != null)
            		{
            			_socialTask.cancel(true);
            			_socialTask = null;
            		}
            		_socialTask = ThreadPoolManager.getInstance().scheduleEffect(new Social(_sailren,2),6000);
            		if(_activityTimeEndTask != null)
            		{
            			_activityTimeEndTask.cancel(true);
            			_activityTimeEndTask = null;
            		}
            		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(_sailren),_activityTimeOfMobs);
            		break;
            	default:
            		break;
            }
            
            if(_sailrenSpawnTask != null)
            {
            	_sailrenSpawnTask.cancel(true);
            	_sailrenSpawnTask = null;
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
    	L2NpcInstance _mob;
    	public ActivityTimeEnd(L2NpcInstance npc)
    	{
    		_mob = npc;
    	}
    	
    	public void run()
    	{
    		if(!_mob.isDead())
    		{
    			_mob.deleteMe();
    			_mob.getSpawn().stopRespawn();
    			_mob = null;
    		}
    	    // clean up sailren's lair.
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
    		_isIntervalForSailrenSpawn = false;
    		if(_intervalEndTask != null)
    		{
    			_intervalEndTask.cancel(true);
    			_intervalEndTask = null;
    		}
    	}
    }
    
    // when annihilating or limit of time coming, the compulsion movement players from the sailren's lair.
	private class OnPartyAnnihilatedTask implements Runnable
	{
		L2PcInstance _player;
		
		public OnPartyAnnihilatedTask(L2PcInstance player)
		{
			_player = player;
		}
		
		public void run()
		{
			setUnspawn();
			
            if(_onPartyAnnihilatedTask != null)
            {
            	_onPartyAnnihilatedTask.cancel(true);
            	_onPartyAnnihilatedTask = null;
            }
			
		}
	}

	// social.
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
    		for (L2PcInstance pc : _playersInSailrenLair)
    		{
    			_npc.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
    		}

    		SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);

            if(_socialTask != null)
    		{
    			_socialTask.cancel(true);
    			_socialTask = null;
    		}
        }
    }
}

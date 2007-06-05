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
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class SailrenManager
{
	private final static Log _log = LogFactory.getLog(SailrenManager.class.getName());
    private static SailrenManager _instance = new SailrenManager();

    protected static final boolean _EnableSinglePlayer = Config.FWS_ENABLESINGLEPLAYER;
    protected static final int _IntervalOfSailrenSpawn = Config.FWS_INTERVALOFSAILRENSPAWN;
    protected static final int _IntervalOfNextMonster = Config.FWS_INTERVALOFNEXTMONSTER;
    protected static final int _ActivityTimeOfMobs = Config.FWS_ACTIVITYTIMEOFMOBS;
    
    private final int _SailrenCubeLocation[][] =
    	{
    		{27734,-6838,-1982,0}
    	};
    protected List<L2Spawn> _SailrenCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _SailrenCube = new FastList<L2NpcInstance>();

    protected List<L2PcInstance> _PlayersInSailrenLair = new FastList<L2PcInstance>();

    protected L2Spawn _VelociraptorSpawn;
    protected L2Spawn _PterosaurSpawn;
    protected L2Spawn _TyrannoSpawn;
    protected L2Spawn _SailrenSapwn;

    protected L2NpcInstance _Velociraptor;
    protected L2NpcInstance _Pterosaur;
    protected L2NpcInstance _Tyranno;
    protected L2NpcInstance _Sailren;
    
    protected Future _CubeSpawnTask = null;
    protected Future _SailrenSpawnTask = null;
    protected Future _IntervalEndTask = null;
    protected Future _ActivityTimeEndTask = null;
    protected Future _OnPartyAnnihilatedTask = null;
    protected Future _SocialTask = null;
    
    protected boolean _IsSailrenSpawned = false;
    protected boolean _IsAlreadyEnteredOtherParty = false;
    protected boolean _IsIntervalForSailrenSpawn = false;
    
    public SailrenManager()
    {
    }

    public static SailrenManager getInstance()
    {
        if (_instance == null) _instance = new SailrenManager();

        return _instance;
    }

    public void init()
    {
    	_IsSailrenSpawned = false;
    	_IsAlreadyEnteredOtherParty = false;
    	_IsIntervalForSailrenSpawn = false;
    	
        try
        {
            L2NpcTemplate template1;
            
            template1 = NpcTable.getInstance().getTemplate(22218);
            _VelociraptorSpawn = new L2Spawn(template1);
            _VelociraptorSpawn.setLocx(27852);
            _VelociraptorSpawn.setLocy(-5536);
            _VelociraptorSpawn.setLocz(-1983);
            _VelociraptorSpawn.setHeading(44732);
            _VelociraptorSpawn.setAmount(1);
            _VelociraptorSpawn.setRespawnDelay(_IntervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_VelociraptorSpawn, false);
            
            template1 = NpcTable.getInstance().getTemplate(22199);
            _PterosaurSpawn = new L2Spawn(template1);
            _PterosaurSpawn.setLocx(27852);
            _PterosaurSpawn.setLocy(-5536);
            _PterosaurSpawn.setLocz(-1983);
            _PterosaurSpawn.setHeading(44732);
            _PterosaurSpawn.setAmount(1);
            _PterosaurSpawn.setRespawnDelay(_IntervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_PterosaurSpawn, false);
            
            template1 = NpcTable.getInstance().getTemplate(22217);
            _TyrannoSpawn = new L2Spawn(template1);
            _TyrannoSpawn.setLocx(27852);
            _TyrannoSpawn.setLocy(-5536);
            _TyrannoSpawn.setLocz(-1983);
            _TyrannoSpawn.setHeading(44732);
            _TyrannoSpawn.setAmount(1);
            _TyrannoSpawn.setRespawnDelay(_IntervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_TyrannoSpawn, false);
            
            template1 = NpcTable.getInstance().getTemplate(29065);
            _SailrenSapwn = new L2Spawn(template1);
            _SailrenSapwn.setLocx(27810);
            _SailrenSapwn.setLocy(-5655);
            _SailrenSapwn.setLocz(-1983);
            _SailrenSapwn.setHeading(44732);
            _SailrenSapwn.setAmount(1);
            _SailrenSapwn.setRespawnDelay(_IntervalOfSailrenSpawn * 2);
            SpawnTable.getInstance().addNewSpawn(_SailrenSapwn, false);
            
        }
        catch (Exception e)
        {
            _log.warn(e.getMessage());
        }

        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(31859);
            L2Spawn spawnDat;
        	
            Cube = NpcTable.getInstance().getTemplate(32107);
            for(int i = 0;i < _SailrenCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_SailrenCubeLocation[i][0]);
                spawnDat.setLocy(_SailrenCubeLocation[i][1]);
                spawnDat.setLocz(_SailrenCubeLocation[i][2]);
                spawnDat.setHeading(_SailrenCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _SailrenCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warn(e.getMessage());
        }
        
        _log.info("SailrenManager:Init SailrenManager.");
    }

    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInSailrenLair;
	}
    
    public int canIntoSailrenLair(L2PcInstance pc)
    {
    	if (_IsSailrenSpawned) return 1;
    	if (_IsAlreadyEnteredOtherParty) return 2;
    	if (_IsIntervalForSailrenSpawn) return 3;
    	if ((_EnableSinglePlayer == false) && (pc.getParty() == null)) return 4;
    	return 0;
    }
    
    public void setSailrenSpawnTask(int NpcId)
    {
    	if ((NpcId == 22218) && (_PlayersInSailrenLair.size() >= 1)) return;

    	if (_SailrenSpawnTask == null)
        {
        	_SailrenSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new SailrenSpawn(NpcId),_IntervalOfNextMonster);
        }
    }

    public void addPlayerToSailrenLair(L2PcInstance pc)
    {
        if (!_PlayersInSailrenLair.contains(pc)) _PlayersInSailrenLair.add(pc);
    }

    public void entryToSailrenLair(L2PcInstance pc)
    {
		int driftx;
		int drifty;

		if(canIntoSailrenLair(pc) != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("--");
			pc.sendPacket(sm);
			_IsAlreadyEnteredOtherParty = false;
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
			List<L2PcInstance> members = new FastList<L2PcInstance>();
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
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
		_IsAlreadyEnteredOtherParty = true;
    }
    
    public void checkAnnihilated(L2PcInstance pc)
    {
    	if(isPartyAnnihilated(pc))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleEffect(new OnPartyAnnihilatedTask(pc),5000);    			
    	}
    }

    public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
    {
		if(pc.getParty() != null)
		{
			for(L2PcInstance mem:pc.getParty().getPartyMembers())
			{
				if(!mem.isDead())
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

    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _PlayersInSailrenLair)
    	{
    		if(pc.getQuestState("sailren") != null) pc.getQuestState("sailren").exitQuest(true);
    		if(ZoneManager.getInstance().checkIfInZone("LairofSailren", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(10468 + driftX,-24569 + driftY,-3650);
    		}
    	}
    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
    }
    
    public void setUnspawn()
	{
    	banishesPlayers();
    	
		for (L2NpcInstance cube : _SailrenCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_SailrenCube.clear();
		
		if(_CubeSpawnTask != null)
		{
			_CubeSpawnTask.cancel(true);
			_CubeSpawnTask = null;
		}
		if(_SailrenSpawnTask != null)
		{
			_SailrenSpawnTask.cancel(true);
			_SailrenSpawnTask = null;
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

		_IsSailrenSpawned = false;
		_Velociraptor = null;
		_Pterosaur = null;
		_Tyranno = null;
		_Sailren = null;

		setInetrvalEndTask();
	}

    public void spawnCube()
    {
		for (L2Spawn spawnDat : _SailrenCubeSpawn)
		{
			_SailrenCube.add(spawnDat.doSpawn());
		}
    	_IsIntervalForSailrenSpawn = true;
    }
    
    public void setCubeSpawn()
    {
		_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            	new CubeSpawn(),10000);
    }
    
    public void setInetrvalEndTask()
    {
    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(
            	new IntervalEnd(),_IntervalOfSailrenSpawn);
    }
    
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
        	_IsSailrenSpawned = true;
            switch (_NpcId)
            {
            	case 22218:
            		_Velociraptor = _VelociraptorSpawn.doSpawn();
            		_Velociraptor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new Social(_Velociraptor,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new ActivityTimeEnd(_Velociraptor),_ActivityTimeOfMobs);
            		break;
            	case 22199:
            		_VelociraptorSpawn.stopRespawn();
            		_Pterosaur = _PterosaurSpawn.doSpawn();
            		_Pterosaur.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new Social(_Pterosaur,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new ActivityTimeEnd(_Pterosaur),_ActivityTimeOfMobs);
            		break;
            	case 22217:
            		_PterosaurSpawn.stopRespawn();
            		_Tyranno = _TyrannoSpawn.doSpawn();
            		_Tyranno.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new Social(_Tyranno,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new ActivityTimeEnd(_Tyranno),_ActivityTimeOfMobs);
            		break;
            	case 29065:
            		_TyrannoSpawn.stopRespawn();
            		_Sailren = _SailrenSapwn.doSpawn();
            		_Sailren.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new Social(_Sailren,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask = 
                        ThreadPoolManager.getInstance().scheduleEffect(
                        		new ActivityTimeEnd(_Sailren),_ActivityTimeOfMobs);
            		break;
            	default:
            		break;
            }
            
            if(_SailrenSpawnTask != null)
            {
            	_SailrenSpawnTask.cancel(true);
            	_SailrenSpawnTask = null;
            }
        }
    }

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
    
    private class ActivityTimeEnd implements Runnable
    {
    	L2NpcInstance _Mob;
    	public ActivityTimeEnd(L2NpcInstance npc)
    	{
    		_Mob = npc;
    	}
    	
    	public void run()
    	{
    		if(!_Mob.isDead())
    		{
    			_Mob.deleteMe();
    			_Mob.getSpawn().stopRespawn();
    			_Mob = null;
    		}
    		setUnspawn();
    	}
    }
    
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}
    	
    	public void run()
    	{
    		_IsIntervalForSailrenSpawn = false;
    		if(_IntervalEndTask != null)
    		{
    			_IntervalEndTask.cancel(true);
    			_IntervalEndTask = null;
    		}
    	}
    }
    
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
			
            if(_OnPartyAnnihilatedTask != null)
            {
            	_OnPartyAnnihilatedTask.cancel(true);
            	_OnPartyAnnihilatedTask = null;
            }
			
		}
	}

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
    		for (L2PcInstance pc : _PlayersInSailrenLair)
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
}

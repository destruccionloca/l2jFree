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
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Skill;
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
 * control for sequence of figth with Antharas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class AntharasManager
{
    private final static Log _log = LogFactory.getLog(AntharasManager.class.getName());
    private static AntharasManager _instance = new AntharasManager();

    // config
    // Interval time of Boss.
    protected int _IntervalOfBoss;

    // Delay of appearance time of Boss.
    protected int _AppTimeOfBoss;

    // Activity time of Boss.
    protected int _ActivityTimeOfBoss;

    // Type of Antharas subjugation.
    // If setting 'True'. The change in the power of Antharas doesn't occur.
    protected boolean _OldAntharas;

    // Limitation value to change power of Antharas by number of players in Antharas's lair. 
    // Weak: LimitOfWeak >= Players
    // Normal: LimitOfWeak < Players <= LimitOfNormal
    // Strong: Players > LimitOfNormal
    // Weak
    protected int _LimitOfWeak;
    // Normal
    protected int _LimitOfNormal;

    // Interval time for spawn of Antharas's minions.
    // Value is minute. Range 1-10
    // Behemoth Dragon
    // Weak
    protected int _IntgervalOfBehemothOnWeak;
    // Normal
    protected int _IntgervalOfBehemothOnNormal;
    // Strong
    protected int _IntgervalOfBehemothOnStrong;
    // Dragon Bomber
    // Weak
    protected int _IntgervalOfBomberOnWeak;
    // Normal
    protected int _IntgervalOfBomberOnNormal;
    // Strong
    protected int _IntgervalOfBomberOnStrong;

    // Whether it moves at random after Antharas appears is decided.
    protected boolean _MoveAtRandom = true;
    
    // location of teleport cube.
    private final int _TeleportCubeId = 31859;
    private final int _TeleportCubeLocation[][] =
    	{
    		{177615, 114941, -7709,0}
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
    protected Future _BehemothSpawnTask = null;
    protected Future _BomberSpawnTask = null;
    protected Future _SelfDestructionTask = null;
    protected Future _MoveAtRandomTask = null;
    
    // status in lair.
    protected boolean _IsBossSpawned = false;
    protected boolean _IsIntervalForNextSpawn = false;
    protected String _ZoneType;
    protected String _QuestName;
    
    // location of banishment
    private final int _BanishmentLocation[][] =
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
    	// read configuration.
    	_IntervalOfBoss = Config.FWA_INTERVALOFANTHARAS;
    	_AppTimeOfBoss = Config.FWA_APPTIMEOFANTHARAS;
    	_ActivityTimeOfBoss = Config.FWA_ACTIVITYTIMEOFANTHARAS;
    	_OldAntharas = Config.FWA_OLDANTHARAS;
    	_LimitOfWeak = Config.FWA_LIMITOFWEAK;
    	_LimitOfNormal = Config.FWA_LIMITOFNORMAL;
    	if(_LimitOfWeak >= _LimitOfNormal) _LimitOfNormal = _LimitOfWeak + 1;
    	_IntgervalOfBehemothOnWeak = Config.FWA_INTGERVALOFBEHEMOTHONWEAK;
    	_IntgervalOfBehemothOnNormal = Config.FWA_INTGERVALOFBEHEMOTHONNORMAL;
    	_IntgervalOfBehemothOnStrong = Config.FWA_INTGERVALOFBEHEMOTHONSTRONG;
    	_IntgervalOfBomberOnWeak = Config.FWA_INTGERVALOFBOMBERONWEAK;
    	_IntgervalOfBomberOnNormal = Config.FWA_INTGERVALOFBOMBERONNORMAL;
    	_IntgervalOfBomberOnStrong = Config.FWA_INTGERVALOFBOMBERONSTRONG;
    	_MoveAtRandom = Config.FWA_MOVEATRANDOM;
    	
    	// initialize status in lair.
    	_IsBossSpawned = false;
    	_IsIntervalForNextSpawn = false;
    	_PlayersInLair.clear();
        _ZoneType = "LairofAntharas";
        _QuestName = "antharas";

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
            tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29019, tempSpawn);
            
            // weak Antharas.
            template1 = NpcTable.getInstance().getTemplate(29066);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29066, tempSpawn);
            
            // normal Antharas.
            template1 = NpcTable.getInstance().getTemplate(29067);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29067, tempSpawn);
            
            // strong Antharas.
            template1 = NpcTable.getInstance().getTemplate(29068);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29068, tempSpawn);
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
        
        _log.info("AntharasManager:Init AntharasManager.");
    }

    // return list of intruders.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInLair;
	}
    
    // Whether it lairs is confirmed. 
    public boolean isEnableEnterToLair()
    {
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
        		int loc = Rnd.get(4);
        		pc.teleToLocation(_BanishmentLocation[loc][0] + driftX,_BanishmentLocation[loc][1] + driftY,_BanishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
    }
    
    // do spawn teleport cube.
    public void spawnCube()
    {
		if(_BehemothSpawnTask != null)
		{
			_BehemothSpawnTask.cancel(true);
			_BehemothSpawnTask = null;
		}
		if(_BomberSpawnTask != null)
		{
			_BomberSpawnTask.cancel(true);
			_BomberSpawnTask = null;
		}
		if(_SelfDestructionTask != null)
		{
			_SelfDestructionTask.cancel(true);
			_SelfDestructionTask = null;
		}
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

    // setting Antharas spawn task.
    public void setAntharasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_PlayersInLair.size() >= 1) return;

    	if (_MonsterSpawnTask == null)
        {
        	_MonsterSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new AntharasSpawn(),_AppTimeOfBoss);
        }
    }
    
    // do spawn Antharas.
    private class AntharasSpawn implements Runnable
    {
    	AntharasSpawn()
    	{
    	}
    	
    	public void run()
    	{
        	int npcId;
        	
        	// Strength of Antharas is decided by the number of players that invaded the lair.
        	if(_OldAntharas) npcId = 29019;	// old
        	else if(_PlayersInLair.size() <= _LimitOfWeak) npcId = 29066;	// weak
        	else if(_PlayersInLair.size() >= _LimitOfNormal) npcId = 29068;	// strong
        	else npcId = 29067;	//normal

        	// do spawn.
        	L2Spawn antharasSpawn = _MonsterSpawn.get(npcId);
        	L2BossInstance antharas = (L2BossInstance)antharasSpawn.doSpawn();
        	_Monsters.add(antharas);
        	
        	// do social.
        	antharas.setIsImobilised(true);
        	antharas.setIsInSocialAction(true);
            SocialAction sa = new SocialAction(antharas.getObjectId(), 3);
            antharas.broadcastPacket(sa);

            _SocialTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new Social(antharas,2), 15000);

            _MobiliseTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(antharas),30000);

            // setting 1st time of minions spawn task.
            if(!_OldAntharas)
            {
            	int intervalOfBehemoth;
            	int intervalOfBomber;
            	
            	// Interval of minions is decided by the number of players that invaded the lair.
            	if(_PlayersInLair.size() <= _LimitOfWeak)	// weak
            	{
            		intervalOfBehemoth = _IntgervalOfBehemothOnWeak;
            		intervalOfBomber = _IntgervalOfBomberOnWeak;
            	}
            	else if(_PlayersInLair.size() >= _LimitOfNormal)	// strong
            	{
            		intervalOfBehemoth = _IntgervalOfBehemothOnStrong;
            		intervalOfBomber = _IntgervalOfBomberOnStrong;
            	}
            	else	//normal
            	{
            		intervalOfBehemoth = _IntgervalOfBehemothOnNormal;
            		intervalOfBomber = _IntgervalOfBomberOnNormal;
            	}
            	
            	// spawn Behemoth.
            	_BehemothSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
                    		new BehemothSpawn(intervalOfBehemoth),30000);

            	// spawn Bomber.
            	_BomberSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new BomberSpawn(intervalOfBomber),30000);
            }
            
            // move at random.
            if(_MoveAtRandom)
            {
            	L2CharPosition pos = new L2CharPosition(Rnd.get(175000, 178500),Rnd.get(112400, 116000),-7707,0);
            	_MoveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new MoveAtRandom(antharas,pos),31000);
            }
            
            // set delete task.
            _ActivityTimeEndTask = 
            	ThreadPoolManager.getInstance().scheduleEffect(new ActivityTimeEnd(),_ActivityTimeOfBoss);
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
                tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
            	_Monsters.add(tempSpawn.doSpawn());

            }
            catch (Exception e)
            {
                _log.warn(e.getMessage());
            }
            
            if(_BehemothSpawnTask != null)
            {
            	_BehemothSpawnTask.cancel(true);
            	_BehemothSpawnTask = null;
            }
            
            // repeat.
        	_BehemothSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
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
                tempSpawn.setRespawnDelay(_IntervalOfBoss * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
                bomber = tempSpawn.doSpawn();
            	_Monsters.add(bomber);

            }
            catch (Exception e)
            {
                _log.warn(e.getMessage());
            }
            
            // set self destruction.
            if(bomber != null)
            {
                _SelfDestructionTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new SelfDestructionOfBomber(bomber),1000);
            }
            
            if(_BomberSpawnTask != null)
            {
            	_BomberSpawnTask.cancel(true);
            	_BomberSpawnTask = null;
            }

            // repeat.
            _BomberSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
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
				default:
					skill = null;
			}
    		
    		_bomber.doCast(skill);

    		if(_SelfDestructionTask != null)
            {
    			_SelfDestructionTask.cancel(true);
    			_SelfDestructionTask = null;
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
    		
    		if(_ActivityTimeEndTask != null)
    		{
    			_ActivityTimeEndTask.cancel(true);
    			_ActivityTimeEndTask = null;
    		}
    	}
    }
    
    // clean Antharas's lair.
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
		if(_BehemothSpawnTask != null)
		{
			_BehemothSpawnTask.cancel(true);
			_BehemothSpawnTask = null;
		}
		if(_BomberSpawnTask != null)
		{
			_BomberSpawnTask.cancel(true);
			_BomberSpawnTask = null;
		}
		if(_SelfDestructionTask != null)
		{
			_SelfDestructionTask.cancel(true);
			_SelfDestructionTask = null;
		}
		if(_MoveAtRandomTask != null)
		{
			_MoveAtRandomTask.cancel(true);
			_MoveAtRandomTask = null;
		}

		// init state of Antharas's lair.
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

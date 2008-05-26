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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.entity.Entity;
import net.sf.l2j.gameserver.model.entity.GrandBossState;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class ...
 * control for sequence of fight against "High Priestess van Halter".
 @version $Revision: $ $Date: $
 @author L2J_JP SANDMAN
**/

public class VanHalterManager extends Entity
{
	private final static Log _log = LogFactory.getLog(VanHalterManager.class.getName());
    private static VanHalterManager _instance = new VanHalterManager();

    // list of intruders.
    protected List<L2PcInstance> _playersInLair = new FastList<L2PcInstance>();
    protected Map<Integer,List> _bleedingPlayers = new FastMap<Integer,List>();
    
    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _monsterSpawn = new FastMap<Integer,L2Spawn>();
    protected List<L2Spawn> _royalGuardSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _royalGuardCaptainSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _royalGuardHelperSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _toriolRevelationSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _toriolRevelationAlive = new FastList<L2Spawn>();
    protected List<L2Spawn> _guardOfAltarSpawn = new FastList<L2Spawn>();
    protected Map<Integer,L2Spawn> _cameraMarkerSpawn = new FastMap<Integer,L2Spawn>();
    protected L2Spawn _ritualOfferingSpawn = null;
    protected L2Spawn _ritualSacrificeSpawn = null;
    protected L2Spawn _vanHalterSpawn = null;
    
    // instance of monsters.
    protected List<L2NpcInstance> _monsters = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _royalGuard = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _royalGuardCaptain = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _royalGuardHepler = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _toriolRevelation = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _guardOfAltar = new FastList<L2NpcInstance>();
    protected Map<Integer,L2NpcInstance> _cameraMarker = new FastMap<Integer,L2NpcInstance>(); 
    protected List<L2DoorInstance> _doorOfAltar = new FastList<L2DoorInstance>();
    protected List<L2DoorInstance> _doorOfSacrifice = new FastList<L2DoorInstance>();
    protected L2NpcInstance _ritualOffering = null;
    protected L2NpcInstance _ritualSacrifice = null;
    protected L2RaidBossInstance _vanHalter = null;
    
    // Task
    protected ScheduledFuture<?> _movieTask = null;
    protected ScheduledFuture<?> _closeDoorOfAltarTask = null;
    protected ScheduledFuture<?> _openDoorOfAltarTask = null;
    protected ScheduledFuture<?> _lockUpDoorOfAltarTask = null;
    protected ScheduledFuture<?> _callRoyalGuardHelperTask = null;
    protected ScheduledFuture<?> _timeUpTask = null;
    protected ScheduledFuture<?> _intervalTask = null;
    protected ScheduledFuture<?> _halterEscapeTask = null;
    protected ScheduledFuture<?> _setBleedTask = null;
    
    // state of High Priestess van Halter
    boolean _isLocked = false;
    boolean _isHalterSpawned = false;
    boolean _isSacrificeSpawned = false;
    boolean _isCaptainSpawned = false;
    boolean _isHelperCalled = false;
    protected GrandBossState _state = new GrandBossState(29062);

    public static VanHalterManager getInstance()
    {
        if (_instance == null) _instance = new VanHalterManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// clear intruder.
    	_playersInLair.clear();

    	// clear flag.
        _isLocked = false;
        _isCaptainSpawned = false;
        _isHelperCalled = false;
        _isHalterSpawned = false;
    	
    	// setting door state.
    	_doorOfAltar.add(DoorTable.getInstance().getDoor(19160014));
    	_doorOfAltar.add(DoorTable.getInstance().getDoor(19160015));
    	openDoorOfAltar(true);
    	_doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160016));
    	_doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160017));
    	closeDoorOfSacrifice();
    	
        // load spawn data of monsters.
    	loadRoyalGuard();
    	loadToriolRevelation();
    	loadRoyalGuardCaptain();
    	loadRoyalGuardHelper();
    	loadGuardOfAltar();
    	loadVanHalter();
    	loadRitualOffering();
    	loadRitualSacrifice();
    	
    	// spawn monsters.
    	spawnRoyalGuard();
    	spawnToriolRevelation();
        spawnVanHalter();
        spawnRitualOffering();
    	
        // setting spawn data of Dummy camera marker.
    	_cameraMarkerSpawn.clear();
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;

            // Dummy camera marker.
            template1 = NpcTable.getInstance().getTemplate(13018);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-10449);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _cameraMarkerSpawn.put(1, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(13018);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-10051);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _cameraMarkerSpawn.put(2, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(13018);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-9741);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _cameraMarkerSpawn.put(3, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(13018);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-9394);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _cameraMarkerSpawn.put(4, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(13018);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55197);
            tempSpawn.setLocz(-8739);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _cameraMarkerSpawn.put(5, tempSpawn);

        }
        catch (Exception e)
        {
            _log.warn("VanHalterManager : " + e.getMessage());
        }
        
    	// set time up.
    	if (_timeUpTask != null) _timeUpTask.cancel(true);
    	_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(),Config.HPH_ACTIVITYTIMEOFHALTER);
        
    	// set bleeding to palyers.
		if (_setBleedTask != null) _setBleedTask.cancel(true);
		_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(),2000);

		// check state of High Priestess van Halter.
		_log.info("VanHalterManager : State of High Priestess van Halter is " + _state.getState() + ".");
		if (_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
			enterInterval();
		else
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
		
		Date dt = new Date(_state.getRespawnDate());
		_log.info("VanHalterManager : Next spawn date of High Priestess van Halter is " + dt + ".");
        _log.info("VanHalterManager : init VanHalterManager.");
    }
    
    // return High Priestess van Halter state.
    public GrandBossState.StateEnum getState()
    {
    	return _state.getState();
    }

    // load Royal Guard.
    protected void loadRoyalGuard()
    {
    	_royalGuardSpawn.clear();
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
            statement.setInt(1, 22175);
            statement.setInt(2, 22176);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_royalGuardSpawn.add(spawnDat);
                }
                else {
                    _log.warn("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadRoyalGuard: Loaded " + _royalGuardSpawn.size() + " Royal Guard spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRoyalGuard()
    {
    	if (!_royalGuard.isEmpty()) deleteRoyalGuard();
    	
    	for(L2Spawn rgs : _royalGuardSpawn)
    	{
    		rgs.startRespawn();
    		_royalGuard.add(rgs.doSpawn());
    	}
    }
    
    protected void deleteRoyalGuard()
    {
    	for(L2NpcInstance rg : _royalGuard)
    	{
    		rg.getSpawn().stopRespawn();
    		rg.deleteMe();
    	}
    	
    	_royalGuard.clear();
    }
    
    // load Triol's Revelation.
    protected void loadToriolRevelation()
    {
    	_toriolRevelationSpawn.clear();
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
            statement.setInt(1, 32058);
            statement.setInt(2, 32068);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_toriolRevelationSpawn.add(spawnDat);
                }
                else {
                    _log.warn("VanHalterManager.loadToriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadToriolRevelation: Loaded " + _toriolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadToriolRevelation: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }

    protected void spawnToriolRevelation()
    {
    	if (!_toriolRevelation.isEmpty()) deleteToriolRevelation();
    	
    	for(L2Spawn trs : _toriolRevelationSpawn)
    	{
    		trs.startRespawn();
    		_toriolRevelation.add(trs.doSpawn());
    		
    		if (trs.getNpcid() != 32067 && trs.getNpcid() != 32068)
    			_toriolRevelationAlive.add(trs);
    	}
    	
    }
    
    protected void deleteToriolRevelation()
    {
    	for(L2NpcInstance tr : _toriolRevelation)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}
    	
    	_toriolRevelation.clear();
    	_bleedingPlayers.clear();

    }
    
    // load Royal Guard Captain.
    protected void loadRoyalGuardCaptain()
    {
    	_royalGuardCaptainSpawn.clear();
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22188);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_royalGuardCaptainSpawn.add(spawnDat);
                }
                else {
                    _log.warn("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadRoyalGuardCaptain: Loaded " + _royalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnRoyalGuardCaptain()
    {
    	if (!_royalGuardCaptain.isEmpty()) deleteRoyalGuardCaptain();
    	
    	for(L2Spawn trs : _royalGuardCaptainSpawn)
    	{
    		trs.startRespawn();
    		_royalGuardCaptain.add(trs.doSpawn());
    	}
    	_isCaptainSpawned = true;    	
    }
    
    protected void deleteRoyalGuardCaptain()
    {
    	for(L2NpcInstance tr : _royalGuardCaptain)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}
    	
    	_royalGuardCaptain.clear();
    	
    }
    
    // load Royal Guard Helper.
    protected void loadRoyalGuardHelper()
    {
    	_royalGuardHelperSpawn.clear();
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22191);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_royalGuardHelperSpawn.add(spawnDat);
                }
                else {
                    _log.warn("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadRoyalGuardHelper: Loaded " + _royalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnRoyalGuardHepler()
    {
    	for(L2Spawn trs : _royalGuardHelperSpawn)
    	{
    		trs.startRespawn();
    		_royalGuardHepler.add(trs.doSpawn());
    	}
    }
    
    protected void deleteRoyalGuardHepler()
    {
    	for(L2NpcInstance tr : _royalGuardHepler)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}
    	
    	_royalGuardHepler.clear();
    	
    }
    
    // load Guard Of Altar
    protected void loadGuardOfAltar()
    {
    	_guardOfAltarSpawn.clear();
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 32051);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_guardOfAltarSpawn.add(spawnDat);
                }
                else {
                    _log.warn("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadGuardOfAltar: Loaded " + _guardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnGuardOfAltar()
    {
    	if (!_guardOfAltar.isEmpty()) deleteGuardOfAltar();
    	
    	for(L2Spawn trs : _guardOfAltarSpawn)
    	{
    		trs.startRespawn();
    		_guardOfAltar.add(trs.doSpawn());
    	}
    	
    }
    
    protected void deleteGuardOfAltar()
    {
    	for(L2NpcInstance tr : _guardOfAltar)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}
    	
    	_guardOfAltar.clear();
    	
    }

    // load High Priestess van Halter.
    protected void loadVanHalter()
    {
    	_vanHalterSpawn = null;
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 29062);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_vanHalterSpawn = spawnDat;
                }
                else {
                    _log.warn("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadVanHalter: Loaded High Priestess van Halter spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnVanHalter()
    {
    	_vanHalter = (L2RaidBossInstance)_vanHalterSpawn.doSpawn();
    	_vanHalter.setIsImmobilized(true);
    	_vanHalter.setIsInvul(true);
    	_isHalterSpawned = true;
    }
    
    protected void deleteVanHalter()
    {
    	_vanHalter.setIsImmobilized(false);
    	_vanHalter.setIsInvul(false);
    	_vanHalter.getSpawn().stopRespawn();
    	_vanHalter.deleteMe();
    }
    
    // load Ritual Offering.
    protected void loadRitualOffering()
    {
    	_ritualOfferingSpawn = null;
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 32038);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_ritualOfferingSpawn = spawnDat;
                }
                else {
                    _log.warn("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadRitualOffering: Loaded Ritual Offering spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnRitualOffering()
    {
    	_ritualOffering = _ritualOfferingSpawn.doSpawn();
    	_ritualOffering.setIsImmobilized(true);
    	_ritualOffering.setIsInvul(true);
    	_ritualOffering.setIsParalyzed(true);
    }
    
    protected void deleteRitualOffering()
    {
    	_ritualOffering.setIsImmobilized(false);
    	_ritualOffering.setIsInvul(false);
    	_ritualOffering.setIsParalyzed(false);
    	_ritualOffering.getSpawn().stopRespawn();
    	_ritualOffering.deleteMe();
    }
 
    // Load Ritual Sacrifice.
    protected void loadRitualSacrifice()
    {
    	_ritualSacrificeSpawn = null;
    	
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(null);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22195);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_ritualSacrificeSpawn = spawnDat;
                }
                else {
                    _log.warn("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager.loadRitualSacrifice: Loaded Ritual Sacrifice spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    	
    }
    
    protected void spawnRitualSacrifice()
    {
    	_ritualSacrifice = _ritualSacrificeSpawn.doSpawn();
    	_ritualSacrifice.setIsImmobilized(true);
    	_ritualSacrifice.setIsInvul(true);
    	_isSacrificeSpawned = true;
    }
    
    protected void deleteRitualSacrifice()
    {
    	if (!_isSacrificeSpawned) return; 

    	_ritualSacrifice.getSpawn().stopRespawn();
    	_ritualSacrifice.deleteMe();
    	_isSacrificeSpawned = false;
    }
    
    protected void spawnCameraMarker()
    {
    	_cameraMarker.clear();
    	for(int i = 1; i <= _cameraMarkerSpawn.size();i++)
    	{
    		_cameraMarker.put(i, _cameraMarkerSpawn.get(i).doSpawn());
    		_cameraMarker.get(i).getSpawn().stopRespawn();
    		_cameraMarker.get(i).setIsImmobilized(true);
    	}
    }
    
    protected void deleteCameraMarker()
    {
    	if (_cameraMarker.isEmpty()) return;
    	
    	for(int i = 1; i <= _cameraMarker.size();i++)
    	{
    		_cameraMarker.get(i).deleteMe();
    	}
    	_cameraMarker.clear();
    }
    
    // door control.
    public void intruderDetection(L2PcInstance intruder)
    {
    	if (!_playersInLair.contains(intruder)) _playersInLair.add(intruder);
    	if (_lockUpDoorOfAltarTask == null && !_isLocked && _isCaptainSpawned)
    	{
    		_lockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(),Config.HPH_TIMEOFLOCKUPDOOROFALTAR);
    	}
    }
    
    private class LockUpDoorOfAltar implements Runnable
    {
    	public LockUpDoorOfAltar()
    	{
    		
    	}
    	
    	public void run()
    	{
    		closeDoorOfAltar(false);
    		_isLocked = true;
    		_lockUpDoorOfAltarTask = null;
    	}
    }
    
    protected void openDoorOfAltar(boolean loop)
    {
    	for (L2DoorInstance door : _doorOfAltar)
    	{
    		try
    		{
    			door.openMe();
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	
    	if (loop)
    	{
    		_isLocked = false;

    		if (_closeDoorOfAltarTask != null) _closeDoorOfAltarTask.cancel(true);
    		_closeDoorOfAltarTask = null;
    		_closeDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(),Config.HPH_INTERVALOFDOOROFALTER);
    	}
    	else
    	{
    		if (_closeDoorOfAltarTask != null) _closeDoorOfAltarTask.cancel(true);
    		_closeDoorOfAltarTask = null;
    	}
    }
    
    private class OpenDoorOfAltar implements Runnable
    {
    	public OpenDoorOfAltar()
    	{
    		
    	}

    	public void run()
    	{
    		openDoorOfAltar(true);
    	}
    }

    protected void closeDoorOfAltar(boolean loop)
    {
    	for (L2DoorInstance door : _doorOfAltar)
    	{
    		door.closeMe();
    	}

    	if (loop)
    	{
    		if(_openDoorOfAltarTask != null) _openDoorOfAltarTask.cancel(true);
    		_openDoorOfAltarTask = null;
    		_openDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(),Config.HPH_INTERVALOFDOOROFALTER);
    	}
    	else
    	{
    		if(_openDoorOfAltarTask != null) _openDoorOfAltarTask.cancel(true);
    		_openDoorOfAltarTask = null;
    	}
    }
    
    private class CloseDoorOfAltar implements Runnable
    {
    	public CloseDoorOfAltar()
    	{
    		
    	}
    	
    	public void run()
    	{
    		closeDoorOfAltar(true);
    	}
    }
    
    protected void openDoorOfSacrifice()
    {
    	for (L2DoorInstance door : _doorOfSacrifice)
    	{
    		try
    		{
    			door.openMe();
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }

    protected void closeDoorOfSacrifice()
    {
    	for (L2DoorInstance door : _doorOfSacrifice)
    	{
    		try
    		{
    			door.closeMe();
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }

    // event
    public void checkToriolRevelationDestroy()
    {
    	if (_isCaptainSpawned) return;
    	
    	boolean isToriolRevelationDestroyed = true;
    	for (L2Spawn tra : _toriolRevelationAlive)
    	{
    		if (!tra.getLastSpawn().isDead()) isToriolRevelationDestroyed = false;
    	}
    	
    	if (isToriolRevelationDestroyed)
    	{
    		spawnRoyalGuardCaptain();
    	}
    }

    public void checkRoyalGuardCaptainDestroy()
    {
    	if (!_isHalterSpawned) return;
    	
    	deleteRoyalGuard();
    	deleteRoyalGuardCaptain();
    	spawnGuardOfAltar();
    	openDoorOfSacrifice();
    	
    	for (L2NpcInstance goa : _guardOfAltar)
    	{
        	CreatureSay cs = new CreatureSay(goa.getObjectId(),1,goa.getName(),"The door of the 3rd floor in the altar was opened.");
        	for (L2PcInstance pc : _playersInLair)
    		{
    			pc.sendPacket(cs);
    		}
    	}
    	
    	updateKnownList(_vanHalter);
    	_vanHalter.setIsImmobilized(true);
    	_vanHalter.setIsInvul(true);
    	spawnCameraMarker();

    	if (_timeUpTask != null) _timeUpTask.cancel(true);
    	_timeUpTask = null;
    	
    	_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1),Config.HPH_APPTIMEOFHALTER);
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

    // start fight against High Priestess van Halter.
    protected void combatBeginning()
    {
    	if (_timeUpTask != null) _timeUpTask.cancel(true);
    	_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(),Config.HPH_FIGHTTIMEOFHALTER);

    	Map<Integer, L2PcInstance> _targets = new FastMap<Integer, L2PcInstance>();
    	int i = 0;
    	
    	for (L2PcInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
    	{
    		i++;
    		_targets.put(i, pc);
    	}
    	
    	_vanHalter.reduceCurrentHp(1, _targets.get(Rnd.get(1, i)));
    	
    }

    // call Royal Guard Helper and escape from player.
    public void callRoyalGuardHelper()
    {
    	if (!_isHelperCalled)
    	{
        	_isHelperCalled = true;
        	_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(),500);
        	_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(),1000);
    	}
    	
    }
    
    private class CallRoyalGuardHelper implements Runnable
    {
    	public CallRoyalGuardHelper()
    	{
    		
    	}
    	
    	public void run()
    	{
    		spawnRoyalGuardHepler();

    		if (_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !_vanHalter.isDead())
    		{
	    		if (_callRoyalGuardHelperTask != null) _callRoyalGuardHelperTask.cancel(true);
    			_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(),Config.HPH_CALLROYALGUARDHELPERINTERVAL);
    		}
    		else
    		{
	    		if (_callRoyalGuardHelperTask != null) _callRoyalGuardHelperTask.cancel(true);
        		_callRoyalGuardHelperTask = null;
    		}
    	}
    }
    
    private class HalterEscape implements Runnable
    {
    	public HalterEscape()
    	{
    		
    	}
    	
    	public void run()
    	{
    		if (_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !_vanHalter.isDead())
    		{
    			if (_vanHalter.isAfraid())
    			{
                	_vanHalter.stopFear(null);
    			}
    			else
    			{
                	_vanHalter.startFear();
        	    	if (_vanHalter.getZ() >= -10476)
        	    	{
        	        	L2CharPosition pos = new L2CharPosition(-16397,-53308,-10448,0);
        	        	if (_vanHalter.getX() == pos.x && _vanHalter.getY() == pos.y)
        	        	{
                        	_vanHalter.stopFear(null);
        	        	}
        	        	else
        	        	{
            	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	        	}
        	    	}
        	    	else if (_vanHalter.getX() >= -16397)
        	    	{
        	        	L2CharPosition pos = new L2CharPosition(-15548,-54830,-10475,0);
        	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	    	}
        	    	else
        	    	{
        	        	L2CharPosition pos = new L2CharPosition(-17248,-54830,-10475,0);
        	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	    	}
    			}
    	  		if (_halterEscapeTask != null)	_halterEscapeTask.cancel(true);
    	  		_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(),5000);
    		}
    		else
    		{
            	_vanHalter.stopFear(null);
    	   		if (_halterEscapeTask != null)	_halterEscapeTask.cancel(true);
    	   		_halterEscapeTask = null;
    		}
    	}
    }

    // check bleeding player.
    protected void addBleeding()
    {
    	L2Skill bleed = SkillTable.getInstance().getInfo(4615,12);
    	
		for (L2NpcInstance tr : _toriolRevelation)
		{
			if (tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).size() == 0 || tr.isDead()) continue;

			List<L2PcInstance> bpc = new FastList<L2PcInstance>();
			
			for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUse(tr, pc, bleed.getId(), 12, 1, 1));
				}
				
				bpc.add(pc);
			}
			_bleedingPlayers.remove(tr.getNpcId());
			_bleedingPlayers.put(tr.getNpcId(), bpc);
		}
    }
    
    public void removeBleeding(int npcId)
    {
    	if (_bleedingPlayers.get(npcId) == null) return;
    	for (L2PcInstance pc : (FastList<L2PcInstance>)_bleedingPlayers.get(npcId))
    	{
			if (pc.getFirstEffect(EffectType.DMG_OVER_TIME) != null) pc.stopEffects(EffectType.DMG_OVER_TIME);
    	}
    	_bleedingPlayers.remove(npcId);
    }

    private class Bleeding implements Runnable
    {
    	public Bleeding()
    	{
    		
    	}
    	
    	public void run()
    	{
    		addBleeding();
    		
    		if (_setBleedTask != null) _setBleedTask.cancel(true);
    		_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(),2000);

    	}
    }
    
    // High Priestess van Halter dead or time up.
    public void enterInterval()
    {
    	// cancel all task
    	if (_callRoyalGuardHelperTask != null)	_callRoyalGuardHelperTask.cancel(true);
   		_callRoyalGuardHelperTask = null;

   		if (_closeDoorOfAltarTask != null)	_closeDoorOfAltarTask.cancel(true);
   		_closeDoorOfAltarTask = null;

   		if (_halterEscapeTask != null)	_halterEscapeTask.cancel(true);
   		_halterEscapeTask = null;
   		
   		if (_intervalTask != null)	_intervalTask.cancel(true);
   		_intervalTask = null;
   		
    	if (_lockUpDoorOfAltarTask != null) _lockUpDoorOfAltarTask.cancel(true);
   		_lockUpDoorOfAltarTask = null;

   		if (_movieTask != null) _movieTask.cancel(true);
   		_movieTask = null;

   		if (_openDoorOfAltarTask != null) _openDoorOfAltarTask.cancel(true);
   		_openDoorOfAltarTask = null;

   		if (_timeUpTask != null) _timeUpTask.cancel(true);
   		_timeUpTask = null;

    	// delete monsters
   		if (_vanHalter.isDead())
   		{
   	    	_vanHalter.getSpawn().stopRespawn();
   		}
   		else
   		{
   	    	deleteVanHalter();
   		}
    	deleteRoyalGuardHepler();
    	deleteRoyalGuardCaptain();
    	deleteRoyalGuard();
    	deleteRitualOffering();
    	deleteRitualSacrifice();
    	deleteGuardOfAltar();

    	// set interval end.
    	if (_intervalTask != null) _intervalTask.cancel(true);

    	if (!_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
    	{
        	int interval = Rnd.get(Config.HPH_FIXINTERVALOFHALTER,Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER);
        	_state.setRespawnDate(interval);
        	_state.setState(GrandBossState.StateEnum.INTERVAL);
        	_state.update();
    		
    	}
    	
    	_intervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(),_state.getInterval());
    }
    
    // interval.
    private class Interval implements Runnable
    {
    	public Interval()
    	{
    		
    	}
    	
    	public void run()
    	{
    		_playersInLair.clear();
    		setupAlter();
    	}
    }

    // interval end.
    public void setupAlter()
    {
    	// cancel all task
    	if (_callRoyalGuardHelperTask != null)	_callRoyalGuardHelperTask.cancel(true);
   		_callRoyalGuardHelperTask = null;

   		if (_closeDoorOfAltarTask != null)	_closeDoorOfAltarTask.cancel(true);
   		_closeDoorOfAltarTask = null;

   		if (_halterEscapeTask != null)	_halterEscapeTask.cancel(true);
   		_halterEscapeTask = null;
   		
   		if (_intervalTask != null)	_intervalTask.cancel(true);
   		_intervalTask = null;
   		
    	if (_lockUpDoorOfAltarTask != null) _lockUpDoorOfAltarTask.cancel(true);
   		_lockUpDoorOfAltarTask = null;

   		if (_movieTask != null) _movieTask.cancel(true);
   		_movieTask = null;

   		if (_openDoorOfAltarTask != null) _openDoorOfAltarTask.cancel(true);
   		_openDoorOfAltarTask = null;

   		if (_timeUpTask != null) _timeUpTask.cancel(true);
   		_timeUpTask = null;
    	
    	// delete all monsters
    	deleteVanHalter();
    	deleteToriolRevelation();
    	deleteRoyalGuardHepler();
    	deleteRoyalGuardCaptain();
    	deleteRoyalGuard();
    	deleteRitualSacrifice();
    	deleteRitualOffering();
    	deleteGuardOfAltar();
    	deleteCameraMarker();
    	
    	// clear flag.
        _isLocked = false;
        _isCaptainSpawned = false;
        _isHelperCalled = false;
        _isHalterSpawned = false;
    	
    	// set door state
    	closeDoorOfSacrifice();
    	openDoorOfAltar(true);
    	
    	// respawn monsters.
    	spawnToriolRevelation();
    	spawnRoyalGuard();
    	spawnRitualOffering();
    	spawnVanHalter();

    	_state.setState(GrandBossState.StateEnum.NOTSPAWN);
    	_state.update();
    	
    	// set time up.
    	if (_timeUpTask != null) _timeUpTask.cancel(true);
    	_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
    }

    // time up.
    private class TimeUp implements Runnable
    {
    	public TimeUp()
    	{
    		
    	}
    	
    	public void run()
    	{
    		enterInterval();
    	}
    }
    
    // appearance movie.
    private class Movie implements Runnable
    {
    	int _distance = 6502500;
    	int _taskId;
        
        public Movie(int taskId)
        {
        	_taskId = taskId;
        }

        public void run()
        {
        	
    		_vanHalter.setHeading(16384);
    		_vanHalter.setTarget(_ritualOffering);
    		
    		switch(_taskId)
    		{
	    		case 1:
	    	    	_state.setState(GrandBossState.StateEnum.ALIVE);
	    	    	_state.update();

	    	    	// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50,90,0,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
	
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);
	
					break;
	    			
	    		case 2:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1842,100,-3,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);
	
					break;
	    			
	    		case 3:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1861,97,-10,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
		            _movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);
	
					break;

	    		case 4:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1876,97,12,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);
	
					break;
	    			
	    		case 5:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1839,94,0,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);
	
					break;
	    			
	    		case 6:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1872,94,15,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);
	
					break;
	    			
	    		case 7:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1839,92,0,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);
	
					break;
	    			
	    		case 8:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1872,92,15,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);
	
					break;
	    			
	    		case 9:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1839,90,5,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);
	
					break;
	    			
	    		case 10:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 1872,90,5,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);
	
					break;
	    			
	    		case 11:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 2002,90,2,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);
	
					break;
	    			
	    		case 12:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50,90,10,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);
	
					break;
	    			
	    		case 13:
	    			// High Priestess van Halter uses the skill to kill Ritual Offering.
					L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
	    			_ritualOffering.setIsInvul(false);
					_vanHalter.setTarget(_ritualOffering);
	        		_vanHalter.setIsImmobilized(false);
	    			_vanHalter.doCast(skill);
	        		_vanHalter.setIsImmobilized(true);
	    			
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);
	
					break;
	    			
	    		case 14:
	    			_ritualOffering.setIsInvul(false);
	    			_ritualOffering.reduceCurrentHp(_ritualOffering.getMaxHp() * 2, _vanHalter);
	    			
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);
	
					break;
	    			
	    		case 15:
	    			spawnRitualSacrifice();
	    			deleteRitualOffering();
					
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 100,90,15,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);
	
					break;
	    			
	    		case 16:
					// set camera.
					for (L2PcInstance pc : _playersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 5200,90,-10,9500,6000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
		            _movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);
	
					break;

	    		case 17:
					// reset camera.
					for (L2PcInstance pc : _playersInLair)
					{
						pc.leaveMovieMode();
					}
					deleteRitualSacrifice();
					deleteCameraMarker();
			    	_vanHalter.setIsImmobilized(false);
			    	_vanHalter.setIsInvul(false);

		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
			    	_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);
		        	
					break;
				
	    		case 18:
	    			combatBeginning();
		            if(_movieTask != null) _movieTask.cancel(true);
	            	_movieTask = null;
    		}
        }
    }
    
}

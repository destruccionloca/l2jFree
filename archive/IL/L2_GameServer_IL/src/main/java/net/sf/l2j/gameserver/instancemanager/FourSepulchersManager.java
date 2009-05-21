/* This program is free software; you can redistribute it and/or modify
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  sandman
 */
public class FourSepulchersManager
{
    private final static Log _log = LogFactory.getLog(FourSepulchersManager.class.getName());

    private static FourSepulchersManager _instance;

    private static final String QUEST_ID = "620_FourGoblets";
    private static final int ENTRANCE_PASS = 7075;
    private static final int USED_PASS = 7261;
    private static final int CHAPEL_KEY = 7260;
    private static final int ANTIQUE_BROOCH = 7262;

    protected boolean _inEntryTime = false;
    protected boolean _inWarmUpTime = false;
    protected boolean _inAttackTime = false;
    protected boolean _inCoolDownTime = false;

    protected Future _changeCoolDownTimeTask = null;
    protected Future _changeEntryTimeTask = null; 
    protected Future _changeWarmUpTimeTask = null;
    protected Future _changeAttackTimeTask = null;
    protected Future _onPartyAnnihilatedTask = null;
     
    protected static Map<Integer,Integer> _hallGateKeepers = new FastMap<Integer,Integer>();
    
    private int[][] _startHallSpawn =
	{
		{181632,-85587,-7218},
		{179963,-88978,-7218},
		{173217,-86132,-7218},
		{175608,-82296,-7218}
	};
    
    protected static Map<Integer,int[]> _startHallSpawns = new FastMap<Integer,int[]>();

    private int[][][] _shadowSpawnLoc =
	{
		{
			{25339,191231,-85574,-7216,33380},
			{25349,189534,-88969,-7216,32768},
			{25346,173195,-76560,-7215,49277},
			{25342,175591,-72744,-7215,49317}
		},
		{
			{25342,191231,-85574,-7216,33380},
			{25339,189534,-88969,-7216,32768},
			{25349,173195,-76560,-7215,49277},
			{25346,175591,-72744,-7215,49317}
		},
		{
			{25346,191231,-85574,-7216,33380},
			{25342,189534,-88969,-7216,32768},
			{25339,173195,-76560,-7215,49277},
			{25349,175591,-72744,-7215,49317}
		},
		{
			{25349,191231,-85574,-7216,33380},
			{25346,189534,-88969,-7216,32768},
			{25342,173195,-76560,-7215,49277},
			{25339,175591,-72744,-7215,49317}
		},
	};
    
    protected Map<Integer,L2Spawn> _shadowSpawns = new FastMap<Integer,L2Spawn>();
    
    protected static Map<Integer,Boolean> _hallInUse = new FastMap<Integer,Boolean>();
    
    protected Map<Integer,L2PcInstance> _challengers = new FastMap<Integer,L2PcInstance>();
    
    protected Map<Integer,L2Spawn> _mysteriousBoxSpawns = new FastMap<Integer,L2Spawn>();
    
    protected List<L2Spawn> _physicalSpawns;
    protected Map<Integer,List> _physicalMonsters = new FastMap<Integer,List>();
    protected List<L2Spawn> _magicalSpawns;
    protected Map<Integer,List> _magicalMonsters = new FastMap<Integer,List>();
    protected List<L2Spawn> _dukeFinalSpawns;
    protected Map<Integer,List> _dukeFinalMobs = new FastMap<Integer,List>();
    protected Map<Integer,Boolean> _archonSpawned = new FastMap<Integer,Boolean>();
    protected List<L2Spawn> _emperorsGraveSpawns;
    protected Map<Integer,List> _emperorsGraveNpcs = new FastMap<Integer,List>();
    
    protected Map<Integer,List> _viscountMobs =	new FastMap<Integer,List>();
    
    protected Map<Integer,List> _dukeMobs =	new FastMap<Integer,List>();
    
    protected Map<Integer,Integer> _keyBoxNpc = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _keyBoxSpawns = new FastMap<Integer,L2Spawn>();
    
    protected Map<Integer,Integer> _victim = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _executionerSpawns = new FastMap<Integer,L2Spawn>();
    
    protected List<L2NpcInstance> _allMobs = new FastList<L2NpcInstance>();
    protected String _zoneName = "Four Sepulcher";
    
    public FourSepulchersManager() {}

    public static final FourSepulchersManager getInstance()
    {
        if (_instance == null)
            _instance = new FourSepulchersManager();
        return _instance;
    }
    
    public void init()
    {
        if(_changeCoolDownTimeTask != null) _changeCoolDownTimeTask.cancel(true);
        if(_changeEntryTimeTask != null) _changeEntryTimeTask.cancel(true);
        if(_changeWarmUpTimeTask != null) _changeWarmUpTimeTask.cancel(true);
        if(_changeAttackTimeTask != null) _changeAttackTimeTask.cancel(true);

        _changeCoolDownTimeTask = null;
        _changeEntryTimeTask = null; 
        _changeWarmUpTimeTask = null;
        _changeAttackTimeTask = null;
        
        _inEntryTime = false;
        _inWarmUpTime = false;
        _inAttackTime = false;
        _inCoolDownTime = false;

        initFixedInfo();
        
        loadMysteriousBox();
        initKeyBoxSpawns();
        loadPhysicalMonsters();
        loadMagicalMonsters();
        initLocationShadowSpawns();
        initExecutionerSpawns();
        loadDukeMonsters();
        loadEmperorsGraveMonsters();
        
        _changeCoolDownTimeTask =
            ThreadPoolManager.getInstance().scheduleEffect(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);
        
    }

    protected void initFixedInfo()
    {
        _startHallSpawns.clear();
        _startHallSpawns.put(31921,_startHallSpawn[0]);
        _startHallSpawns.put(31922,_startHallSpawn[1]);
        _startHallSpawns.put(31923,_startHallSpawn[2]);
        _startHallSpawns.put(31924,_startHallSpawn[3]);
        
        _hallGateKeepers.clear();
        _hallGateKeepers.put(31925, 25150012);
        _hallGateKeepers.put(31926, 25150013);
        _hallGateKeepers.put(31927, 25150014);
        _hallGateKeepers.put(31928, 25150015);
        _hallGateKeepers.put(31929, 25150016);
        _hallGateKeepers.put(31930, 25150002);
        _hallGateKeepers.put(31931, 25150003);
        _hallGateKeepers.put(31932, 25150004);
        _hallGateKeepers.put(31933, 25150005);
        _hallGateKeepers.put(31934, 25150006);
        _hallGateKeepers.put(31935, 25150032);
        _hallGateKeepers.put(31936, 25150033);
        _hallGateKeepers.put(31937, 25150034);
        _hallGateKeepers.put(31938, 25150035);
        _hallGateKeepers.put(31939, 25150036);
        _hallGateKeepers.put(31940, 25150022);
        _hallGateKeepers.put(31941, 25150023);
        _hallGateKeepers.put(31942, 25150024);
        _hallGateKeepers.put(31943, 25150025);
        _hallGateKeepers.put(31944, 25150026);
        
        _keyBoxNpc.clear();
        _keyBoxNpc.put(18120,31455);
        _keyBoxNpc.put(18121,31455);
        _keyBoxNpc.put(18122,31455);
        _keyBoxNpc.put(18123,31455);
        _keyBoxNpc.put(18124,31456);
        _keyBoxNpc.put(18125,31456);
        _keyBoxNpc.put(18126,31456);
        _keyBoxNpc.put(18127,31456);
        _keyBoxNpc.put(18128,31457);
        _keyBoxNpc.put(18129,31457);
        _keyBoxNpc.put(18130,31457);
        _keyBoxNpc.put(18131,31457);
        _keyBoxNpc.put(18149,31458);
        _keyBoxNpc.put(18150,31459);
        _keyBoxNpc.put(18151,31459);
        _keyBoxNpc.put(18152,31459);
        _keyBoxNpc.put(18153,31459);
        _keyBoxNpc.put(18154,31460);
        _keyBoxNpc.put(18155,31460);
        _keyBoxNpc.put(18156,31460);
        _keyBoxNpc.put(18157,31460);
        _keyBoxNpc.put(18158,31461);
        _keyBoxNpc.put(18159,31461);
        _keyBoxNpc.put(18160,31461);
        _keyBoxNpc.put(18161,31461);
        _keyBoxNpc.put(18162,31462);
        _keyBoxNpc.put(18163,31462);
        _keyBoxNpc.put(18164,31462);
        _keyBoxNpc.put(18165,31462);
        _keyBoxNpc.put(18183,31463);
        _keyBoxNpc.put(18184,31464);
        _keyBoxNpc.put(18212,31465);
        _keyBoxNpc.put(18213,31465);
        _keyBoxNpc.put(18214,31465);
        _keyBoxNpc.put(18215,31465);
        _keyBoxNpc.put(18216,31466);
        _keyBoxNpc.put(18217,31466);
        _keyBoxNpc.put(18218,31466);
        _keyBoxNpc.put(18219,31466);
     
        _victim.clear();
        _victim.put(18150,18158);
        _victim.put(18151,18159);
        _victim.put(18152,18160);
        _victim.put(18153,18161);
        _victim.put(18154,18162);
        _victim.put(18155,18163);
        _victim.put(18156,18164);
        _victim.put(18157,18165);

    }

    private void loadMysteriousBox()
    {
        java.sql.Connection con = null;
        
        _mysteriousBoxSpawns.clear();

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id");
            statement.setInt(1, 0);
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
                	int keyNpcId = rset.getInt("key_npc_id");
                    _mysteriousBoxSpawns.put(keyNpcId,spawnDat);
                }
                else {
                    _log.warn("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("FourSepulchersManager: loaded " + _mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void initKeyBoxSpawns()
    {
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        for(int keyNpcId:_keyBoxNpc.keySet())
        {
            try
            {
                template = NpcTable.getInstance().getTemplate(_keyBoxNpc.get(keyNpcId));
                if (template != null)
                {
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(0);
                	spawnDat.setLocy(0);
                	spawnDat.setLocz(0);
                	spawnDat.setHeading(0);
                	spawnDat.setRespawnDelay(3600);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_keyBoxSpawns.put(keyNpcId, spawnDat);
                }
                else {
                    _log.warn("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + _keyBoxNpc.get(keyNpcId) + ".");
                }
            }
            catch (Exception e)
            {
                _log.warn("FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e);
            }
        }
    }
    
    private void loadPhysicalMonsters()
    {
    	_physicalMonsters.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 1);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 1);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _physicalSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_physicalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warn("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
            	_physicalMonsters.put(keyNpcId,_physicalSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager: loaded " + loaded + " Physical type monsters spawns.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void loadMagicalMonsters()
    {

    	_magicalMonsters.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 2);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 2);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _magicalSpawns = new FastList<L2Spawn>();            	

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_magicalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warn("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _magicalMonsters.put(keyNpcId,_magicalSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager: loaded " + loaded + " Magical type monsters spawns.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void loadDukeMonsters()
    {
    	_dukeFinalMobs.clear();
    	_archonSpawned.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 5);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 5);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _dukeFinalSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_dukeFinalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warn("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _dukeFinalMobs.put(keyNpcId,_dukeFinalSpawns);
                _archonSpawned.put(keyNpcId, false);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager: loaded " + loaded + " Church of duke monsters spawns.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void loadEmperorsGraveMonsters()
    {

    	_emperorsGraveNpcs.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 6);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 6);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _emperorsGraveSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_emperorsGraveSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warn("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _emperorsGraveNpcs.put(keyNpcId,_emperorsGraveSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager: loaded " + loaded + " Emperor's grave NPC spawns.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    protected void initLocationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	final int[] gateKeeper = {31929,31934,31939,31944};
    	
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        _shadowSpawns.clear();

        for(int i=0;i<=3;i++)
    	{
            template = NpcTable.getInstance().getTemplate(_shadowSpawnLoc[locNo][i][0]);
            if (template != null)
            {
            	try
            	{
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(_shadowSpawnLoc[locNo][i][1]);
                	spawnDat.setLocy(_shadowSpawnLoc[locNo][i][2]);
                	spawnDat.setLocz(_shadowSpawnLoc[locNo][i][3]);
                	spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	int keyNpcId = gateKeeper[i];
                	_shadowSpawns.put(keyNpcId,spawnDat);
            	}
            	catch(Exception e)
            	{
            		_log.warn(e.getMessage());
            		e.printStackTrace();
            	}
            }
            else {
                _log.warn("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + _shadowSpawnLoc[locNo][i][0] + ".");
            }
    	}
    }

    protected void initExecutionerSpawns()
    {
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        for(int keyNpcId:_victim.keySet())
        {
            try
            {
                template = NpcTable.getInstance().getTemplate(_victim.get(keyNpcId));
                if (template != null)
                {
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(0);
                	spawnDat.setLocy(0);
                	spawnDat.setLocz(0);
                	spawnDat.setHeading(0);
                	spawnDat.setRespawnDelay(3600);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_executionerSpawns.put(keyNpcId, spawnDat);
                }
                else {
                    _log.warn("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + _victim.get(keyNpcId) + ".");
                }
            }
            catch (Exception e)
            {
                _log.warn("FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e);
            }
        }
    }
    
    public boolean isEntryTime()
    {
    	return _inEntryTime;
    }
    
    public boolean isAttackTime()
    {
    	return _inAttackTime;
    }
    
	public synchronized void tryEntry(L2NpcInstance npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			// ID ok
			case 31921: case 31922: case 31923: case 31924:
				break;
			// ID not ok
			default:
				if (!player.isGM())
				{
					_log.warn("Player " + player.getName() + "(" + player.getObjectId() + ") tried to cheat in four sepulchers.");
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName()
						+ " tried to enter four sepulchers with invalid npc id.", Config.DEFAULT_PUNISH);
				}
				return;
		}

		if(!isEntryTime())
		{
			showHtmlFile(player, npcId+"-NE.htm", npc, null);
			return;
		}

		if(_hallInUse.get(npcId).booleanValue())
		{
			showHtmlFile(player, npcId+"-FULL.htm", npc, null);
			return;
		}

		if(Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			if(!player.isInParty() || player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT)
			{
				showHtmlFile(player, npcId+"-SP.htm", npc, null);
				return;
			}

			if(!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId+"-NL.htm", npc, null);
				return;
			}
			
			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				// FIXME: After accepting the quest, a player must relog
				if(mem.getQuestState(QUEST_ID).get("<state>") == null)
				{
					showHtmlFile(player, npcId+"-NS.htm", npc, mem);
					return;
				}
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId+"-SE.htm", npc, mem);
					return;
				}

				if(player.getWeightPenalty() >= 3)
				{
					mem.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
			}
		}
		else
		{
			// FIXME: After accepting the quest, a player must relog
			if(player.getQuestState(QUEST_ID).get("<state>") == null)
			{
				showHtmlFile(player, npcId+"-NS.htm", npc, player);
				return;
			}
			if(player.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId+"-SE.htm", npc, player);
				return;
			}

			if(player.getWeightPenalty() >= 3)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
				return;
			}
		}

		showHtmlFile(player, npcId+"-OK.htm", npc, null);
		
		entry(npcId, player);
	}

	private void entry(int npcId, L2PcInstance player)
	{
		int[] Location = _startHallSpawns.get(npcId);
		int driftx;
		int drifty;

		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			List<L2PcInstance> members = new FastList<L2PcInstance>();
			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}

			for (L2PcInstance mem : members)
			{
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem,true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}

				L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if(hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}

			_challengers.remove(npcId);
			_challengers.put(npcId, player);

			_hallInUse.remove(npcId);
			_hallInUse.put(npcId, true);
		}
		else
		{
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
			player.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, player, true);
			if (player.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
			{
				player.addItem("Quest", USED_PASS, 1, player, true);
			}

			L2ItemInstance hallsKey = player.getInventory().getItemByItemId(CHAPEL_KEY);
			if(hallsKey != null)
			{
				player.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), player, true);
			}

			_challengers.remove(npcId);
			_challengers.put(npcId, player);

			_hallInUse.remove(npcId);
			_hallInUse.put(npcId, true);
		}
	}

    public void spawnMysteriousBox(int npcId)
    {
    	if (!isAttackTime()) return;
    	
    	L2Spawn spawnDat = _mysteriousBoxSpawns.get(npcId);
    	if(spawnDat != null)
    	{
        	_allMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    	}
    }

    public void spawnMonster(int npcId)
    {
    	if (!isAttackTime()) return;

    	FastList<L2Spawn> monsterList;
    	List<L2SepulcherMonsterInstance> mobs = new FastList<L2SepulcherMonsterInstance>();
    	L2Spawn keyBoxMobSpawn;
    	
    	if(Rnd.get(2) == 0)
    	{
    		monsterList = (FastList<L2Spawn>)_physicalMonsters.get(npcId);
    	}
    	else
    	{
    		monsterList = (FastList<L2Spawn>)_magicalMonsters.get(npcId);
    	}
    	
    	if(monsterList != null)
    	{
    		boolean spawnKeyBoxMob = false;
    		boolean spawnedKeyBoxMob = false;
    		
        	for (L2Spawn spawnDat:monsterList)
        	{
        		if(spawnedKeyBoxMob)
        		{
        			spawnKeyBoxMob = false;
        		}
        		else
        		{
            		switch(npcId)
            		{
            		    case 31469:
            		    case 31474:
            		    case 31479:
            		    case 31484:
            		    	if(Rnd.get(48) == 0)
            		    	{
            		    		spawnKeyBoxMob = true;
            		    		//_log.info("FourSepulchersManager.SpawnMonster: Set to spawn Church of Viscount Key Mob.");
            		    	}
            		    	break;
        		    	default:
        		    		spawnKeyBoxMob = false;
            		}
        		}

        		L2SepulcherMonsterInstance mob = null;

        		if(spawnKeyBoxMob)
        		{
                    try
                    {
            			L2NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
                        if (template != null)
                        {
                        	keyBoxMobSpawn = new L2Spawn(template);
                        	keyBoxMobSpawn.setAmount(1);
                        	keyBoxMobSpawn.setLocx(spawnDat.getLocx());
                        	keyBoxMobSpawn.setLocy(spawnDat.getLocy());
                        	keyBoxMobSpawn.setLocz(spawnDat.getLocz());
                        	keyBoxMobSpawn.setHeading(spawnDat.getHeading());
                        	keyBoxMobSpawn.setRespawnDelay(3600);
                        	SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
                    		mob = (L2SepulcherMonsterInstance)keyBoxMobSpawn.doSpawn();
                    		keyBoxMobSpawn.stopRespawn();
                        }
                        else {
                            _log.warn("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
                        }
                    }
                    catch (Exception e)
                    {
                        _log.warn("FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e);
                    }
        			
        			spawnedKeyBoxMob = true;
        		}
        		else
        		{
            		mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
                	spawnDat.stopRespawn();
        		}

        		if(mob != null)
        		{
            		mob.mysteriousBoxId = npcId;
            		switch(npcId)
            		{
            		    case 31469:
            		    case 31474:
            		    case 31479:
            		    case 31484:
            		    case 31472:
            		    case 31477:
            		    case 31482:
            		    case 31487:
            		    	mobs.add(mob);
            		}
            		_allMobs.add(mob);
        		}
        	}

        	switch(npcId)
    		{
    		    case 31469:
    		    case 31474:
    		    case 31479:
    		    case 31484:
    		    	_viscountMobs.put(npcId, mobs);
    		    	break;
    		    
    		    case 31472:
    		    case 31477:
    		    case 31482:
    		    case 31487:
    		    	_dukeMobs.put(npcId, mobs);
    		    	break;
    		}
    	}
    }

    public synchronized boolean isViscountMobsAnnihilated(int npcId)
    {
    	FastList<L2SepulcherMonsterInstance> mobs =
    		(FastList<L2SepulcherMonsterInstance>)_viscountMobs.get(npcId);
    	
    	if(mobs == null) return true;
    	
    	for(L2SepulcherMonsterInstance mob:mobs)
    	{
    		if(!mob.isDead()) return false;
    	}
    	
    	return true;
    }

    public synchronized boolean isDukeMobsAnnihilated(int npcId)
    {
    	FastList<L2SepulcherMonsterInstance> mobs =
    		(FastList<L2SepulcherMonsterInstance>)_dukeMobs.get(npcId);
    	
    	if(mobs == null) return true;
    	
    	for(L2SepulcherMonsterInstance mob:mobs)
    	{
    		if(!mob.isDead()) return false;
    	}
    	
    	return true;
    }

    public void spawnKeyBox(L2NpcInstance activeChar)
    {
    	if (!isAttackTime()) return;

    	L2Spawn	spawnDat = _keyBoxSpawns.get(activeChar.getNpcId());

    	if(spawnDat != null)
    	{
        	spawnDat.setAmount(1);
        	spawnDat.setLocx(activeChar.getX());
        	spawnDat.setLocy(activeChar.getY());
        	spawnDat.setLocz(activeChar.getZ());
        	spawnDat.setHeading(activeChar.getHeading());
        	spawnDat.setRespawnDelay(3600);
        	_allMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    		
    	}
    }
    
    public void spawnExecutionerOfHalisha(L2NpcInstance activeChar)
    {
    	if (!isAttackTime()) return;

    	L2Spawn spawnDat = _executionerSpawns.get(activeChar.getNpcId());

    	if(spawnDat != null)
    	{
    		spawnDat.setAmount(1);
        	spawnDat.setLocx(activeChar.getX());
        	spawnDat.setLocy(activeChar.getY());
        	spawnDat.setLocz(activeChar.getZ());
        	spawnDat.setHeading(activeChar.getHeading());
        	spawnDat.setRespawnDelay(3600);
        	_allMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    	}
    }

    public void spawnArchonOfHalisha(int npcId)
    {
    	if (!isAttackTime()) return;
    	
    	if(_archonSpawned.get(npcId)) return;

    	FastList<L2Spawn> monsterList = (FastList<L2Spawn>)_dukeFinalMobs.get(npcId);
    	
    	if(monsterList != null)
    	{
        	for (L2Spawn spawnDat:monsterList)
        	{
        		L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
            	spawnDat.stopRespawn();

        		if(mob != null)
        		{
            		mob.mysteriousBoxId = npcId;
            		_allMobs.add(mob);
        		}
    		}
        	_archonSpawned.put(npcId, true);
    	}
    }

    public void spawnEmperorsGraveNpc(int npcId)
    {
    	if (!isAttackTime()) return;
    	
    	FastList<L2Spawn> monsterList = (FastList<L2Spawn>)_emperorsGraveNpcs.get(npcId);
    	
    	if(monsterList != null)
    	{
        	for (L2Spawn spawnDat:monsterList)
        	{
        		_allMobs.add(spawnDat.doSpawn());
            	spawnDat.stopRespawn();
    		}
    	}
    }

    protected void locationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	//_log.info("FourSepulchersManager.LocationShadowSpawns: Location index is " + locNo + ".");
    	final int[] gateKeeper = {31929,31934,31939,31944};
    	
    	L2Spawn spawnDat;

        for(int i=0;i<=3;i++)
    	{
        	int keyNpcId = gateKeeper[i];
        	spawnDat = _shadowSpawns.get(keyNpcId);
        	spawnDat.setLocx(_shadowSpawnLoc[locNo][i][1]);
        	spawnDat.setLocy(_shadowSpawnLoc[locNo][i][2]);
        	spawnDat.setLocz(_shadowSpawnLoc[locNo][i][3]);
        	spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
        	_shadowSpawns.put(keyNpcId,spawnDat);
    	}
    }

    public void spawnShadow(int npcId)
    {
    	if (!isAttackTime()) return;

    	L2Spawn spawnDat = _shadowSpawns.get(npcId);
    	if(spawnDat != null)
    	{
    		L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
        	spawnDat.stopRespawn();

    		if(mob != null)
    		{
        		mob.mysteriousBoxId = npcId;
        		_allMobs.add(mob);
    		}
    	}
    }

    public void checkAnnihilated(L2PcInstance player)
    {
    	if(isPartyAnnihilated(player))
    	{
    		_onPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleEffect(new OnPartyAnnihilatedTask(player),5000);    			
    	}
    }

    public synchronized boolean isPartyAnnihilated(L2PcInstance player)
    {
		if(player.getParty() != null)
		{
			for(L2PcInstance mem:player.getParty().getPartyMembers())
			{
				if(!mem.isDead())
				{
					return false;
				}
			}
			return true;
		}
		return true;
    }
    
    public synchronized void onPartyAnnihilated(L2PcInstance player)
    {
		if(player.getParty() != null)
		{
			for(L2PcInstance mem:player.getParty().getPartyMembers())
			{
				if(!mem.isDead()) break;
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		mem.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
			}
		}
		else
		{
    		int driftX = Rnd.get(-80,80);
    		int driftY = Rnd.get(-80,80);
    		player.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
    	}
    }
    
    public void deleteAllMobs()
    {
    	//_log.info("FourSepulchersManager.DeleteAllMobs: Try to delete " + _allMobs.size() + " monsters.");

    	int delCnt = 0;
        for(L2NpcInstance mob : _allMobs)
        {
			try
            {
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
                delCnt++;
            }
            catch(Exception e)
            {
                _log.warn(e.getMessage());
            }
        }
        _allMobs.clear();
    	//_log.info("FourSepulchersManager.DeleteAllMobs: Deleted " + delCnt + " monsters.");
    }
    
    protected void closeAllDoors()
    {
    	for(int doorId:_hallGateKeepers.values())
    	{
            try
            {
            	DoorTable.getInstance().getDoor(doorId).closeMe();
            }
            catch (Exception e)
            {
                _log.warn(e.getMessage());
            }
    	}
    }

    protected class ChangeEntryTime implements Runnable
    {
        public void run()
        {
            //_log.info("FourSepulchersManager:In Entry Time");
            _inEntryTime = true;
            _inWarmUpTime = false;
            _inAttackTime = false;
            _inCoolDownTime = false;
            
            _changeWarmUpTimeTask =
                ThreadPoolManager.getInstance().scheduleEffect(new ChangeWarmUpTime(),Config.FS_TIME_ENTRY * 60000);

            if(_changeEntryTimeTask != null)
            {
            	_changeEntryTimeTask.cancel(true);
            	_changeEntryTimeTask = null;
            }
        }
    }
    
    protected class ChangeWarmUpTime implements Runnable
    {
        public void run()
        {
            //_log.info("FourSepulchersManager:In Warm-Up Time");
            _inEntryTime = true;
            _inWarmUpTime = false;
            _inAttackTime = false;
            _inCoolDownTime = false;
            
            _changeAttackTimeTask =
                ThreadPoolManager.getInstance().scheduleEffect(new ChangeAttackTime(),Config.FS_TIME_WARMUP * 60000);

            if(_changeWarmUpTimeTask != null)
            {
            	_changeWarmUpTimeTask.cancel(true);
            	_changeWarmUpTimeTask = null;
            }
        }
    }
    
    protected class ChangeAttackTime implements Runnable
    {
        public void run()
        {
            //_log.info("FourSepulchersManager:In Attack Time");
            _inEntryTime = false;
            _inWarmUpTime = false;
            _inAttackTime = true;
            _inCoolDownTime = false;
            
            locationShadowSpawns();
            
            spawnMysteriousBox(31921);
            spawnMysteriousBox(31922);
            spawnMysteriousBox(31923);
            spawnMysteriousBox(31924);
            
            _changeCoolDownTimeTask =
                ThreadPoolManager.getInstance().scheduleEffect(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);

            if(_changeAttackTimeTask != null)
            {
            	_changeAttackTimeTask.cancel(true);
            	_changeAttackTimeTask = null;
            }
        }
    }

    protected class ChangeCoolDownTime implements Runnable
    {
        
        public void run()
        {
            //_log.info("FourSepulchersManager:In Cool-Down Time");
            _inEntryTime = false;
            _inWarmUpTime = false;
            _inAttackTime = false;
            _inCoolDownTime = true;
            
            
            for(L2PcInstance player :L2World.getInstance().getAllPlayers())
            {
            	if ( checkIfInDungeon(player) &&
            		(player.getZ() >= -7250 && player.getZ() <= -6841) &&
            		!player.isGM())
            	{
            		int driftX = Rnd.get(-80,80);
            		int driftY = Rnd.get(-80,80);
            		player.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
            	}
            }
            
            deleteAllMobs();

            closeAllDoors();
            
            _hallInUse.clear();
            _hallInUse.put(31921,false);
            _hallInUse.put(31922,false);
            _hallInUse.put(31923,false);
            _hallInUse.put(31924,false);

            if(_archonSpawned.size() != 0)
            {
                Set<Integer> npcIdSet = _archonSpawned.keySet();
                for(int npcId:npcIdSet)
                {
                	_archonSpawned.put(npcId, false);
                }
            }
            
            _changeEntryTimeTask = 
                ThreadPoolManager.getInstance().scheduleEffect(new ChangeEntryTime(),Config.FS_TIME_COOLDOWN * 60000);

            if(_changeCoolDownTimeTask != null)
            {
            	_changeCoolDownTimeTask.cancel(true);
            	_changeCoolDownTimeTask = null;
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
			onPartyAnnihilated(_player);
			if(_onPartyAnnihilatedTask != null)
			{
				_onPartyAnnihilatedTask.cancel(true);
				_onPartyAnnihilatedTask = null;
			}
		}
	}
	
	public boolean checkIfInDungeon(L2Object obj)
	{
		return ZoneManager.getInstance().checkIfInZone(ZoneType.BossDungeon, _zoneName, obj);
	}

	public Map<Integer,Integer> getHallGateKeepers()
	{
		return _hallGateKeepers;
	}

	public void showHtmlFile(L2PcInstance player, String file, L2NpcInstance npc, L2PcInstance member)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/SepulcherNpc/"+file);
		if(member != null)
			html.replace("%member%", member.getName());
		player.sendPacket(html);
	}
}

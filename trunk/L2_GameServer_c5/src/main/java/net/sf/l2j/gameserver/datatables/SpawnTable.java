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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
    private final static Log _log = LogFactory.getLog(SpawnTable.class.getName());

    private static final SpawnTable _instance = new SpawnTable();

    private FastMap<Integer, L2Spawn> _spawntable = new FastMap<Integer, L2Spawn>();
    private int _npcSpawnCount;
    private int _cSpawnCount;
    private int _highestDbId;
    private int _highestCustomDbId;

    public static SpawnTable getInstance()
    {
        return _instance;
    }

    private SpawnTable()
    {
        if (!Config.ALT_DEV_NO_SPAWNS)
            fillSpawnTable();
        else
            _log.debug("Spawns Disabled");
    }

    public FastMap<Integer, L2Spawn> getSpawnTable()
    {
        return _spawntable;
    }

    private void fillSpawnTable()
    {
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id");
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                    if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
                    {
                        // Don't spawn siege guards
                    }
                    else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
                    {
                        // Don't spawn raidbosses
                    }
                    else if (!Config.SPAWN_CLASS_MASTER && template1.type.equals("L2ClassMaster"))
                    {
                        // Dont' spawn class masters
                    }
                    else if (!Config.SPAWN_WYVERN_MANAGER && template1.type.equals("L2WyvernManager"))
                    {
                        // Dont' spawn wyvern managers
                    }
                    else
                    {
                        spawnDat = new L2Spawn(template1);
                        spawnDat.setId(_npcSpawnCount);
                        spawnDat.setDbId(rset.getInt("id"));
                        spawnDat.setAmount(rset.getInt("count"));
                        spawnDat.setLocx(rset.getInt("locx"));
                        spawnDat.setLocy(rset.getInt("locy"));
                        spawnDat.setLocz(rset.getInt("locz"));
                        spawnDat.setHeading(rset.getInt("heading"));
                        spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                        int loc_id = rset.getInt("loc_id");
                        spawnDat.setLocation(loc_id);                             
                        
                        switch(rset.getInt("periodOfDay")) {
                            case 0: // default
                                _npcSpawnCount += spawnDat.init();
                                break;
                            case 1: // Day
                                DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                                _npcSpawnCount++;
                                break;
                            case 2: // Night
                                DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                                _npcSpawnCount++;
                                break;     
                        }
                        
                        if (spawnDat.getDbId()>_highestDbId)_highestDbId=spawnDat.getDbId();
                        _spawntable.put(spawnDat.getId(), spawnDat);
                    }
                }
                else
                {
                    _log.warn("SpawnTable: Data missing in NPC/Custom NPC table for ID: "
                        + rset.getInt("npc_templateid") + ".");
                }
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("SpawnTable: Spawn could not be initialized: " + e);
        }
        finally
        {
            try
            {
                con.close();
                con=null;
            }
            catch (Exception e)
            {
            }
        }

        _log.info("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations.");

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist ORDER BY id");
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;
            
            _cSpawnCount = _spawntable.size();
            
            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                    if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
                    {
                        // Don't spawn siege guards
                    }
                    else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
                    {
                        // Don't spawn raidbosses
                    }
                    else if (!Config.SPAWN_CLASS_MASTER && template1.type.equals("L2ClassMaster"))
                    {
                        // Dont' spawn class masters
                    }
                    else
                    {
                        spawnDat = new L2Spawn(template1);
                        spawnDat.setId(_npcSpawnCount);
                        spawnDat.setDbId(rset.getInt("id"));
                        spawnDat.setAmount(rset.getInt("count"));
                        spawnDat.setLocx(rset.getInt("locx"));
                        spawnDat.setLocy(rset.getInt("locy"));
                        spawnDat.setLocz(rset.getInt("locz"));
                        spawnDat.setHeading(rset.getInt("heading"));
                        spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                        spawnDat.setCustom();
                        int loc_id = rset.getInt("loc_id");
                        spawnDat.setLocation(loc_id);                             
                        
                        switch(rset.getInt("periodOfDay")) {
                            case 0: // default
                                _npcSpawnCount += spawnDat.init();
                                break;
                            case 1: // Day
                                DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                                _npcSpawnCount++;
                                break;
                            case 2: // Night
                                DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                                _npcSpawnCount++;
                                break;     
                        }
                        
                        if (spawnDat.getDbId()>_highestCustomDbId)_highestCustomDbId=spawnDat.getDbId();
                         _spawntable.put(spawnDat.getId(), spawnDat);
                    }
                }
                else
                {
                    _log.warn("SpawnTable: Data missing in NPC/Custom NPC table for ID: "
                        + rset.getInt("npc_templateid") + ".");
                }
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warn("SpawnTable: Custom spawn could not be initialized: " + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
        _cSpawnCount =  _spawntable.size() - _cSpawnCount;
        if (_cSpawnCount>0)
        _log.info("SpawnTable: Loaded " + _cSpawnCount + " Custom Spawn Locations.");

        if (_log.isDebugEnabled())
            _log.debug("SpawnTable: Spawning completed, total number of NPCs in the world: "
                + _npcSpawnCount);
        
    }

    public FastMap<Integer, L2Spawn> getAllTemplates()
    {
        return _spawntable;
    }

    public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
    {
       _npcSpawnCount++;
       if (spawn.isCustom())
       {
           _highestCustomDbId++;
           spawn.setDbId(_highestCustomDbId);
       }
       else        
       {
           _highestDbId++;
           spawn.setDbId(_highestDbId);
       }
       
       spawn.setId(_npcSpawnCount);
           
       _spawntable.put(spawn.getId(), spawn);

       if (storeInDb)
       {
            java.sql.Connection con = null;

            try
            {
                con = L2DatabaseFactory.getInstance().getConnection(con);
                PreparedStatement statement = con.prepareStatement("INSERT INTO "+(spawn.isCustom()?"custom_spawnlist":"spawnlist")+" (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
                statement.setInt(1, spawn.getDbId());
                statement.setInt(2, spawn.getAmount());
                statement.setInt(3, spawn.getNpcid());
                statement.setInt(4, spawn.getLocx());
                statement.setInt(5, spawn.getLocy());
                statement.setInt(6, spawn.getLocz());
                statement.setInt(7, spawn.getHeading());
                statement.setInt(8, spawn.getRespawnDelay() / 1000);
                statement.setInt(9, spawn.getLocation());
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                // problem with storing spawn
                _log.warn("SpawnTable: Could not store spawn in the DB:" + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }
       }
    }

    public void deleteSpawn(L2Spawn spawn, boolean updateDb)
    {
        if (_spawntable.remove(spawn.getId()) == null) return;

        if (updateDb)
        {
            java.sql.Connection con = null;

            try
            {
                con = L2DatabaseFactory.getInstance().getConnection(con);
                PreparedStatement statement = con.prepareStatement("DELETE FROM "+(spawn.isCustom()?"custom_spawnlist":"spawnlist")+" WHERE id=?");
                statement.setInt(1, spawn.getDbId());
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                // problem with deleting spawn
                _log.warn("SpawnTable: Spawn " + spawn.getDbId() + " could not be removed from DB: "
                    + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    //just wrapper
    public void reloadAll()
    { 
        fillSpawnTable();
    }

    public void cleanUp()
    { 
    	_spawntable.clear();
    }
    
    public L2Spawn getTemplate(int id)
    {
        return _spawntable.get(id);
    }
}

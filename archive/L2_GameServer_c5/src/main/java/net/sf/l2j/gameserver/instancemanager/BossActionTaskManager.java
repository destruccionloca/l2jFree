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
 @author sandman
 **/

package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * 
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  sandman
 */
public class BossActionTaskManager
{
    protected static Logger _log = Logger.getLogger(BossActionTaskManager.class.getName());

    protected static final int _ActivityTimeOfBoss = Config.ACTIVITY_TIME_OF_BOSS;
    protected static final int _CapacityOfLairOfValakas = Config.CAPACITY_OF_LAIR_OF_VALAKAS;
    protected static final int _AppTimeOfAntharas = Config.APPTIME_OF_ANTHARAS;
    protected static final int _AppTimeOfValakas = Config.APPTIME_OF_VALAKAS;


    private final int _angellocation[][] = 
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
    protected List<L2NpcInstance> _angels = new FastList<L2NpcInstance>();

    private final int _AntharasCubeLocation[][] =
    	{
    		{177615, 114941, -7709,0}
    	};
    protected List<L2Spawn> _AntharasCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _AntharasCube = new FastList<L2NpcInstance>();
 
    private final int _ValakasCubeLocation[][] =
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
    protected List<L2Spawn> _ValakasCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _ValakasCube = new FastList<L2NpcInstance>();
    
    protected List<L2PcInstance> _playersInAntharasLair = new FastList<L2PcInstance>();
    protected List<L2PcInstance> _playersInBaiumLair = new FastList<L2PcInstance>();
    protected List<L2PcInstance> _playersInValakasLair = new FastList<L2PcInstance>();

    protected L2Spawn _AntharasSpawn;
    protected L2Spawn _BaiumSpawn;
    protected L2Spawn _ValakasSpawn;

    protected L2NpcInstance _npcbaium;

    protected static boolean _IsAntharasSpawned = false;
    protected static boolean _IsValakasSpawned = false;

    protected Future _AntharasSpawnTask = null;
    protected Future _VarakasSpawnTask = null;
    protected Future _CubeSpawnTask = null;

    private static BossActionTaskManager _instance = new BossActionTaskManager();

    public BossActionTaskManager()
    {
    }

    public static BossActionTaskManager getInstance()
    {
        if (_instance == null) _instance = new BossActionTaskManager();

        return _instance;
    }

    public void init()
    {
        try
        {
            L2NpcTemplate template1;
            
            template1 = NpcTable.getInstance().getTemplate(29019); //Antharas
            _AntharasSpawn = new L2Spawn(template1);
            _AntharasSpawn.setLocx(181323);
            _AntharasSpawn.setLocy(114850);
            _AntharasSpawn.setLocz(-7623);
            _AntharasSpawn.setHeading(32542);
            _AntharasSpawn.setAmount(1);
            _AntharasSpawn.setRespawnDelay(_ActivityTimeOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(_AntharasSpawn, false);

            template1 = NpcTable.getInstance().getTemplate(29020); //Baium
            _BaiumSpawn = new L2Spawn(template1);
            _BaiumSpawn.setAmount(1);
            _BaiumSpawn.setRespawnDelay(_ActivityTimeOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(_BaiumSpawn, false);
            
            template1 = NpcTable.getInstance().getTemplate(29028); //Valakas
            _ValakasSpawn = new L2Spawn(template1);
            _ValakasSpawn.setLocx(213168);
            _ValakasSpawn.setLocy(-114578);
            _ValakasSpawn.setLocz(-1635);
            _ValakasSpawn.setHeading(22106);
            _ValakasSpawn.setAmount(1);
            _ValakasSpawn.setRespawnDelay(_ActivityTimeOfBoss * 2);
            SpawnTable.getInstance().addNewSpawn(_ValakasSpawn, false);
            
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        try
        {
            L2NpcTemplate Angel = NpcTable.getInstance().getTemplate(29021);
            L2Spawn spawnDat;
            _angelSpawn.clear();
            _angelSpawn1.clear();
            _angelSpawn2.clear();

            for (int i = 0; i < 10; i = i + 2)
            {
                spawnDat = new L2Spawn(Angel);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_angellocation[i][0]);
                spawnDat.setLocy(_angellocation[i][1]);
                spawnDat.setLocz(_angellocation[i][2]);
                spawnDat.setHeading(_angellocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _angelSpawn1.add(spawnDat);
            }
            _angelSpawn.put(0, _angelSpawn1);

            for (int i = 1; i < 10; i = i + 2)
            {
                spawnDat = new L2Spawn(Angel);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_angellocation[i][0]);
                spawnDat.setLocy(_angellocation[i][1]);
                spawnDat.setLocz(_angellocation[i][2]);
                spawnDat.setHeading(_angellocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _angelSpawn2.add(spawnDat);
            }
            _angelSpawn.put(1, _angelSpawn1);
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(31859);
            L2Spawn spawnDat;
        	
            for(int i = 0;i < _AntharasCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_AntharasCubeLocation[i][0]);
                spawnDat.setLocy(_AntharasCubeLocation[i][1]);
                spawnDat.setLocz(_AntharasCubeLocation[i][2]);
                spawnDat.setHeading(_AntharasCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _AntharasCubeSpawn.add(spawnDat);
            }

            Cube = NpcTable.getInstance().getTemplate(31759);
            for(int i = 0;i < _ValakasCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_ValakasCubeLocation[i][0]);
                spawnDat.setLocy(_ValakasCubeLocation[i][1]);
                spawnDat.setLocz(_ValakasCubeLocation[i][2]);
                spawnDat.setHeading(_ValakasCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _ValakasCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }
        
        _log.info("BossActionTaskManager:Init BossActionTaskManager.");
    }

    public List<L2PcInstance> getPlayersInLair(int npcId)
    {
    	switch(npcId)
    	{
    		case 29019:	// Antharas
    			return _playersInAntharasLair;
    		case 29020: // Baium
    			return _playersInBaiumLair;
    		case 29028: // Valakas
    			return _playersInValakasLair;
    		default:
    			return new FastList<L2PcInstance>();
    	}
    }
    
    public void CallArcAngel()
    {
    	_angels.clear();
    	int i = Rnd.get(0,1);
    	for(L2Spawn spawn : (FastList<L2Spawn>)_angelSpawn.get(i))
    	{
    		_angels.add(spawn.doSpawn());
    	}
    	
        for (L2NpcInstance Angel : _angels)
        {
            Angel.setIsInvul(true); // Arc Angel is invulnerable.
        }
    }

    public void RemoveArcAngel()
    {
        for (L2NpcInstance Angel : _angels)
        {
            Angel.getSpawn().stopRespawn();
            Angel.deleteMe();
        }
        _angels.clear();
    }

    public void SetNpcBaiumDecayAction(L2NpcInstance NpcBaium)
    {
        _npcbaium = NpcBaium;

        DeleteObject deo = new DeleteObject(_npcbaium);
        _npcbaium.broadcastPacket(deo);

        _BaiumSpawn.setLocx(_npcbaium.getX());
        _BaiumSpawn.setLocy(_npcbaium.getY());
        _BaiumSpawn.setLocz(_npcbaium.getZ());
        _BaiumSpawn.setHeading(_npcbaium.getHeading());
        L2BossInstance _Baium = (L2BossInstance)_BaiumSpawn.doSpawn();

        _Baium.setTargetForKill((L2PcInstance)_npcbaium.getTarget());
        
        _npcbaium.decayMe();
        
        _npcbaium.getSpawn().stopRespawn();

        _Baium = null;
    }

    public boolean CanIntoValakasLair()
    {
        return (_playersInValakasLair.size() < _CapacityOfLairOfValakas && !_IsValakasSpawned);
    }

    public boolean CanIntoAntharasLair()
    {
        return !_IsAntharasSpawned;
    }

    public void SetAntharasSpawnTask()
    {
        if (_playersInAntharasLair.size() >= 1) return;
        
        if (_AntharasSpawnTask == null)
        {
            _AntharasSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new AntharasSpawn(),_AppTimeOfAntharas);
        }
    }

    public void SetValakasSpawnTask()
    {
        if (_playersInValakasLair.size() >= 1) return;
        
        if (_VarakasSpawnTask == null)
        {
            _VarakasSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
            		new ValakasSpawn(),_AppTimeOfValakas);
        }
    }

    public void AddPlayerToAntharasLair(L2PcInstance pc)
    {
        if (!_playersInAntharasLair.contains(pc)) _playersInAntharasLair.add(pc);
    }

    public void AddPlayerToBaiumsLair(L2PcInstance pc)
    {
        if (!_playersInBaiumLair.contains(pc)) _playersInBaiumLair.add(pc);
    }
    
    public void AddPlayerToValakasLair(L2PcInstance pc)
    {
        if (!_playersInValakasLair.contains(pc)) _playersInValakasLair.add(pc);
    }

    public void banishesPlayers(int bossId)
    {
        switch (bossId)
        {
        	case 29019: // Antharas
        		banishesPlayersFromAntharasLair();
        		break;
        	case 29020: //Baium
        		banishesPlayersFromBaiumLair();
                break;
            case 29028: // Valakas
            	banishesPlayersFromValakasLair();
                break;
        }
    }

    protected void banishesPlayersFromAntharasLair()
    {
    	for(L2PcInstance pc : L2World.getInstance().getAllPlayers())
    	{
    		if(ZoneManager.getInstance().checkIfInZone("LairofAntharas", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(79959 + driftX,151774 + driftY,-3532);
    		}
    	}
    	_playersInAntharasLair.clear();
    }

    protected void banishesPlayersFromBaiumLair()
    {
    	for(L2PcInstance pc : L2World.getInstance().getAllPlayers())
    	{
    		if(ZoneManager.getInstance().checkIfInZone("LairofBaium", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(119705 + driftX,17888 + driftY,-5127);
    		}
    	}
    	_playersInBaiumLair.clear();
    }
    
    protected void banishesPlayersFromValakasLair()
    {
    	for(L2PcInstance pc : L2World.getInstance().getAllPlayers())
    	{
    		if(ZoneManager.getInstance().checkIfInZone("LairofValakas", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(150604 + driftX,-56283 + driftY,-2980);
    		}
    	}
    	_playersInValakasLair.clear();
    }
    
    public void setUnspawn(int bossId)
    {
        switch (bossId)
        {
        	case 29020: //Baium
        		_npcbaium.getSpawn().setRespawnDelay(600);
        		_npcbaium.getSpawn().startRespawn();
        		_npcbaium.getSpawn().decreaseCount(_npcbaium);
        		_npcbaium = null;
                break;
            case 29019: // Antharas
            	for(L2NpcInstance cube : _AntharasCube)
            	{
            		cube.getSpawn().stopRespawn();
            		cube.deleteMe();
            	}
            	_AntharasCube.clear();
                _IsAntharasSpawned = false;
                break;
            case 29028: // Valakas
            	for(L2NpcInstance cube : _ValakasCube)
            	{
            		cube.getSpawn().stopRespawn();
            		cube.deleteMe();
            	}
            	_ValakasCube.clear();
                _IsValakasSpawned = false;
                break;
        }
    }

    public void spawnCube(int bossId)
    {
        switch (bossId)
        {
        	case 29019: // Antharas
        		for(L2Spawn spawnDat :_AntharasCubeSpawn)
        		{
        			_AntharasCube.add(spawnDat.doSpawn());
        		}
        		break;
        	case 29028: // Valakas
        		for(L2Spawn spawnDat :_ValakasCubeSpawn)
        		{
        			_ValakasCube.add(spawnDat.doSpawn());
        		}
        		break;
        }
    }
    
    public void setCubeSpawn(int bossId)
    {
        switch (bossId)
        {
        	case 29019: // Antharas
        		_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
                    	new CubeSpawn(bossId),15000);
        		break;
        	case 29028: // Valakas
        		_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(
                		new CubeSpawn(bossId),21000);
        		break;
        }
    }
    
    private class AntharasSpawn implements Runnable
    {
    	public AntharasSpawn()
    	{
    	}
    	
        public void run()
        {
        	_AntharasSpawn.doSpawn();
        	
            _IsAntharasSpawned = true;
            
            if(_AntharasSpawnTask != null)
            {
            	_AntharasSpawnTask.cancel(true);
            	_AntharasSpawnTask = null;
            }
        }
    }

    private class ValakasSpawn implements Runnable
    {
    	public ValakasSpawn()
    	{
    	}
    	
        public void run()
        {
        	_ValakasSpawn.doSpawn();
        	
            _IsValakasSpawned = true;
            
            if(_VarakasSpawnTask != null)
            {
            	_VarakasSpawnTask.cancel(true);
            	_VarakasSpawnTask = null;
            }
        }
    }

    private class CubeSpawn implements Runnable
    {
    	int _bossId;
    	public CubeSpawn(int bossId)
    	{
    		_bossId = bossId;
    	}
    	
        public void run()
        {
        	spawnCube(_bossId);
        }
    }
}
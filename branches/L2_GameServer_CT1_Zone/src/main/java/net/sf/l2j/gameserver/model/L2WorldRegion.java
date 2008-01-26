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
package net.sf.l2j.gameserver.model;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.util.L2ObjectSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2WorldRegion
{
    private final static Log _log = LogFactory.getLog(L2WorldRegion.class.getName());

    /** L2ObjectHashSet(L2PcInstance) containing L2PlayableInstance of all player & summon in game in this L2WorldRegion */
    private L2ObjectSet<L2PlayableInstance> _allPlayable;

    /** L2ObjectHashSet(L2Object) containing L2Object visible in this L2WorldRegion */
    private L2ObjectSet<L2Object> _visibleObjects;

    private FastList<L2WorldRegion> _surroundingRegions;
    private int _tileX, _tileY;
    private Boolean _active = false;   
    protected ScheduledFuture<?> _neighborsTask = null;
    public static final int MAP_MIN_X = -131072;
    public static final int MAP_MAX_X = 228608;
    public static final int MAP_MIN_Y = -262144;
    public static final int MAP_MAX_Y = 262144;

    private final FastList<L2Zone> _zones;

    public L2WorldRegion(int pTileX, int pTileY)
    {
        _allPlayable = L2ObjectSet.createL2PlayerSet(); //new L2ObjectHashSet<L2PcInstance>();
        _visibleObjects = L2ObjectSet.createL2ObjectSet(); // new L2ObjectHashSet<L2Object>();
        _surroundingRegions = new FastList<L2WorldRegion>();
        //_surroundingRegions.add(this); //done in L2World.initRegions()

        _tileX = pTileX;
        _tileY = pTileY;
        
        // default a newly initialized region to inactive, unless always on is specified
        if (Config.GRIDS_ALWAYS_ON)
            _active = true;
        else
            _active = false;

        _zones = new FastList<L2Zone>();
    }

    public void addZone(L2Zone zone)
    {
        _zones.add(zone);
    }

    public void removeZone(L2Zone zone)
    {
        _zones.remove(zone);
    }

    public void revalidateZones(L2Character character)
    {
        for (L2Zone z : _zones)
        {
            z.revalidateInZone(character);
        }
    }

    public void removeFromZones(L2Character character)
    {
        for (L2Zone z : _zones)
        {
            z.removeCharacter(character);
        }
    }
    
    public boolean containsZone(int zoneId)
    {
        for (L2Zone z : _zones)
        {
            if (z.getId() == zoneId)
            {
                return true;
            }
        }
        return false;
    }

    public void onDeath(L2Character character)
    {
        for (L2Zone z : _zones)
        {
            z.onDieInside(character);
        }
    }

    public void onRevive(L2Character character)
    {
        for (L2Zone z : _zones)
        {
            z.onReviveInside(character);
        }
    }
    
    /** Task of AI and geodata notification */
    public class NeighborsTask implements Runnable
    {
        private boolean _isActivating;
        
        public NeighborsTask(boolean isActivating)
        {
            _isActivating = isActivating;
        }
        
        public void run()
        {
            if (_isActivating)
            {
                for (L2WorldRegion neighbor: getSurroundingRegions())
                    neighbor.setActive(true);
            }
            else
            {
                if(areNeighborsEmpty())
                    setActive(false);
                
                // check and deactivate
                for (L2WorldRegion neighbor: getSurroundingRegions())
                    if(neighbor.areNeighborsEmpty())
                        neighbor.setActive(false);   
            }
        }
    }
    
    private void switchAI(Boolean isOn)
    {
        int c = 0;
        if (!isOn)
        {
            for(L2Object o: _visibleObjects)
            {
                if (o instanceof L2Attackable)
                {
                    c++;
                    L2Attackable mob = (L2Attackable)o;

                    // Set target to null and cancel Attack or Cast
                    mob.setTarget(null);
                    
                    // Stop movement
                    mob.stopMove(null);
                    
                    // Stop all active skills effects in progress on the L2Character
                    mob.stopAllEffects();
                    
                    mob.clearAggroList();
                    mob.getKnownList().removeAllKnownObjects();
                    
                    mob.getAI().setIntention(net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE);

                    // stop the ai tasks
                    ((L2AttackableAI) mob.getAI()).stopAITask();

                    // Stop HP/MP/CP Regeneration task
                    // try this: allow regen, but only until mob is 100% full...then stop
                    // it until the grid is made active.
                    //mob.getStatus().stopHpMpRegeneration();
                }
            }
            if(_log.isDebugEnabled()) _log.debug(c+ " mobs were turned off");
        }
        else
        {
            for(L2Object o: _visibleObjects)
            {
                if (o instanceof L2Attackable)
                {
                    c++;
                    // Start HP/MP/CP Regeneration task
                    ((L2Attackable)o).getStatus().startHpMpRegeneration();
                    
                    // start the ai
                    //((L2AttackableAI) mob.getAI()).startAITask();
                }
                else if (o instanceof L2NpcInstance)
                {
                    // Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it 
                    // L2Monsterinstance/L2Attackable socials are handled by AI
                    ((L2NpcInstance)o).startRandomAnimationTimer();
                }
            }
            if(_log.isDebugEnabled()) _log.debug(c+ " mobs were turned on");
        }
        
    }

    public Boolean isActive()
    {
        return _active;
    }
    
    // check if all 9 neighbors (including self) are inactive or active but with no players.
    // returns true if the above condition is met. 
    public Boolean areNeighborsEmpty()
    {
        // if this region is occupied, return false.
        if (isActive() && (_allPlayable.size() > 0 ))
            return false;
        
        // if any one of the neighbors is occupied, return false
        for (L2WorldRegion neighbor: _surroundingRegions)
            if (neighbor.isActive() && (neighbor._allPlayable.size() > 0))
                return false;
        
        // in all other cases, return true.
        return true;
    }
    
    /**
     * this function turns this region's AI and geodata on or off
     * @param value
     */   
    public void setActive(Boolean value)
    {
        if (_active == value)
            return;
        
        _active = value;
        
        // turn the AI on or off to match the region's activation.
        switchAI(value);
        
        // turn the geodata on or off to match the region's activation.
        if(value)
            if(_log.isDebugEnabled())
                _log.info("Starting Grid " + _tileX + ","+ _tileY);
        else
            if(_log.isDebugEnabled())
            _log.info("Stoping Grid " + _tileX + ","+ _tileY);
    }

    /** Immediately sets self as active and starts a timer to set neighbors as active
     * this timer is to avoid turning on neighbors in the case when a person just 
     * teleported into a region and then teleported out immediately...there is no 
     * reason to activate all the neighbors in that case.
     */
    private void startActivation()
    {
        // first set self to active and do self-tasks...
        setActive(true);
        
        // if the timer to deactivate neighbors is running, cancel it.
        if(_neighborsTask !=null)
        {
            _neighborsTask.cancel(true);
            _neighborsTask = null;
        }
        
        // then, set a timer to activate the neighbors
		_neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000*Config.GRID_NEIGHBOR_TURNON_TIME);
    }
    
    /** starts a timer to set neighbors (including self) as inactive
     * this timer is to avoid turning off neighbors in the case when a person just 
     * moved out of a region that he may very soon return to.  There is no reason
     * to turn self & neighbors off in that case.
     */
    private void startDeactivation()
    {
        // if the timer to activate neighbors is running, cancel it.
        if(_neighborsTask !=null)
        {
            _neighborsTask.cancel(true);
            _neighborsTask = null;
        }
        
        // start a timer to "suggest" a deactivate to self and neighbors.
        // suggest means: first check if a neighbor has L2PcInstances in it.  If not, deactivate.
        _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000*Config.GRID_NEIGHBOR_TURNOFF_TIME);
    }
    
    /**
     * Add the L2Object in the L2ObjectHashSet(L2Object) _visibleObjects containing L2Object visible in this L2WorldRegion <BR>
     * If L2Object is a L2PcInstance, Add the L2PcInstance in the L2ObjectHashSet(L2PcInstance) _allPlayable
     * containing L2PcInstance of all player in game in this L2WorldRegion <BR>
     * Assert : object.getCurrentWorldRegion() == this
     */
    public void addVisibleObject(L2Object object)
    {
        if (Config.ASSERT) assert object.getWorldRegion() == this;

        if (object == null) return;
        _visibleObjects.put(object);

        if (object instanceof L2PlayableInstance)
        {
            _allPlayable.put((L2PlayableInstance) object);

            // if this is the first player to enter the region, activate self & neighbors
            if ((_allPlayable.size() == 1) && (!Config.GRIDS_ALWAYS_ON))
                startActivation();
        }
    }

    /**
     * Remove the L2Object from the L2ObjectHashSet(L2Object) _visibleObjects in this L2WorldRegion <BR><BR>
     * 
     * If L2Object is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) _allPlayable of this L2WorldRegion <BR>
     * Assert : object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null
     */
    public void removeVisibleObject(L2Object object)
    {
        if (Config.ASSERT) assert object.getWorldRegion() == this || object.getWorldRegion() == null;

        if (object == null) return;
        _visibleObjects.remove(object);

        if (object instanceof L2PlayableInstance)
        {
            _allPlayable.remove((L2PlayableInstance) object);
            
            if ((_allPlayable.size() == 0 ) && (!Config.GRIDS_ALWAYS_ON))
                startDeactivation();
        }
    }

    public void addSurroundingRegion(L2WorldRegion region)
    {
        _surroundingRegions.add(region);
    }

    /**
     * Return the FastList _surroundingRegions containing all L2WorldRegion around the current L2WorldRegion 
     */
    public FastList<L2WorldRegion> getSurroundingRegions()
    {
        //change to return L2WorldRegion[] ?
        //this should not change after initialization, so maybe changes are not necessary 

        return _surroundingRegions;
    }

    public Iterator<L2PlayableInstance> iterateAllPlayers()
    {
        return _allPlayable.iterator();
    }

    public L2ObjectSet<L2Object> getVisibleObjects()
    {
        return _visibleObjects;
    }

    public String getName()
    {
        return "(" + _tileX + ", " + _tileY + ")";
    }

    /**
     * Deleted all spawns in the world.
     */
    public synchronized void deleteVisibleNpcSpawns()
    {
        if(_log.isDebugEnabled()) _log.debug("Deleting all visible NPC's in Region: " + getName());
        for (L2Object obj : _visibleObjects)
        {
            if (obj instanceof L2NpcInstance)
            {
                L2NpcInstance target = (L2NpcInstance) obj;
                target.deleteMe();
                L2Spawn spawn = target.getSpawn();
                if (spawn != null)
                {
                    spawn.stopRespawn();
                    SpawnTable.getInstance().deleteSpawn(spawn, false);
                }
                if(_log.isDebugEnabled()) _log.debug("Removed NPC " + target.getObjectId());
            }
        }
        _log.info("All visible NPC's deleted in Region: " + getName());
    }
}

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
package com.l2jfree.gameserver.model;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SpawnTable;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.util.L2Collection;
import com.l2jfree.util.L2SynchronizedCollection;

public final class L2WorldRegion
{
    private final static Log _log = LogFactory.getLog(L2WorldRegion.class.getName());

    /** L2ReadWriteCollection(L2PlayableInstance) containing L2PlayableInstance of all player & summon in game in this L2WorldRegion */
    private final L2Collection<L2PlayableInstance> _playables = new L2SynchronizedCollection<L2PlayableInstance>();

    /** L2ReadWriteCollection(L2Object) containing L2Object visible in this L2WorldRegion */
    private final L2Collection<L2Object> _objects = new L2SynchronizedCollection<L2Object>();

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

    public FastList<L2Zone> getZones()
    {
        return _zones;
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
        // do NOT update the world region while the character is still in the process of teleporting
        // Once the teleport is COMPLETED, revalidation occurs safely, at that time.

        if (character.isTeleporting())
            return;

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

    public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, final int x, final int y, final int z)
    {
        final int range = skill.getEffectRange();
        final int up = y + range;
        final int down = y - range;
        final int left = x + range;
        final int right = x - range;

        for (L2Zone e : _zones)
        {
            if (e.isPeace())
            {
                if (e.isInsideZone(x, up, z))
                    return false;

                if (e.isInsideZone(x, down, z))
                    return false;

                if (e.isInsideZone(left, y, z))
                    return false;

                if (e.isInsideZone(right, y, z))
                    return false;

                if (e.isInsideZone(x, y, z))
                    return false;
            }
        }
        return true;
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
    
	private void switchAI(boolean isOn)
	{
		if (!isOn)
		{
			for (L2Object obj : getVisibleObjects())
			{
				if (obj instanceof L2Attackable)
				{
					L2Attackable mob = (L2Attackable)obj;
					
					mob.setTarget(null);
					mob.stopMove(null, false);
					mob.stopAllEffects();
					mob.clearAggroList();
					mob.clearDamageContributors();
					mob.resetAbsorbList();
					mob.getKnownList().removeAllKnownObjects();
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					mob.getAI().stopAITask();
				}
			}
		}
		else
		{
			for (L2Object obj : getVisibleObjects())
			{
				if (obj instanceof L2Attackable)
				{
					((L2Attackable)obj).getStatus().startHpMpRegeneration();
					//((L2AttackableAI)((L2Attackable)obj).getAI()).startAITask();
				}
				else if (obj instanceof L2NpcInstance)
				{
					((L2NpcInstance)obj).startRandomAnimationTimer();
				}
			}
			
			updateRegion();
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
        if (isActive() && (_playables.size() > 0 ))
            return false;
        
        // if any one of the neighbors is occupied, return false
        for (L2WorldRegion neighbor: _surroundingRegions)
            if (neighbor.isActive() && (neighbor._playables.size() > 0))
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
        synchronized(this)
        {
            if(_neighborsTask !=null)
            {
                _neighborsTask.cancel(true);
                _neighborsTask = null;
            }

            // then, set a timer to activate the neighbors
            _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
        }
    }
    
    /** starts a timer to set neighbors (including self) as inactive
     * this timer is to avoid turning off neighbors in the case when a person just 
     * moved out of a region that he may very soon return to.  There is no reason
     * to turn self & neighbors off in that case.
     */
    private void startDeactivation()
    {
        // if the timer to activate neighbors is running, cancel it.
        synchronized(this)
        {
            if(_neighborsTask !=null)
            {
                _neighborsTask.cancel(true);
                _neighborsTask = null;
            }

            // start a timer to "suggest" a deactivate to self and neighbors.
            // suggest means: first check if a neighbor has L2PcInstances in it.  If not, deactivate.
            _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000*Config.GRID_NEIGHBOR_TURNOFF_TIME);
        }
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
        _objects.add(object);

        if (object instanceof L2PlayableInstance)
        {
            _playables.add((L2PlayableInstance) object);

            // if this is the first player to enter the region, activate self & neighbors
            if ((_playables.size() == 1) && (!Config.GRIDS_ALWAYS_ON))
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
        _objects.remove(object);

        if (object instanceof L2PlayableInstance)
        {
        	_playables.remove((L2PlayableInstance) object);
            
            if ((_playables.size() == 0 ) && (!Config.GRIDS_ALWAYS_ON))
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
	
	public L2Object[] getVisibleObjects()
	{
		return _objects.toArray(new L2Object[_objects.size()]);
	}
	
	public L2PlayableInstance[] getVisiblePlayables()
	{
		return _playables.toArray(new L2PlayableInstance[_playables.size()]);
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
        for (L2Object obj : getVisibleObjects())
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
	
	private void updateRegion()
	{
		List<L2Object> addList = getAllSurroundingObjects();
		
		for (L2Object object : getVisibleObjects())
		{
			if (object == null || !object.isVisible())
				continue;
			
			object.getKnownList().tryAddObjects(addList);
		}
	}
	
	public final List<L2Object> getAllSurroundingObjects()
	{
		List<L2Object> result = new FastList<L2Object>();
		
		for (L2WorldRegion region : getSurroundingRegions())
			for (L2Object obj : region.getVisibleObjects())
				result.add(obj);
		
		return result;
	}
}

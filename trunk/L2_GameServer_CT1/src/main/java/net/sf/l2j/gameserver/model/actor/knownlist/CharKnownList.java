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
package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

public class CharKnownList extends ObjectKnownList
{
    // =========================================================
    // Data Field
    private Map<Integer, L2PcInstance> _knownPlayers;
    private Map<Integer, Integer> _knownRelations;
    
    // =========================================================
    // Constructor
    public CharKnownList(L2Character activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    @Override
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    @Override
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;
        if (object instanceof L2PcInstance) {
        	getKnownPlayers().put(object.getObjectId(), (L2PcInstance)object);
        	getKnownRelations().put(object.getObjectId(), -1);
        }
        return true;
    }

    /**
     * Return True if the L2PcInstance is in _knownPlayer of the L2Character.<BR><BR>
     * @param player The L2PcInstance to search in _knownPlayer
     */
    public final boolean knowsThePlayer(L2PcInstance player) { return getActiveChar() == player || getKnownPlayers().containsKey(player.getObjectId()); }
    
    /** Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI. */
    @Override
    public final void removeAllKnownObjects()
    {
        super.removeAllKnownObjects();
        getKnownPlayers().clear();
        getKnownRelations().clear();        

        // Set _target of the L2Character to null
        // Cancel Attack or Cast
        getActiveChar().setTarget(null);

        // Cancel AI Task
        if (getActiveChar().hasAI()) getActiveChar().setAI(null);
    }
    
    @Override
    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object)) return false;
        if (object instanceof L2PcInstance) {
        	getKnownPlayers().remove(object.getObjectId());
        	getKnownRelations().remove(object.getObjectId());
        }
        // If object is targeted by the L2Character, cancel Attack or Cast
        if (object == getActiveChar().getTarget()) getActiveChar().setTarget(null);

        return true;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    
    /**
     * Update the _knownObject and _knowPlayers of the L2Character and of its
     * already known L2Object.<BR>
     * <BR>
     * 
     * <B><U> Actions</U> :</B><BR>
     * <BR>
     * <li>Remove invisible and too far L2Object from _knowObject and if
     * necessary from _knownPlayers of the L2Character </li>
     * <li>Add visible L2Object near the L2Character to _knowObject and if
     * necessary to _knownPlayers of the L2Character </li>
     * <li>Add L2Character to _knowObject and if necessary to _knownPlayers of
     * L2Object alreday known by the L2Character </li>
     * <BR>
     * <BR>
     */
    public final synchronized void updateKnownObjects()
    {
		// Remove all invisible and too far objects<br>
        // Go through knownObjects
        Collection<L2Object> knownObjects = getKnownObjects().values();

        if (knownObjects != null && knownObjects.size() != 0)
        {
        	for (L2Object object : knownObjects)
        	{
        		if (object == null)
        			continue;

        		// Remove all invisible object
        		// Remove all too far object
        		if (!object.isVisible() || !Util.checkIfInRange(getDistanceToForgetObject(object), getActiveObject(), object, true))
        			if (object instanceof L2BoatInstance && getActiveObject() instanceof L2PcInstance)
        			{
        				if (((L2BoatInstance) (object)).getVehicleDeparture() == null)
        				{
        					//
        				} else if (((L2PcInstance) getActiveObject()).isInBoat())
        				{
        					if (((L2PcInstance) getActiveObject()).getBoat() == object)
        					{
        						//
        					} else
        					{
        						removeKnownObject(object);
        					}
        				} else
        				{
        					removeKnownObject(object);
        				}
        			} else
        			{
        				removeKnownObject(object);
        			}
        	}
        }
        
    	// find known objects
    	boolean isActiveObjectPlayable = (getActiveObject() instanceof L2PlayableInstance);

        if (isActiveObjectPlayable)
        {
            FastList<L2Object> objects = L2World.getInstance().getVisibleObjects(getActiveObject());
            if (objects == null)
                return;

            // Go through all visible L2Object near the L2Character
            for (L2Object object : objects)
            {
                if (object == null)
                    continue;
                
                if(!Util.checkIfInRange(getDistanceToForgetObject(object), getActiveObject(), object, true))
                	continue;

                // Try to add object to active object's known objects
                // L2PlayableInstance sees everything
                addKnownObject(object);

                // Try to add active object to object's known objects
                // Only if object is a L2Character and active object is a
                // L2PlayableInstance
                if (object instanceof L2Character)
                    object.getKnownList().addKnownObject(getActiveObject());
            }
        } else
        {
            FastList<L2PlayableInstance> playables = L2World.getInstance().getVisiblePlayable(getActiveObject());
            if (playables == null)
                return;

            // Go through all visible L2Object near the L2Character
            for (L2Object playable : playables)
            {
                if (playable == null)
                    continue;
                
                if(!Util.checkIfInRange(getDistanceToForgetObject(playable), getActiveObject(), playable, true))
                	continue;

                // Try to add object to active object's known objects
                // L2Character only needs to see visible L2PcInstance and
                // L2PlayableInstance,
                // when moving. Other l2characters are currently only known from
                // initial spawn area.
                // Possibly look into getDistanceToForgetObject values before
                // modifying this approach...
                addKnownObject(playable);
            }
        }
    }

    public L2Character getActiveChar() { return (L2Character)super.getActiveObject(); }
    
    @Override
    public int getDistanceToForgetObject(L2Object object) { return 0; }

    @Override
    public int getDistanceToWatchObject(L2Object object) { return 0; }

    public Collection<L2Character> getKnownCharacters()
    {
        List<L2Character> result = new ArrayList<L2Character>();
        
        for (L2Object obj : getKnownObjects().values())  
        {  
            if (obj != null && obj instanceof L2Character) result.add((L2Character) obj);  
        }
        
        return result;
    }
    
    public Collection<L2Character> getKnownCharactersInRadius(long radius)
    {
       List<L2Character> result = new ArrayList<L2Character>();
       
       for (L2Object obj : getKnownObjects().values())  
       {  
           if (obj instanceof L2PcInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2PcInstance)obj);  
           }  
           else if (obj instanceof L2MonsterInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2MonsterInstance)obj);  
           }  
           else if (obj instanceof L2NpcInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2NpcInstance)obj);  
           }
       }
       
       return result;
    }

    public final Map<Integer, L2PcInstance> getKnownPlayers()
    {
        if (_knownPlayers == null) _knownPlayers = new FastMap<Integer, L2PcInstance>().setShared(true);
        return _knownPlayers;
    }

	public final Map<Integer, Integer> getKnownRelations()
	{
	    if (_knownRelations == null) _knownRelations = new FastMap<Integer, Integer>().setShared(true);
	    return _knownRelations;
	}

    public final Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
    {
        List<L2PcInstance> result = new ArrayList<L2PcInstance>();
        
        for (L2PcInstance player : getKnownPlayers().values())
            if (Util.checkIfInRange((int)radius, getActiveChar(), player, true))
                result.add(player);
            
        return result;
    }
    
    /**
     * Asynchronous task use to update known objects periodically
     *
     */
    public static class KnownListAsynchronousUpdateTask implements Runnable
    {
        /**
         * active object
         */
        private L2Character _obj;
        
        /**
         * Constructor with the active object
         * @param obj
         */
        public KnownListAsynchronousUpdateTask(L2Character obj)
        {
            _obj = obj;
        }

        /**
         * Update known objects of active objects
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            if (_obj != null)
                _obj.getKnownList().updateKnownObjects();
        }
    }
}

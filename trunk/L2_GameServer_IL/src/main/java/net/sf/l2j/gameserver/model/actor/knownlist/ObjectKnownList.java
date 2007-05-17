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
package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that hold the known list of a specific L2Object.<br>
 * 
 * If you want to add some treatment for a specific subclass of L2Object, you need
 * to inherit from ObjectKnownList and override modify L2Object.getKnownList in the new subclass 
 * of L2Object.
 *
 */
public class ObjectKnownList
{
    /**
     * Reference to the L2Object
     */
    private L2Object _ActiveObject;
    
    /**
     * Map of all L2Object known by the active char
     */
    private Map<Integer, L2Object> _KnownObjects;
    
    /**
     * Logger
     */
    private static final Log _log = LogFactory.getLog(ObjectKnownList.class.getName());
    
    /**
     * Constructor with the reference of an activeObject
     * @param activeObject
     */
    public ObjectKnownList(L2Object activeObject)
    {
        _ActiveObject = activeObject;
    }

    /**
     * Add a object in the known object of activeObject
     * @param object
     * @return true if the add was successfull, false otherwise
     */
    public boolean addKnownObject(L2Object object)
    {
        return addKnownObject(object, null);
    }
    
    /**
     * Add a object in the known object of activeObject
     * @param object
     * @param dropper
     * @return true if the add was successfull, false otherwise
     */
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (object == null || object == getActiveObject()) return false;

        // Check if already know object
        if (knowsObject(object))
        {
            if (!object.isVisible())
                removeKnownObject(object);
            return false;
        }

        // Check if object is not inside distance to watch object
        if (!Util.checkIfInRange(getDistanceToWatchObject(object), getActiveObject(), object, true))
            return false;

        return (getKnownObjects().put(object.getObjectId(), object) == null);
    }
    
    /**
     * Check if active object knows this object
     * @param object
     * @return true if the active object know this object, false otherwise
     */
    public final boolean knowsObject(L2Object object)
    {
        return getActiveObject() == object || getKnownObjects().containsKey(object.getObjectId());
    }

    /** 
     * Remove all L2Object from _knownObjects 
     */
    public void removeAllKnownObjects()
    {
        getKnownObjects().clear();
    }

    /**
     * Remove a specific object of the known object for this active object
     * @param object
     * @return
     */
    public boolean removeKnownObject(L2Object object)
    {
        if (object == null)
            return false;
        if (getKnownObjects() == null)
        {
            _log.error("Well there is definetly sth wrong with this knownobjectlist thingie");
            return false;
        }
        return (getKnownObjects().remove(object.getObjectId()) != null);
    }

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
     * 
     * TODO move this treatment in CharKnownList
     */
    public final synchronized void updateKnownObjects()
    {
        // Only bother updating knownobjects for L2Character; don't for L2Object
        if (getActiveObject() instanceof L2Character)
        {
            findCloseObjects();
            forgetObjects();
        }
    }

    /**
     * Go through all visible L2Object near the active L2Object<br>
     * Try to add object to active object's known objects<br>
     * Try to add active object to object's known objects<br>
     *
     */
    private final void findCloseObjects()
    {
        boolean isActiveObjectPlayable = (getActiveObject() instanceof L2PlayableInstance);

        if (isActiveObjectPlayable)
        {
            Collection<L2Object> objects = L2World.getInstance().getVisibleObjects(getActiveObject());
            if (objects == null)
                return;

            // Go through all visible L2Object near the L2Character
            for (L2Object object : objects)
            {
                if (object == null)
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
            Collection<L2PlayableInstance> playables = L2World.getInstance().getVisiblePlayable(getActiveObject());
            if (playables == null)
                return;

            // Go through all visible L2Object near the L2Character
            for (L2Object playable : playables)
            {
                if (playable == null)
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

    /**
     * Go through known objects <br>
     * Remove all invisible object<br>
     * Remove all too far object<br>
     *
     */
    private final void forgetObjects()
    {
        // Go through knownObjects
        Collection<L2Object> knownObjects = getKnownObjects().values();

        if (knownObjects == null || knownObjects.size() == 0)
            return;

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

    /**
     * @return the active object
     */
    public L2Object getActiveObject()
    {
        return _ActiveObject;
    }

    /**
     * Return the distance to forget object
     * @param object
     * @return 0
     */
    public int getDistanceToForgetObject(L2Object object)
    {
        return 0;
    }
    
    /**
     * Return the distance to watch object
     * @param object
     * @return 0
     */
    public int getDistanceToWatchObject(L2Object object)
    {
        return 0;
    }

    /**
     * @return the _knownObjects containing all L2Object known by the active L2Object
     */
    public final Map<Integer, L2Object> getKnownObjects()
    {
        if (_KnownObjects == null)
            _KnownObjects = new FastMap<Integer, L2Object>().setShared(true);
        return _KnownObjects;
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
        private L2Object _obj;
        
        /**
         * Constructor with the active object
         * @param obj
         */
        public KnownListAsynchronousUpdateTask(L2Object obj)
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

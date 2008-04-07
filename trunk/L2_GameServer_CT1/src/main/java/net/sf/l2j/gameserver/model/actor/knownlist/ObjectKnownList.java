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
package net.sf.l2j.gameserver.model.actor.knownlist;

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
    private L2Object _activeObject;
    
    /**
     * Map of all L2Object known by the active char
     */
    private Map<Integer, L2Object> _knownObjects;
    
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
        _activeObject = activeObject;
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
        if (object == null) return false;

        // Check if already know object
        if (knowsObject(object))
        {
            return false;
        }

        // Check if object is not inside distance to watch object
        if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
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
        return (getKnownObjects().remove(object.getObjectId()) != null);
    }

    // Remove invisible and too far L2Object from _knowObject and if necessary from _knownPlayers of the L2Character
    public final void forgetObjects(boolean fullCheck)
    {
        // Go through knownObjects
        for (L2Object object: getKnownObjects().values())
        {
            if (!fullCheck && !(object instanceof L2PlayableInstance))
                continue;

            // Remove all invisible object
            // Remove all too far object
            if (!object.isVisible() ||
                    !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true)
            )
            {
                if (object instanceof L2BoatInstance && getActiveObject() instanceof L2PcInstance)
                {
                    if(((L2BoatInstance)(object)).getVehicleDeparture() == null)
                    {
                        //
                    }
                    else if(((L2PcInstance)getActiveObject()).isInBoat())
                    {
                        if(((L2PcInstance)getActiveObject()).getBoat() == object)
                        {
                            //
                        }
                        else
                            removeKnownObject(object);
                    }
                    else
                        removeKnownObject(object);
                }
                else
                    removeKnownObject(object);
            }
        }
    }

    /**
     * @return the active object
     */
    public L2Object getActiveObject()
    {
        return _activeObject;
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
        if (_knownObjects == null)
            _knownObjects = new FastMap<Integer, L2Object>().setShared(true);
        return _knownObjects;
    }
}

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
package com.l2jfree.gameserver.model.actor.knownlist;

import java.util.List;
import java.util.Map;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.util.SingletonMap;

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

        // instance -1 for gms can see everything on all instances 
        if(getActiveObject().getInstanceId() != -1 && (object.getInstanceId() != getActiveObject().getInstanceId()))
            return false;
        
        // check if the object is i a l2pcinstance in ghost mode
        if(object instanceof L2PcInstance && ((L2PcInstance)object).getAppearance().isGhost())
        	return false;
        	
        
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
		for (L2Object object : getKnownObjects().values())
		{
			removeKnownObject(object);
			object.getKnownList().removeKnownObject(getActiveObject());
		}
		
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
			_knownObjects = new SingletonMap<Integer, L2Object>().setShared();
		
		return _knownObjects;
	}

	public final L2Object getKnownObject(int objectId)
	{
		return getKnownObjects().get(objectId);
	}
	
	public final void tryAddObjects(List<L2Object> addList)
	{
		if (getActiveObject() instanceof L2Character)
		{
			if (addList == null)
				addList = L2World.getInstance().getVisibleObjects(getActiveObject());
			
			for (L2Object object : addList)
			{
				addKnownObject(object);
				
				if (object instanceof L2Character)
					object.getKnownList().addKnownObject(getActiveObject());
			}
		}
	}
	
	public final void tryRemoveObjects()
	{
		for (L2Object object : getKnownObjects().values())
		{
			tryRemoveObject(object);
			object.getKnownList().tryRemoveObject(getActiveObject());
		}
	}
	
	private final void tryRemoveObject(L2Object obj)
	{
		if (obj.isVisible() && Util.checkIfInShortRadius(getDistanceToForgetObject(obj), getActiveObject(), obj, true))
			return;
		
		if (obj instanceof L2BoatInstance && getActiveObject() instanceof L2PcInstance)
		{
			if (((L2BoatInstance)obj).getVehicleDeparture() == null)
				return;
			
			L2PcInstance pc = (L2PcInstance)getActiveObject();
			
			if (pc.isInBoat() && pc.getBoat() == obj)
				return;
		}
		
		removeKnownObject(obj);
	}
	
	private long _lastUpdate;
	
	public synchronized final void updateKnownObjects()
	{
		if (System.currentTimeMillis() - _lastUpdate < 100)
			return;
		
		tryRemoveObjects();
		tryAddObjects(null);
		
		_lastUpdate = System.currentTimeMillis();
	}
}
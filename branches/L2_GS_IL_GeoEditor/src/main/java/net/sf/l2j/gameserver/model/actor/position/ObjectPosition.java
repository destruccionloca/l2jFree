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

package net.sf.l2j.gameserver.model.actor.position;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that hold the position of a specific L2Object.<br>
 * 
 * If you want to add some treatment for a specific subclass of L2Object, you need
 * to inherit from ObjectPosition and override modify L2Object.getPosition in the new subclass 
 * of L2Object.
 */
public class ObjectPosition
{
    /**
     * Logger 
     */
    private static final Log _log = LogFactory.getLog(ObjectPosition.class.getName());

    /**
     * Reference of the L2Object that own this ObjectPosition
     */
    private L2Object _activeObject;
    /**
     * Heading (Direction of the object)
     */
    private int _heading    = 0;
    /**
     * The world position with a 3d coordinate (x,y,z)
     */
    private Point3D _worldPosition;
    /**
     * Object localization in world : Used for items/chars that are seen in the world
     */
    private L2WorldRegion _worldRegion;  

    /**
     * Constructor with the L2Object in reference
     * @param activeObject
     */
    public ObjectPosition(L2Object activeObject)
    {
        _activeObject = activeObject;
        setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
    }
    
    /**
     * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.<BR><BR>
     *
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldRegion != null</li><BR><BR>
     * 
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Update position during and after movement, or after teleport </li><BR>
     */
    public final void setXYZ(int x, int y, int z)
    {
        if (Config.ASSERT) assert getWorldRegion() != null;
        
        setWorldPosition(x, y ,z);
        
        try
        {
            if (L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
                updateWorldRegion();
        }
        catch (Exception e)
        {
            _log.warn("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
            if (getActiveObject() instanceof L2Character)
                getActiveObject().decayMe();
            else if (getActiveObject() instanceof L2PcInstance)
            {
                //((L2PcInstance)obj).deleteMe();
                ((L2PcInstance)getActiveObject()).teleToLocation(0,0,0, false);
                ((L2PcInstance)getActiveObject()).sendMessage("Error with your coords, Please ask a GM for help!");
            }
        }
    }

    /**
     * Set the x,y,z position of the L2Object and make it invisible.<BR><BR>
     * 
     * <B><U> Concept</U> :</B><BR><BR>
     * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldregion==null <I>(L2Object is invisible)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Create a Door</li>
     * <li> Restore L2PcInstance</li><BR>
     */
    public final void setXYZInvisible(int x, int y, int z)
    {
        if (Config.ASSERT) assert getWorldRegion() == null;
        if (x > L2World.MAP_MAX_X) x = L2World.MAP_MAX_X - 5000;
        if (x < L2World.MAP_MIN_X) x = L2World.MAP_MIN_X + 5000;
        if (y > L2World.MAP_MAX_Y) y = L2World.MAP_MAX_Y - 5000;
        if (y < L2World.MAP_MIN_Y) y = L2World.MAP_MIN_Y + 5000;

        setWorldPosition(x, y ,z);
        getActiveObject().setIsVisible(false);
    }

    /**
     * checks if current object changed its region, if so, update referencies
     */
    public final void updateWorldRegion() 
    {
        if (!getActiveObject().isVisible()) return;

        L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
        if (newRegion != getWorldRegion())
        {
            getWorldRegion().removeVisibleObject(getActiveObject());

            setWorldRegion(newRegion);

            // Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
            getWorldRegion().addVisibleObject(getActiveObject());
        }
    }
    
    /**
     * @return the active object
     */
    private final L2Object getActiveObject()
    {
        return _activeObject;
    }
    
    /**
     * @return the heading
     */
    public final int getHeading()
    {
        return _heading;
    }

    /**
     * @param value the heading to set
     */
    public final void setHeading(int value)
    {
        _heading = value;
    }

    /** 
     * @return the x position of the L2Object. 
     */
    public final int getX()
    {
        return getWorldPosition().getX();
    }

    /** 
     * @return the y position of the L2Object. 
     */
    public final int getY()
    {
        return getWorldPosition().getY();
    }

    /**
     * @return the z position of the L2Object. 
    */
    public final int getZ()
    {
        return getWorldPosition().getZ();
    }

    /**
     * @return the world position (x,y,z)
     */
    public final Point3D getWorldPosition()
    {
        if (_worldPosition == null)
            _worldPosition = new Point3D(0, 0, 0);
        return _worldPosition;
    }

    /**
     * Set the world position and revalidate zone if necessary for L2PcInstance
     * 
     * @param x
     * @param y
     * @param z
     */
    public final void setWorldPosition(int x, int y, int z)
    {
        getWorldPosition().setXYZ(x, y, z);
    }

    /**
     * Set the world position
     * 
     * @see ObjectPosition#setWorldPosition(int, int, int)
     * @param newPosition
     */
    public final void setWorldPosition(Point3D newPosition)
    {
        setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
    }
    
    /**
     * @return the world region 
     */
    public final L2WorldRegion getWorldRegion()
    {
        return _worldRegion;
    }
    
    /**
     * @param value the world region to set
     */
    public final void setWorldRegion(L2WorldRegion value)
    {
        _worldRegion = value;
    }
}

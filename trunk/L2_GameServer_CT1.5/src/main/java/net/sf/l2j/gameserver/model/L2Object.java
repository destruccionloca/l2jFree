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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.GetItem;

/**
 * Mother class of all objects in the world wich ones is it possible 
 * to interact (PC, NPC, Item...)<BR><BR>
 * 
 * An object have a visibility, a position and an appearance.
 * An object know several other objects via ObjectKnownList <br>
 * 
 * L2Object :<BR><BR>
 * <li>L2Character</li>
 * <li>L2ItemInstance</li>
 * <li>L2Potion</li> 
 * 
 */
public abstract class L2Object
{
    /**
     * Object visibility
     */
    private boolean _isVisible;
    /**
     * Objects known by this object
     */
    private ObjectKnownList _knownList;
    /**
     * Name of this object
     */
    private String _name;
    /**
     * unique identifier
     */
    private int _objectId;
    /**
     * Appearance and type of object 
     */
    private ObjectPoly _poly;
    /**
     * Position of object
     */
    private ObjectPosition _position;

    /**
     * Constructor
     * @param objectId
     */
    public L2Object(int objectId)
    {
        _objectId = objectId;
        _name = "";
    }
    
    /**
     * Default action played by this object
     * @param player
     */
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    /**
     * 
     * @param client
     */
    public void onActionShift(L2PcInstance player)
    {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
    
    /**
     * Determine default action on forced attack
     * @param player
     */
    public void onForcedAttack(L2PcInstance player)
    {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    /**
     * Do Nothing.<BR><BR>
     * 
     * Determine default actions on spawn
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2GuardInstance :  Set the home location of its L2GuardInstance </li>
     * <li> L2Attackable    :  Reset the Spoiled falg </li><BR><BR>
     * 
     */
    public void onSpawn()
    {
    }

    /**
     * get the x coordinate for this object
     * @return x
     */
    public final int getX()
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() != null || _isVisible;
        return getPosition().getX();
    }

    /**
     * get the y coordinate for this object
     * @return y
     */
    public final int getY()
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() != null || _isVisible;
        return getPosition().getY();
    }

    /**
     * get the z coordinate for this object
     * @return z
     */
    public final int getZ()
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() != null || _isVisible;
        return getPosition().getZ();
    }
    
    /**
     * Remove a L2Object from the world.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Remove the L2Object from the world</li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Delete NPC/PC or Unsummon</li><BR><BR>
     * 
     */
    public void decayMe() 
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() != null;
        
        L2WorldRegion reg = getPosition().getWorldRegion();
        
        synchronized (this) 
        {
            _isVisible = false;
            getPosition().setWorldRegion(null);
        }
        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Remove the L2Object from the world
        L2World.getInstance().removeVisibleObject(this, reg);
        L2World.getInstance().removeObject(this);
    }

    /**
     * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member </li>
     * <li>Remove the L2Object from the world</li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> this instanceof L2ItemInstance</li>
     * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Do Pickup Item : PCInstance and Pet</li><BR><BR>
     * 
     * @param player Player that pick up the item
     * 
     */
    public final void pickupMe(L2Character player) // NOTE: Should move this function into L2ItemInstance because it does not apply to L2Character
    {
        if (Config.ASSERT) assert this instanceof L2ItemInstance;
        if (Config.ASSERT) assert getPosition().getWorldRegion() != null;
        
        L2WorldRegion oldregion = getPosition().getWorldRegion();
        
        // Create a server->client GetItem packet to pick up the L2ItemInstance
        GetItem gi = new GetItem((L2ItemInstance)this, player.getObjectId());
        player.broadcastPacket(gi);
        
        synchronized (this) 
        {
            _isVisible = false;
            getPosition().setWorldRegion(null);
        }

        // if this item is a mercenary ticket, remove the spawns!
        if (this instanceof L2ItemInstance)
        {
            ItemsOnGroundManager.getInstance().removeObject(this);
            int itemId = ((L2ItemInstance)this).getItemId();
            if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
            {
                MercTicketManager.getInstance().removeTicket((L2ItemInstance)this);
            }
            else if (itemId == 57 || itemId == 6353)
            {
                QuestState qs = null;
                if (player instanceof L2Summon)
                {
                    qs = ((L2Summon)player).getOwner().getQuestState("255_Tutorial");
                    if (qs != null)
                        qs.getQuest().notifyEvent("CE"+itemId+"",null,((L2Summon)player).getOwner());
                }
                else if (player instanceof L2PcInstance)
                {
                    qs = ((L2PcInstance)player).getQuestState("255_Tutorial");
                    if(qs != null)
                        qs.getQuest().notifyEvent("CE"+itemId+"",null,(L2PcInstance)player);
                }
            }
        }
        
        ItemsOnGroundManager.getInstance().removeObject(this);
        
        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Remove the L2ItemInstance from the world
        L2World.getInstance().removeVisibleObject(this, oldregion);
    }

    /**
     * Refresh the object id (ask to IdFactory to release the old id and get a new one)
     * @see net.sf.l2j.gameserver.idfactory.IdFactory
     */
    public void refreshID()
    {
        L2World.getInstance().removeObject(this);
        IdFactory.getInstance().releaseId(getObjectId());
        _objectId = IdFactory.getInstance().getNextId();
    }

    /**
     * Init the position of a L2Object spawn and add it in the world as a visible object.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Set the x,y,z position of the L2Object spawn and update its _worldregion </li>
     * <li>Add the L2Object spawn in the _allobjects of L2World </li>
     * <li>Add the L2Object spawn to _visibleObjects of its L2WorldRegion</li>
     * <li>Add the L2Object spawn in the world as a <B>visible</B> object</li><BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Create Door</li>
     * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
     * 
     */
    public final void spawnMe()
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() == null && getPosition().getWorldPosition().getX() != 0 && getPosition().getWorldPosition().getY() != 0 && getPosition().getWorldPosition().getZ() != 0;
        
        synchronized (this) 
        {
            // Set the x,y,z position of the L2Object spawn and update its _worldregion
            _isVisible = true;
            getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
            
            // Add the L2Object spawn in the _allobjects of L2World
            L2World.getInstance().storeObject(this);
            
            // Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
            getPosition().getWorldRegion().addVisibleObject(this);
        }
        
        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Add the L2Object spawn in the world as a visible object
        L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);
        
        onSpawn();
    }

    /**
     * Init the position of a L2Object spawn and add it in the world as a visible object.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Set the x,y,z position of the L2Object spawn and update its _worldregion </li>
     * <li>Add the L2Object spawn in the _allobjects of L2World </li>
     * <li>Add the L2Object spawn to _visibleObjects of its L2WorldRegion</li>
     * <li>Add the L2Object spawn in the world as a <B>visible</B> object</li><BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Create Door</li>
     * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
     * 
     * @param x
     * @param y
     * @param z 
     */
    public final void spawnMe(int x, int y, int z)
    {
        if (Config.ASSERT) assert getPosition().getWorldRegion() == null;
        
        synchronized (this) 
        {
            // Set the x,y,z position of the L2Object spawn and update its _worldregion
            _isVisible = true;

            if (x > L2World.MAP_MAX_X) x = L2World.MAP_MAX_X - 5000;
            if (x < L2World.MAP_MIN_X) x = L2World.MAP_MIN_X + 5000;
            if (y > L2World.MAP_MAX_Y) y = L2World.MAP_MAX_Y - 5000;
            if (y < L2World.MAP_MIN_Y) y = L2World.MAP_MIN_Y + 5000;
            
            getPosition().setWorldPosition(x, y ,z);
            getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
            
            // Add the L2Object spawn in the _allobjects of L2World
            L2World.getInstance().storeObject(this);
            
            // Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
            getPosition().getWorldRegion().addVisibleObject(this);
        }
        
        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Add the L2Object spawn in the world as a visible object
        L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);
        
        onSpawn();
    }
    
    /**
     * If the object is visible, decay it. It not, spawn it.
     */
    public void toggleVisible()
    {
        if (isVisible())
            decayMe();
        else
            spawnMe();
    }

    /**
     * Tell if this object is attackable or not. 
     * By default, L2Object are not attackable.
     * @return
     */
    public boolean isAttackable()
    {
        return false;
    }
    
    /**
     * Return True if the L2Character is autoAttackable
     * 
     * @param attacker
     * @return true if L2Character is autoAttackable, false otherwise
     */
    public abstract boolean isAutoAttackable(L2Character attacker);

    /**
     * Return the visibilty state of the L2Object. <BR><BR>
     *  
     * <B><U> Concept</U> :</B><BR><BR>
     * A L2Object is visble if <B>_worldregion</B>!=null <BR><BR>
     */
    public final boolean isVisible() 
    {
        //return getPosition().getWorldRegion() != null && _isVisible;
        return getPosition().getWorldRegion() != null;
    }
    /**
     * Set the visibilty state of the L2Object. <BR><BR>
     * (Set world region to null)
     * @param value
     */
    public final void setIsVisible(boolean value)
    {
        _isVisible = value;
        if (!_isVisible) 
            getPosition().setWorldRegion(null);
    }
    
    /**
     * Return the known list of object from this instance
     * @return an objectKnownList
     */
    public ObjectKnownList getKnownList()
    {
        if (_knownList == null) _knownList = new ObjectKnownList(this);
        return _knownList;
    }
    /**
     * Set the known list 
     * @param value the knownlist to set
     */
    public final void setKnownList(ObjectKnownList value) { _knownList = value; }
    
    /**
     * return the name
     * @return the name
     */
    public final String getName()
    {
        return _name;
    }
    
    /**
     * @param value the name to set
     */
    public final void setName(String value)
    {
        _name = value;
    }

    /**
     * @return the object id
     */
    public final int getObjectId()
    {
        return _objectId;
    }
    
    /**
     * @return the appearance
     */
    public final ObjectPoly getPoly()
    {
        if (_poly == null) _poly = new ObjectPoly(this);
        return _poly;
    }
    
    /**
     * @return the position
     */
    public final ObjectPosition getPosition()
    {
        if (_position == null) _position = new ObjectPosition(this);
        return _position;
    }

    /**
     * @return reference to region this object is in
     */
    public L2WorldRegion getWorldRegion() 
    {
        return getPosition().getWorldRegion();
    }
    
    /**
     * Basic implementation of toString to print the object id
     */
    @Override
    public String toString()
    {
        return "" + getObjectId();
    }
    
    public boolean isInFunEvent()
    {
    	if(this instanceof L2PcInstance)
    	{
    		return ((L2PcInstance)this).isInFunEvent();
    	}
    	if(this instanceof L2PetInstance)
    	{
    		return ((L2PetInstance)this).getOwner().isInFunEvent();
    	}
    	if(this instanceof L2SummonInstance)
    	{
    		return ((L2SummonInstance)this).getOwner().isInFunEvent();
    	}
    	return false;
    }
}

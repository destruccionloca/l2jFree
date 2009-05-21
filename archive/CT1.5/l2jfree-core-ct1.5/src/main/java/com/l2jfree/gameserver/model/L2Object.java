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

import com.l2jfree.Config;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jfree.gameserver.instancemanager.MercTicketManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.knownlist.ObjectKnownList;
import com.l2jfree.gameserver.model.actor.poly.ObjectPoly;
import com.l2jfree.gameserver.model.actor.position.ObjectPosition;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.GetItem;

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
	private boolean				_isVisible;
	/**
	 * Objects known by this object
	 */
	protected ObjectKnownList	_knownList;
	/**
	 * Name of this object
	 */
	private String				_name;
	/**
	 * unique identifier
	 */
	private Integer				_objectId;
	/**
	 * Appearance and type of object 
	 */
	private ObjectPoly			_poly;
	/**
	 * Position of object
	 */
	private ObjectPosition		_position;

	// Objects can only see objects in same instancezone, instance 0 is normal world -1 the all seeing world
	private int					_instanceId = 0;

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
	 * <B><U> Overridden in </U> :</B><BR><BR>
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
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() != null || _isVisible;
		return getPosition().getX();
	}

	/**
	 * get the y coordinate for this object
	 * @return y
	 */
	public final int getY()
	{
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() != null || _isVisible;
		return getPosition().getY();
	}

	/**
	 * get the z coordinate for this object
	 * @return z
	 */
	public final int getZ()
	{
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() != null || _isVisible;
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
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() != null;

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
		if (Config.ASSERT)
			assert this instanceof L2ItemInstance;
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() != null;

		L2WorldRegion oldregion = getPosition().getWorldRegion();

		// Create a server->client GetItem packet to pick up the L2ItemInstance
		GetItem gi = new GetItem((L2ItemInstance) this, player.getObjectId());
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
			int itemId = ((L2ItemInstance) this).getItemId();
			if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
			{
				MercTicketManager.getInstance().removeTicket((L2ItemInstance) this);
			}
			else if (itemId == 57 || itemId == 6353)
			{
				QuestState qs = null;
				if (player instanceof L2Summon)
				{
					qs = ((L2Summon) player).getOwner().getQuestState("255_Tutorial");
					if (qs != null)
						qs.getQuest().notifyEvent("CE" + itemId + "", null, ((L2Summon) player).getOwner());
				}
				else if (player instanceof L2PcInstance)
				{
					qs = ((L2PcInstance) player).getQuestState("255_Tutorial");
					if (qs != null)
						qs.getQuest().notifyEvent("CE" + itemId + "", null, (L2PcInstance) player);
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
	 * @see com.l2jfree.gameserver.idfactory.IdFactory
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
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() == null && getPosition().getWorldPosition().getX() != 0 && getPosition().getWorldPosition().getY() != 0
					&& getPosition().getWorldPosition().getZ() != 0;

		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

		}
		// Add the L2Object spawn in the _allobjects of L2World
		L2World.getInstance().storeObject(this);

		// these can synchronize on others instancies, so they're out of
		// synchronized, to avoid deadlocks

		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		getPosition().getWorldRegion().addVisibleObject(this);

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
		if (Config.ASSERT)
			assert getPosition().getWorldRegion() == null;

		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;

			if (x > L2World.MAP_MAX_X)
				x = L2World.MAP_MAX_X - 5000;
			if (x < L2World.MAP_MIN_X)
				x = L2World.MAP_MIN_X + 5000;
			if (y > L2World.MAP_MAX_Y)
				y = L2World.MAP_MAX_Y - 5000;
			if (y < L2World.MAP_MIN_Y)
				y = L2World.MAP_MIN_Y + 5000;

			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
		}

		// Add the L2Object spawn in the _allobjects of L2World
		L2World.getInstance().storeObject(this);
		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		getPosition().getWorldRegion().addVisibleObject(this);
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
		if (_knownList == null)
			_knownList = new ObjectKnownList(this);

		return _knownList;
	}

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
		_name = (value == null ? null : value.intern());
	}

	/**
	 * @return the object id
	 */
	public final Integer getObjectId()
	{
		return _objectId;
	}

	/**
	 * @return the appearance
	 */
	public final ObjectPoly getPoly()
	{
		if (_poly == null)
			_poly = new ObjectPoly(this);
		return _poly;
	}

	/**
	 * @return the position
	 */
	public final ObjectPosition getPosition()
	{
		if (_position == null)
			_position = new ObjectPosition(this);
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
	* @return The id of the instance zone the object is in - id 0 is global
	* since everything like dropped items, mobs, players can be in a instanciated area, it must be in l2object
	*/
	public int getInstanceId()
	{
		return _instanceId;
	}

	/**
	* @param instanceId The id of the instance zone the object is in - id 0 is global
	*/
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;

		// If we change it for visible objects, me must clear & revalidate knownlists
		if (_isVisible && _knownList != null)
		{
			if (this instanceof L2PcInstance)
			{
				// We don't want some ugly looking disappear/appear effects, so don't update
				// the knownlist here, but players usually enter instancezones through teleporting
				// and the teleport will do the revalidation for us.
			}
			else
			{
				decayMe();
				spawnMe();
			}
		}
	}
	
	/**
	 * Basic implementation of toString to print the object id
	 */
	@Override
	public String toString()
	{
		return String.valueOf(getObjectId());
	}
	
	public L2PcInstance getActingPlayer()
	{
		return null;
	}
	
	public final static L2PcInstance getActingPlayer(L2Object obj)
	{
		return (obj == null ? null : obj.getActingPlayer());
	}
	
	public L2Summon getActingSummon()
	{
		return null;
	}
	
	public final static L2Summon getActingSummon(L2Object obj)
	{
		return (obj == null ? null : obj.getActingSummon());
	}
	
	public boolean isInFunEvent()
	{
		L2PcInstance player = getActingPlayer();
		
		return (player == null ? false : player.isInFunEvent());
	}
}
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

import java.util.Set;

import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.instancemanager.FriendListManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.FriendPacket;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.FriendPacket.FriendAction;

/**
 * @author G1ta0
 */
public final class L2FriendList
{
	private final L2PcInstance _owner;
	private final Set<Integer> _set;
	
	public L2FriendList(L2PcInstance owner)
	{
		_owner = owner;
		_set = FriendListManager.getInstance().getFriendList(owner.getObjectId());
	}
	
	public boolean contains(Integer objectId)
	{
		return _set.contains(objectId);
	}
	
	public boolean contains(L2PcInstance player)
	{
		return player != null && contains(player.getObjectId());
	}
	
	public Iterable<Integer> getFriendIds()
	{
		return _set;
	}
	
	public boolean canAddAsFriend(L2PcInstance friend)
	{
		if (friend == null || friend.isOnline() == 0 || friend.getAppearance().isInvisible())
			_owner.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
		else if (friend == _owner)
			_owner.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
		else if (BlockList.isBlocked(_owner, friend))
			_owner.sendMessage(friend.getName() + " is already on your Ignore List.");
		else if (BlockList.isBlocked(friend, _owner))
			_owner.sendMessage("You are in target's Ignore List.");
		else if (contains(friend))
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_FRIENDS_LIST).addPcName(friend));
		else
			return true;
		
		return false;
	}
	
	public void add(L2PcInstance friend)
	{
		if (!canAddAsFriend(friend))
			return;
		
		if (FriendListManager.getInstance().insert(_owner.getObjectId(), friend.getObjectId()))
		{
			_owner.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
			
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ADDED_TO_FRIENDS).addPcName(friend));
			_owner.sendPacket(new FriendPacket(FriendAction.ADD_FRIEND, friend.getObjectId()));
			
			friend.sendPacket(new SystemMessage(SystemMessageId.C1_JOINED_AS_FRIEND).addPcName(_owner));
			friend.sendPacket(new FriendPacket(FriendAction.ADD_FRIEND, _owner.getObjectId()));
		}
		else
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_FRIENDS_LIST).addPcName(friend));
	}
	
	public void remove(String name)
	{
		Integer objId = CharNameTable.getInstance().getObjectIdByName(name);
		
		if (objId != null && FriendListManager.getInstance().remove(_owner.getObjectId(), objId))
		{
			name = CharNameTable.getInstance().getNameByObjectId(objId);
			
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(name));
			_owner.sendPacket(new FriendPacket(FriendAction.REMOVE_FRIEND, objId));
			
			L2PcInstance friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
			{
				friend.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addPcName(_owner));
				friend.sendPacket(new FriendPacket(FriendAction.REMOVE_FRIEND, _owner.getObjectId()));
			}
		}
		else
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST).addString(name));
	}
}

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
import com.l2jfree.gameserver.datatables.CharNameTable.ICharacterInfo;
import com.l2jfree.gameserver.instancemanager.BlockListManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * @author luisantonioa
 */
public final class BlockList
{
	private final L2PcInstance _owner;
	private final Set<String> _set;
	
	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_set = BlockListManager.getInstance().getBlockList(_owner.getObjectId());
	}
	
	public void add(String name)
	{
		final ICharacterInfo player = CharNameTable.getInstance().getICharacterInfoByName(name);
		if (player == null || player.getObjectId() == _owner.getObjectId())
		{
			_owner.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
			return;
		}
		
		if (_owner.getFriendList().contains(player.getObjectId()))
		{
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_FRIENDS_LIST).addPcName(player));
			return;
		}
		
		if (player.isGM())
		{
			_owner.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
			return;
		}
		
		if (_set.add(player.getName()))
		{
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addPcName(player));
			player.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(_owner));
			BlockListManager.getInstance().insert(_owner, player.getName());
		}
		else
			_owner.sendMessage(player.getName() + " was already on your Ignore List.");
	}
	
	public void remove(String name)
	{
		if (_set.remove(name))
		{
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(name));
			
			BlockListManager.getInstance().remove(_owner, name);
		}
		else
			_owner.sendMessage(name + " wasn't on your Ignore List.");
	}
	
	public boolean contains(L2PcInstance player)
	{
		if (player == null || player.isGM())
			return false;
		
		return _owner.getMessageRefusal() || _set.contains(player.getName());
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance player)
	{
		return listOwner.getBlockList().contains(player);
	}
	
	public void setBlockingAll(boolean blockingAll)
	{
		_owner.setMessageRefusal(blockingAll);
		
		if (blockingAll)
			_owner.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
		else
			_owner.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
	}
	
	public void sendListToOwner()
	{
		_owner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		
		int i = 1;
		for (String name : _set)
			_owner.sendMessage((i++) + ". " + name);
		
		_owner.sendMessage("");
	}
}

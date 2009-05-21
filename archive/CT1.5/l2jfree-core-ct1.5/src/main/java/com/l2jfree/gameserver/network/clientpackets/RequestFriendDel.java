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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.model.L2FriendList;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;


public class RequestFriendDel extends L2GameClientPacket{
	
	private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";

	private String _name;
	
    @Override
    protected void readImpl()
    {
        _name = readS();
    }

    @Override
    protected void runImpl()
	{
		SystemMessage sm;
		L2PcInstance activeChar = getClient().getActiveChar();
		
        if (activeChar == null) 
            return;
        
        L2PcInstance friend = L2World.getInstance().getPlayer(_name);

        if (friend == activeChar)
        {
        	return;
        }
        else if (!L2FriendList.isInFriendList(activeChar, _name))
        { 
            // Target is not in friend list.
        	sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
		    sm = null;
        }
        else if (friend != null)
        {
        	L2FriendList.removeFromFriendList(activeChar, friend);
            // Notify that target deleted from friends list.
 			sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
            // Notify target that requester deleted from friends list.
			sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(activeChar.getName());
			friend.sendPacket(sm);
        }
        else
        {
        	L2FriendList.removeFromFriendList(activeChar, _name);
            // Notify that target deleted from friends list.
 			sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
        }
	}
	
	@Override
	public String getType()
	{
		return _C__61_REQUESTFRIENDDEL;
	}
}

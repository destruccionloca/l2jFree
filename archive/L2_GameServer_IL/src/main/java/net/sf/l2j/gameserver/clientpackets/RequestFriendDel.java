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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


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

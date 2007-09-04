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
import net.sf.l2j.gameserver.serverpackets.AskJoinFriend;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendInvite extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";

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
        
    	if (friend == null)
        {
    	    //Target is not found in the game.
    		sm = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
    		activeChar.sendPacket(sm);
    	}
        else if (friend == activeChar)
        {
    	    //You cannot add yourself to your own friend list.
        	sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
        	activeChar.sendPacket(sm);
    	}
        else if (L2FriendList.isInFriendList(activeChar, friend))
        { 
            // Target is already in friend list.
        	sm = new SystemMessage(SystemMessageId.S1_ALREADY_ON_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
        }
        else if (!friend.isProcessingRequest())
		{
		    activeChar.onTransactionRequest(friend);
		    sm = new SystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS);
		    sm.addString(activeChar.getName());
		    friend.sendPacket(sm);
		    
		    AskJoinFriend ajf = new AskJoinFriend(activeChar.getName());
		    friend.sendPacket(ajf);  	    
    	} 
        else 
        {
    		sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
			activeChar.sendPacket(sm);
    	}
    	
    	sm = null;
	}
	
	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}

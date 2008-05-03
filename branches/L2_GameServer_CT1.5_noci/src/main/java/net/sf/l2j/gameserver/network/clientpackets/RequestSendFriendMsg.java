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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2FriendSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Recieve Private (Friend) Message - 0xCC
 * 
 * Format: c SS
 * 
 * S: Message
 * S: Receiving Player
 * 
 * @author Tempy
 * 
 */
public class RequestSendFriendMsg extends L2GameClientPacket
{
    private static final String _C__CC_REQUESTSENDMSG = "[C] CC RequestSendMsg";
	private static Log _logChat = LogFactory.getLog("chat");
    
    private String _message;
    private String _reciever;
    
    @Override
    protected void readImpl()
    {
        _message = readS();
        _reciever = readS();
    }

    @Override
    protected void runImpl()
    {
    	L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null) return;
        
        L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
        
        if (targetPlayer == null && _message != null) 
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
        	return;
        }
        
		if (Config.LOG_CHAT) 
		{
			_logChat.info("PRIV_MSG" + "[" + activeChar.getName() + " to "+ _reciever +"]" + _message);
		}
        
        L2FriendSay frm = new L2FriendSay(activeChar.getName(), _reciever, _message);
        targetPlayer.sendPacket(frm);
    }

    @Override
    public String getType()
    {
        return _C__CC_REQUESTSENDMSG;
    }
}

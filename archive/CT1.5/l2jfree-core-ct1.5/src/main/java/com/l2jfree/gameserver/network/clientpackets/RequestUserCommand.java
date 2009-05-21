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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.IUserCommandHandler;
import com.l2jfree.gameserver.handler.UserCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUserCommand extends L2GameClientPacket
{
	private static final String _C__AA_REQUESTUSERCOMMAND = "[C] aa RequestUserCommand";
	static Log _log = LogFactory.getLog(RequestUserCommand.class.getName());
			
	private int _command;
	
	/**
	 * packet type id 0xaa
	 * format:	cd
	 *  
	 * @param rawPacket
	 */
    @Override
    protected void readImpl()
    {
        _command = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null)
            return;

        IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
        
        if (handler == null)
        {
            player.sendMessage("User command ID "+_command+" is not implemented yet.");
        }
        else
        {
            handler.useUserCommand(_command, getClient().getActiveChar());
        }
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__AA_REQUESTUSERCOMMAND;
	}
}

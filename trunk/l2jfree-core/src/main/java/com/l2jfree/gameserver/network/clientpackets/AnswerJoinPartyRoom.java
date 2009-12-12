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

import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.L2PartyRoom;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Format: (ch) d
 * @author -Wooden- (format)
 * @author Myzreal (implementation)
 */
public class AnswerJoinPartyRoom extends L2GameClientPacket
{
    private static final String _C__D0_30_ANSWERJOINPARTYROOM = "[C] D0:30 AnswerJoinPartyRoom";

    private int _response;

    @Override
    protected void readImpl()
    {
        _response = readD();
    }

    @Override
    protected void runImpl()
    {
    	L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;

		if (Shutdown.isActionDisabled(DisableType.PC_ITERACTION))
        {
        	requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
            return;
        }

		L2PcInstance requester = activeChar.getActiveRequester();
		if (requester == null)
		{
			sendAF();
			return;
		}

		if (_response == 1) // takes care of everything
			L2PartyRoom.tryJoin(activeChar, requester.getPartyRoom(), true);
		else
			sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);

		// Clears requesting status
		activeChar.setActiveRequester(null);
		requester.onTransactionResponse();

		sendAF();
    }

    @Override
    public String getType()
    {
        return _C__D0_30_ANSWERJOINPARTYROOM;
    }
}

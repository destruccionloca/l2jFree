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

import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2PartyRoom;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.JoinParty;

/**
 *  sample
 *  2a
 *  01 00 00 00
 * 
 * format (c) d
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerJoinParty";

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		L2PcInstance requestor = player.getActiveRequester();
        if (requestor == null)
        {
        	sendAF();
        	return;
        }

		requestor.sendPacket(new JoinParty(_response));

		if (_response == 1)
		{
			if (requestor.getParty() != null)
			{
				if (requestor.getParty().getMemberCount() >= 9)
				{
					requestor.sendPacket(SystemMessageId.PARTY_FULL);
					requestFailed(SystemMessageId.PARTY_FULL);
					return;
				}
			}
			player.joinParty(requestor.getParty());
		}
		else
		{
			requestor.sendPacket(SystemMessageId.PLAYER_DECLINED_PARTY);

			L2Party party = requestor.getParty();
			//activate garbage collection if there are no other members in party (happens when we were creating a new one)
			if (party != null && party.getMemberCount() == 1)
			{
				L2PartyRoom room = party.getPartyRoom();
				if (room != null)
					room.setParty(null);
				party.setPartyRoom(null);
				requestor.setParty(null);
			}
		}

		sendAF();

		if (requestor.getParty() != null)
			requestor.getParty().setPendingInvitation(false); // if party is null, there is no need of decreasing
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

	@Override
	public String getType()
	{
		return _C__2A_REQUESTANSWERPARTY;
	}
}

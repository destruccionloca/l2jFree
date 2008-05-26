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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.JoinParty;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 *  sample
 *  2a 
 *  01 00 00 00
 * 
 *  format  cdd
 * 
 * 
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerJoinParty";
	//private final static Log _log = LogFactory.getLog(RequestAnswerJoinParty.class.getName());
	
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
		if(player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
				return;

			JoinParty join = new JoinParty(_response);
			requestor.sendPacket(join);

			if (_response == 1) 
			{
				if(requestor.getParty()!=null)
				{
					if(requestor.getParty().getMemberCount() >= 9)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
						requestor.sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
						return;
					}
				}
				player.joinParty(requestor.getParty());
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.PLAYER_DECLINED);
				requestor.sendPacket(msg);
				msg = null;

				//activate garbage collection if there are no other members in party (happens when we were creating new one) 
				if (requestor.getParty() != null && requestor.getParty().getMemberCount() == 1) requestor.setParty(null);
			}
			if (requestor.getParty() != null)
				requestor.getParty().decreasePendingInvitationNumber(); // if party is null, there is no need of decreasing

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__2A_REQUESTANSWERPARTY;
	}
}

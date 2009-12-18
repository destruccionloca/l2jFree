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

import com.l2jfree.gameserver.instancemanager.PartyRoomManager;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2PartyRoom;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Format (ch) dd
 * @author -Wooden- (format)
 * @author Myzreal & savormix (implementation)
 */
public class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private static final String	_C__D0_0B_REQUESTWITHDRAWPARTYROOM	= "[C] D0:0B RequestWithdrawPartyRoom";

	private int					_roomId;
	//private int					_data2;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		/*_data2 = */readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;

		L2Party party = activeChar.getParty();
		if (party != null && party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
		{
			requestFailed(SystemMessageId.COULD_NOT_LEAVE_PARTY);
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null && room.getId() == _roomId)
		{
			if (room.getLeader() == activeChar)
				PartyRoomManager.getInstance().removeRoom(_roomId);
			else if (party != null)
				party.removePartyMember(activeChar, false);
			else
				room.removeMember(activeChar, false);
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_0B_REQUESTWITHDRAWPARTYROOM;
	}
}

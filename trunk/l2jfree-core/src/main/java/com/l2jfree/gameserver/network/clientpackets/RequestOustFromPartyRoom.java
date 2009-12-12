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
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Sent when a player selects a party room member and clicks the
 * "Kick" button.
 * format (ch) d
 * @author -Wooden- (format)
 * @author Myzreal (implementation)
 */
public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_09_REQUESTOUSTFROMPARTYROOM = "[C] D0:09 RequestOustFromPartyRoom";

	private int _objectId;

    @Override
    protected void readImpl()
    {
    	_objectId = readD();
    }

	@Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;
		L2PcInstance target = L2World.getInstance().findPlayer(_objectId);
		if (target == null || target == activeChar)
		{
			sendAF();
			return;
		}

		L2Party party = target.getParty();
		if (party != null && party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
		{
			activeChar.sendMessage("You can't dismiss party members when you are in Dimensional Rift.");
			sendAF();
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null && room.getLeader() == activeChar)
		{
			if (party != null)
				party.removePartyMember(target, true);
			else
				room.removeMember(target, true);
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__D0_09_REQUESTOUSTFROMPARTYROOM;
	}
}

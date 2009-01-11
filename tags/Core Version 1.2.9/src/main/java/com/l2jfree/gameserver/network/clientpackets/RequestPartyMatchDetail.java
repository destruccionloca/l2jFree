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

/**
 * Format: (ch) dd
 * @author  Crion/kombat
 */

public class RequestPartyMatchDetail extends L2GameClientPacket
{
	private static final String _C__71_REQUESTPARTYMATCHDETAIL = "[C] 71 RequestPartyMatchDetail";

	private int _roomId;
	@SuppressWarnings("unused")
	private int _mode;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_mode = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_roomId > 0)
		{
			// Join the room with this number and send PartyMatchDetail
		}
		else
		{
			// Player clicked "Auto Join" button
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__71_REQUESTPARTYMATCHDETAIL;
	}
}

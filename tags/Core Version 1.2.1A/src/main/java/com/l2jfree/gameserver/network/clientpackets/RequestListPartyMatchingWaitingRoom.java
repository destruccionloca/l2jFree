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
 * Format: (ch) dddd
 * @author  Crion/kombat
 * 
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static final String _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:16 RequestListPartyMatchingWaitingRoom";

	@SuppressWarnings("unused")
	private int _page;
	@SuppressWarnings("unused")
	private boolean _showAll;
	private int _minLevel;
	private int _maxLevel;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_minLevel = readD();
		_maxLevel = readD();
		_showAll = readD() == 1; // client sends 0 if in party room, 1 if not in party room. If you are in party room, only players with matching level are shown.
	}

	/**
	 * @see com.l2jfree.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_minLevel < 1)
			_minLevel = 1;
		else if (_minLevel > 85)
			_minLevel = 85;
		if (_maxLevel < _minLevel)
			_maxLevel = _minLevel;
		else if (_maxLevel > 85)
			_maxLevel = 85;

		// Send waiting list packet here
	}

	/**
	 * @see com.l2jfree.gameserver.network.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM;
	}
}
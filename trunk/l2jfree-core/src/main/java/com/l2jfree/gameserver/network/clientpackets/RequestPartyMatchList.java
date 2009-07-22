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
 * Format:(ch) ddddds
 * @author  Crion/kombat
 */

public class RequestPartyMatchList extends L2GameClientPacket
{
	private static final String _C__80_REQUESTPARTYMATCHLIST = "[C] 80 RequestPartyMatchList";

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_maxMembers = readD();
		_minLevel = readD();
		_maxLevel = readD();
		_lootDist = readD();
		_roomTitle = readS();
	}

	@SuppressWarnings("unused")
	private int _lootDist;
	@SuppressWarnings("unused")
	private int _maxMembers;
	@SuppressWarnings("unused")
	private int _minLevel;
	@SuppressWarnings("unused")
	private int _maxLevel;
	@SuppressWarnings("unused")
	private int _roomId;
	@SuppressWarnings("unused")
	private String _roomTitle;

	@Override
	protected void runImpl()
	{
		//TODO: Implementation RequestPartyMatchList
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		
		// This packet is used to create a party room.
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__80_REQUESTPARTYMATCHLIST;
	}
}

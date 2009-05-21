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
package com.l2jfree.gameserver.network.serverpackets;


/**
 * Format:(c) d d[dsddddds]
 * @author  Crion/kombat
 */

public class ListPartyWaiting extends L2GameServerPacket
{
	private static final String S_9C_LISTPARTYWAITING = "[S] 9c ListPartyWaiting";

	public ListPartyWaiting()
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9c);

		// ??
		writeD(0);

		writeD(0x00); // Size of party room list
		// [
		//    D   party room id
		//    S   party room title
		//    D   party room location id (from 0 to 15)
		//    D   party room min level
		//    D   party room max level
		//    D   how many ppl currently inside
		//    D   max member count (3-12)
		//    S   leader name
		// ]
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return S_9C_LISTPARTYWAITING;
	}
}

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
 * Format:(c) dddddds
 * @author  Crion/kombat
 */
public class PartyMatchDetail extends L2GameServerPacket
{
	private static final String _S__B0_PARTYMATCHDETAIL = "[S] 97 PartyMatchDetail";
	
	/**
	 * @param allPlayers
	 */
	public PartyMatchDetail()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		
		// This packet shows the inside of a party matching room
		
		// D   room id
		// D   room max member count
		// D   room min level
		// D   room max level
		// D   loot distri - 0 Finders Keepers, 1 Random, 2 Random Inc, 3 By Turn, 4 By Turn Inc
		// D   room location (from 0 to 15) 1 Talking, 2 Gludio, 3 Dark Elven, 4 Elven, 5 Dion, 6 Giran, 7 Neutral Zone, 9 Schuttgart, 10 Oren, 11 Hunters, 12 Innadril, 13 Aden, 14 Rune, 15 Goddard
		//  S  room title
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__B0_PARTYMATCHDETAIL;
	}
}

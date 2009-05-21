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
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format:(ch) d d [dsdddd]
 * @author  Crion/kombat
 */

public class ExPartyRoomMember extends L2GameServerPacket
{
	public ExPartyRoomMember()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x08);

		// 0x01 - we are leader
		// 0x00 - we are not leader
		writeD(0x00);

		writeD(0x00);   // D     size
		// [
		//     D    player object id
		//     S    player name
		//     D    player class id
		//     D    player level
		//     D    player region (from 0 to 15)
		//     D     1 leader     2  party member    0 not party member
		// ]
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "FE_08_ExPartyRoomMember";
	}
}

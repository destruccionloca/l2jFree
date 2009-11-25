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
 * When teleported anywhere in CC, you get this packet
 * every time containing the same id while logged in.<BR>
 * No further use tested.
 * @author savormix
 */
public class ExBR_PremiumState extends L2GameServerPacket
{
	private static final String _S__FE_AA_EXBRPREMIUMSTATE = "[S] FE:AA ExBR_PremiumState";

	private final int _state;

	public ExBR_PremiumState(int state)
	{
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xaa);

		writeD(_state);

		writeC(0x00);
	}

	@Override
	public String getType()
	{
		return _S__FE_AA_EXBRPREMIUMSTATE;
	}
}

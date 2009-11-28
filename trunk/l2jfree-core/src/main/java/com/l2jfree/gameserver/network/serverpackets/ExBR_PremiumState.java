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
 * This packet is merely an information packet telling why
 * the player failed to purchase/receive a premium product.<BR>
 * Available messages begin with <code>STATE_</code>.<BR>
 * After client receives this packet, it will send ExBR_GamePoint.
 * <BR><BR>
 * 
 * Packet with the same obfuscated opcode is sent each time you
 * teleport/are teleported. Different byte values after
 * object id don't seem to have any effect.
 * @author savormix
 */
public class ExBR_PremiumState extends L2GameServerPacket
{
	private static final String _S__FE_AA_EXBRPREMIUMSTATE = "[S] FE:AA ExBR_PremiumState";

	public static final int STATE_NOT_ENOUGH_POINTS = -1;
	public static final int STATE_WRONG_PRODUCT = -2; // also -5
	public static final int STATE_INVENTORY_FULL = -4;
	public static final int STATE_SALE_PERIOD_ENDED = -7; // also -8
	public static final int STATE_WRONG_USER_STATE = -9; // also -11
	public static final int STATE_WRONG_PRODUCT_ITEM = -10;

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
	}

	@Override
	public String getType()
	{
		return _S__FE_AA_EXBRPREMIUMSTATE;
	}
}

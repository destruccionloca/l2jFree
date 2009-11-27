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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Sent (broadcast?) each time you teleport/are teleported.<BR>
 * Different states don't seem to have any effect.
 * @author savormix
 */
public class ExBR_PremiumState extends L2GameServerPacket
{
	private static final String _S__FE_AA_EXBRPREMIUMSTATE = "[S] FE:AA ExBR_PremiumState";

	private final int _charId;
	private final int _state;

	private ExBR_PremiumState(int pcObjectId, int state)
	{
		_charId = pcObjectId;
		_state = state;
	}

	public ExBR_PremiumState(L2PcInstance player)
	{
		this(player.getObjectId(), 0);
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xaa);

		writeD(_charId);
		writeC(_state);
	}

	@Override
	public String getType()
	{
		return _S__FE_AA_EXBRPREMIUMSTATE;
	}
}

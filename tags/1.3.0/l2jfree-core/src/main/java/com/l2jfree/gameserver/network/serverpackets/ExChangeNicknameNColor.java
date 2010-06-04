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
import com.l2jfree.gameserver.network.L2GameClient;

/**
 * Opens a dialog to change your title (nickname) and name color. 
 * @author savormix
 */
public final class ExChangeNicknameNColor extends StaticPacket
{
	private static final String _S__EXCHANGENICKNAMENCOLOR = "[S] FE:83 ExChangeNicknameNColor";
	public static final ExChangeNicknameNColor PACKET = new ExChangeNicknameNColor();

	private ExChangeNicknameNColor()
	{
	}

	@Override
	protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
	{
		writeC(0xFE);
		writeH(0x83);
	}

	@Override
	public String getType()
	{
		return _S__EXCHANGENICKNAMENCOLOR;
	}
}

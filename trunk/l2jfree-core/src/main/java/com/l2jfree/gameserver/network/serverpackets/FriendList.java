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

import java.util.Map;

import com.l2jfree.gameserver.model.L2FriendList;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;


/**
 * Support for "Chat with Friends" dialog. 
 * 
 * Format: ch (hdSdh)
 * h: Total Friend Count
 * 
 * h: Unknown
 * d: Player Object ID
 * S: Friend Name
 * d: Online/Offline
 * h: Unknown
 * 
 * @author Tempy
 *
 */
public class FriendList extends L2GameServerPacket
{
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";

	private L2PcInstance _activeChar;

	public FriendList(L2PcInstance cha)
	{
		_activeChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)  
			return;  

		if (L2FriendList.getFriendList(_activeChar).size() > 0)
		{
			writeC(0x75);
			writeH(L2FriendList.getFriendListNames(_activeChar).length);

			for (Map.Entry<Integer, String> _friend : L2FriendList.getFriendList(_activeChar).entrySet())
			{
				L2PcInstance friend = L2World.getInstance().getPlayer(_friend.getValue());
				writeH(0); // ??
				writeD(_friend.getKey());
				writeS(_friend.getValue());
				if (friend == null)
				{
					writeD(0); // offline
					writeD(0x00030b7a); // object id if online
				}
				else
				{
					writeD(1); // online
					writeD(0x00); // ??
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}

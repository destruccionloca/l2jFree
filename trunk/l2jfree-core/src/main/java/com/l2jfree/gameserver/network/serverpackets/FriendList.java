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

import java.util.Set;

import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog. This packet is sent only at login.
 * Format: cd (dSdd) d:
 * Total Friend Count d:
 * Player Object ID S:
 * Friend Name d:
 * Online/Offline d:
 * Unknown (0 if offline)
 * 
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	
	private static final String	_S__FA_FRIENDLIST	= "[S] 75 FriendList";
	private final Set<Integer>		_friends;
	
	public FriendList(L2PcInstance character)
	{
		_friends = (Set<Integer>) character.getFriendList().getFriendIds();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x75);
		if (_friends != null)
		{
			writeD(_friends.size());
			
			for (Integer objId : _friends)
			{
				String name = CharNameTable.getInstance().getNameByObjectId(objId);
				L2PcInstance player = L2World.getInstance().findPlayer(objId);
				boolean isOnline = (player != null && player.isOnline() == 1) ? true : false;
				
				writeD(objId);
				writeS(name);
				writeD(isOnline ? 0x01 : 0x00); // online
				writeD(isOnline ? objId : 0x00); // object id if online
			}
		}
		else
			writeD(0);
	}
	
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}

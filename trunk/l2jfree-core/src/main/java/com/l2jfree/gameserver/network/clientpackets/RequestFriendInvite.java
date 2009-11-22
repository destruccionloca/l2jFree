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

import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.FriendAddRequest;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null) return;

		L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		if (friend == null || (friend.getAppearance().isInvisible() && friend.isGM()))
			sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		else if (friend == activeChar)
			sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
		else if (activeChar.getFriendList().contains(friend))
			sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_LIST).addPcName(friend));
		else if (friend.isProcessingRequest())
			sendPacket(new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addPcName(friend));
		else
		{
			activeChar.onTransactionRequest(friend);

			friend.sendPacket(new SystemMessage(SystemMessageId.C1_REQUESTED_TO_BECOME_FRIENDS).addPcName(activeChar));
			friend.sendPacket(new FriendAddRequest(activeChar.getName()));
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}

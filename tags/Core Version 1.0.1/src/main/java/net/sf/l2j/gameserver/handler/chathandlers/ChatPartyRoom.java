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
package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

/**
 *
 * @author  Noctarius
 */

public class ChatPartyRoom implements IChatHandler
{
	private SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Party_Room };

	/**
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#getChatTypes()
	 */
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#useChatHandler(net.sf.l2j.gameserver.character.player.L2PcInstance, java.lang.String, net.sf.l2j.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		String charName = "";
		int charObjId = 0;

		if (activeChar != null)
		{
			charName = activeChar.getName();
			charObjId = activeChar.getObjectId();
		}

		if (activeChar.isInParty())
		{
			if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
			{
				CreatureSay cs = new CreatureSay(charObjId, chatType.getId(), charName, text);
				activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
			}
		}
	}
}

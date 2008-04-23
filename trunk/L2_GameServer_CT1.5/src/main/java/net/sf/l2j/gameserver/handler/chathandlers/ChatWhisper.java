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
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author  Noctarius
 */
public class ChatWhisper implements IChatHandler
{
	private SystemChatChannelId[] _chatTypes = { SystemChatChannelId.Chat_Tell };

	/**
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#getChatType()
	 */
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#useChatHandler(net.sf.l2j.gameserver.character.player.L2PcInstance, net.sf.l2j.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		L2PcInstance receiver = L2World.getInstance().getPlayer(target);
		
		if (receiver != null && !BlockList.isBlocked(receiver, activeChar))
		{
			if (!receiver.getMessageRefusal() || activeChar.isGM())
			{
				receiver.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text));
				receiver.broadcastSnoop(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);
				activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(),  chatType.getId(), "->" + receiver.getName(), text));
				activeChar.broadcastSnoop(activeChar.getObjectId(), chatType.getId(), "->"+receiver.getName(), text);
			}
			else
				activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
			sm.addString(target);
			activeChar.sendPacket(sm);
			sm = null;
		}
	}
}

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
package com.l2jfree.gameserver.handler;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.chathandlers.ChatAll;
import com.l2jfree.gameserver.handler.chathandlers.ChatAlliance;
import com.l2jfree.gameserver.handler.chathandlers.ChatAnnounce;
import com.l2jfree.gameserver.handler.chathandlers.ChatClan;
import com.l2jfree.gameserver.handler.chathandlers.ChatCommander;
import com.l2jfree.gameserver.handler.chathandlers.ChatHero;
import com.l2jfree.gameserver.handler.chathandlers.ChatParty;
import com.l2jfree.gameserver.handler.chathandlers.ChatPartyRoom;
import com.l2jfree.gameserver.handler.chathandlers.ChatPetition;
import com.l2jfree.gameserver.handler.chathandlers.ChatShout;
import com.l2jfree.gameserver.handler.chathandlers.ChatSystem;
import com.l2jfree.gameserver.handler.chathandlers.ChatTrade;
import com.l2jfree.gameserver.handler.chathandlers.ChatWhisper;
import com.l2jfree.gameserver.network.SystemChatChannelId;


import javolution.util.FastMap;


/**
 *
 * @author  Noctarius
 */
public class ChatHandler
{
	private final static Log						_log		= LogFactory.getLog(ChatHandler.class.getName());
	private static ChatHandler						_instance	= null;

	private Map<SystemChatChannelId, IChatHandler>	_datatable;

	public static ChatHandler getInstance()
	{
		if (_instance == null)
			_instance = new ChatHandler();

		return _instance;
	}

	public ChatHandler()
	{
		_datatable = new FastMap<SystemChatChannelId, IChatHandler>();
		registerChatHandler(new ChatAll());
		registerChatHandler(new ChatAlliance());
		registerChatHandler(new ChatAnnounce());
		registerChatHandler(new ChatClan());
		registerChatHandler(new ChatCommander());
		registerChatHandler(new ChatSystem());
		registerChatHandler(new ChatHero());
		registerChatHandler(new ChatParty());
		registerChatHandler(new ChatPartyRoom());
		registerChatHandler(new ChatPetition());
		registerChatHandler(new ChatShout());
		registerChatHandler(new ChatTrade());
		registerChatHandler(new ChatWhisper());
		_log.info("ChatHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerChatHandler(IChatHandler handler)
	{
		SystemChatChannelId chatId[] = handler.getChatTypes();

		for (SystemChatChannelId chat : chatId)
		{
			// Adding handler for each ChatChannelId
			_datatable.put(chat, handler);
		}
	}

	public IChatHandler getChatHandler(SystemChatChannelId chatId)
	{
		return _datatable.get(chatId);
	}

	public int size()
	{
		return _datatable.size();
	}
}
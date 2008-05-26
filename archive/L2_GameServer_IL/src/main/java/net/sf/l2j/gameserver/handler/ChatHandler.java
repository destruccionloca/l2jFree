/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.l2j.gameserver.handler.chathandlers.ChatAll;
import net.sf.l2j.gameserver.handler.chathandlers.ChatAlliance;
import net.sf.l2j.gameserver.handler.chathandlers.ChatAnnounce;
import net.sf.l2j.gameserver.handler.chathandlers.ChatClan;
import net.sf.l2j.gameserver.handler.chathandlers.ChatCommander;
import net.sf.l2j.gameserver.handler.chathandlers.ChatHero;
import net.sf.l2j.gameserver.handler.chathandlers.ChatParty;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyRoom;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPetition;
import net.sf.l2j.gameserver.handler.chathandlers.ChatShout;
import net.sf.l2j.gameserver.handler.chathandlers.ChatSystem;
import net.sf.l2j.gameserver.handler.chathandlers.ChatTrade;
import net.sf.l2j.gameserver.handler.chathandlers.ChatWhisper;

import javolution.util.FastMap;

import net.sf.l2j.gameserver.network.SystemChatChannelId;

/**
 *
 * @author  Noctarius
 */
public class ChatHandler
{
	private final static Log _log = LogFactory.getLog(ChatHandler.class.getName());
	private static ChatHandler _instance = null;
	
	private Map<SystemChatChannelId, IChatHandler> _datatable;
	
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

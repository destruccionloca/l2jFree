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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.ChatHandler;
import com.l2jfree.gameserver.handler.IChatHandler;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.handler.VoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemChatChannelId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;

/**
 * This class represents a packet sent by the server when a chat message is sent.
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public class Say2 extends L2GameClientPacket
{
	private static final String		_C__38_SAY2	= "[C] 38 Say2";
	private final static Log		_log		= LogFactory.getLog(Say2.class.getName());
	private static Log				_logChat	= LogFactory.getLog("chat");

	private String					_text;
	private SystemChatChannelId		_type;
	private String					_target;

	private static final String[]	LINKED_ITEM	=
	{ "Type=", "ID=", "Color=", "Underline=", "Title=" };

	/**
	 * packet type id 0x38
	 * format:      cSd (S)
	 */
	@Override
	protected void readImpl()
	{
		_text = readS().replaceAll("\\\\n", "");
		_type = SystemChatChannelId.getChatType(readD());
		_target = _type == SystemChatChannelId.Chat_Tell ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warn("[Say2.java] Active Character is null.");
			return;
		}

		// If no or wrong channel is used - return
		if (_type == SystemChatChannelId.Chat_None ||
				_type == SystemChatChannelId.Chat_Announce ||
				_type == SystemChatChannelId.Chat_Critical_Announce ||
				_type == SystemChatChannelId.Chat_System ||
				_type == SystemChatChannelId.Chat_Custom ||
					(_type == SystemChatChannelId.Chat_GM_Pet && !activeChar.isGM()))
		{
			//is this uninformative message still useful?
			//_log.warn("[Say2.java] Illegal chat channel was used.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// If player is chat banned
		if (ObjectRestrictions.getInstance().checkRestriction(activeChar, AvailableRestriction.PlayerChat)
				&& _type != SystemChatChannelId.Chat_User_Pet
				&& _type != SystemChatChannelId.Chat_Tell)
		{
			requestFailed(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}

		if (activeChar.isCursedWeaponEquipped())
		{
			switch (_type)
			{
			case Chat_Shout:
			case Chat_Market:
				requestFailed(SystemMessageId.SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON);
				return;
			}
		}

		// If player is jailed
		if ((activeChar.isInJail() || activeChar.isInsideZone(L2Zone.FLAG_JAIL)) && Config.JAIL_DISABLE_CHAT && !activeChar.isGM())
		{
			if (_type != SystemChatChannelId.Chat_User_Pet && _type != SystemChatChannelId.Chat_Normal)
			{
				requestFailed(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}
		}

		// If Petition and GM use GM_Petition Channel
		if (_type == SystemChatChannelId.Chat_User_Pet && activeChar.isGM())
			_type = SystemChatChannelId.Chat_GM_Pet;

		if (!Config.GM_ALLOW_CHAT_INVISIBLE && activeChar.getAppearance().isInvisible() &&
				(_type == SystemChatChannelId.Chat_Normal ||
						_type == SystemChatChannelId.Chat_Shout ||
						_type == SystemChatChannelId.Chat_Market))
		{
			requestFailed(SystemMessageId.NOT_CHAT_WHILE_INVISIBLE);
			return;
		}

		if (Config.BAN_CLIENT_EMULATORS)
		{
			//Under no circumstances the official client will send a 400 character message
			//If there are no linked items in the message, you can only input 105 characters
			if (_text.length() > 400 || (_text.length() > 105 && !containsLinkedItems()))
			{
				Util.handleIllegalPlayerAction(activeChar, "Bot usage for chatting by " + activeChar,
						IllegalPlayerAction.PUNISH_KICKBAN);
			}
		}
		else if (_text.length() > 400)
		{
			requestFailed(SystemMessageId.DONT_SPAM);
			//prevent crashing official clients
			return;
		}

		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
			for (String pattern : Config.FILTER_LIST)
				_text = _text.replaceAll("(?i)" + pattern, "-_-");

		if (_text.startsWith("."))
		{
			String[] _commandParams = _text.split(" ");

			String command = _commandParams[0].substring(1);
			String params = "";

			// if entered "command text"
			if (_commandParams.length > 1)
				params = _text.substring(1 + command.length()).trim(); // get all text
			else if (activeChar.getTarget() != null)
				params = activeChar.getTarget().getName();

			IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		// Some custom implementation to show how to add channels
		// (for me Chat_System is used for emotes - further informations
		// in ChatSystem.java)
		// else if (_text.startsWith("(")&&
		//		_text.length() >= 5 &&
		//		_type == SystemChatChannelId.Chat_Normal)
		//{
		//	_type = SystemChatChannelId.Chat_System;
		//
		//	_text = _text.substring(1);
		//	_text = "*" + _text + "*";
		//}

		// Log chat to file
		if (Config.LOG_CHAT)
		{
			if (_type == SystemChatChannelId.Chat_Tell)
				_logChat.info(_type.getName() + "[" + activeChar.getName() + " to " + _target + "] " + _text);
			else
				_logChat.info(_type.getName() + "[" + activeChar.getName() + "] " + _text);
		}

		IChatHandler ich = ChatHandler.getInstance().getChatHandler(_type);
		if (ich != null)
			ich.useChatHandler(activeChar, _target, _type, _text);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}

	public void changeString(String newString)
	{
		_text = newString;
	}

	public String getSay()
	{
		return _text;
	}

	private boolean containsLinkedItems()
	{
		for (int i = 0; i < LINKED_ITEM.length; i++)
			if (!_text.contains(LINKED_ITEM[i]))
				return false;
		return true;
	}
}

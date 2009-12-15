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
import com.l2jfree.gameserver.handler.VoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemChatChannelId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.util.Util;

/**
 * This class represents a packet sent by the server when a chat message is
 * sent.
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public class Say2 extends L2GameClientPacket
{
	private static final String		_C__38_SAY2	= "[C] 38 Say2";
	private static final Log		_logChat	= LogFactory.getLog("chat");

	private String					_text;
	private SystemChatChannelId		_type;
	private String					_target;

	private static final String[]	LINKED_ITEM	=
												{ "Type=", "ID=", "Color=", "Underline=", "Title=" };

	/**
	 * packet type id 0x38 format: cSd (S)
	 */
	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = SystemChatChannelId.getChatType(readD());
		_target = _type == SystemChatChannelId.Chat_Tell ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		// If no or wrong channel is used - punish/return
		switch (_type)
		{
		case Chat_None:
		case Chat_Announce:
		case Chat_Critical_Announce:
		case Chat_System:
		case Chat_Custom:
			if (Config.BAN_CLIENT_EMULATORS)
				Util.handleIllegalPlayerAction(activeChar, "Bot usage for chatting with wrong type by " + activeChar);
			else
				sendAF();
			return;
		}

		if (Config.DISABLE_ALL_CHAT)
		{
			requestFailed(SystemMessageId.GM_NOTICE_CHAT_DISABLED);
			return;
		}

		switch (_type)
		{
		case Chat_GM_Pet:
		case Chat_User_Pet:
		case Chat_Tell:
			break;
		default:
			// If player is chat banned
			if (ObjectRestrictions.getInstance().checkRestriction(activeChar, AvailableRestriction.PlayerChat))
			{
				requestFailed(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}
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

		switch (_type)
		{
		case Chat_GM_Pet:
		case Chat_User_Pet:
		case Chat_Normal:
			break;
		default:
			// If player is jailed
			if ((activeChar.isInJail() || activeChar.isInsideZone(L2Zone.FLAG_JAIL)) && Config.JAIL_DISABLE_CHAT && !activeChar.isGM())
			{
				requestFailed(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}
		}

		// If Petition and GM use GM_Petition Channel
		if (_type == SystemChatChannelId.Chat_User_Pet && activeChar.isGM())
			_type = SystemChatChannelId.Chat_GM_Pet;

		switch (_type)
		{
		case Chat_Normal:
		case Chat_Shout:
		case Chat_Market:
			if (!Config.GM_ALLOW_CHAT_INVISIBLE && activeChar.getAppearance().isInvisible())
			{
				requestFailed(SystemMessageId.NOT_CHAT_WHILE_INVISIBLE);
				return;
			}
		}

		if (_text.isEmpty())
		{
			if (Config.BAN_CLIENT_EMULATORS)
				Util.handleIllegalPlayerAction(activeChar, "Bot usage for chatting with empty messages by " + activeChar);
			else
				sendAF();

			_log.warn(activeChar.getName() + ": sending empty text. Possible packet hack!");
			return;
		}

		//Under no circumstances the official client will send a 400 character message
		//If there are no linked items in the message, you can only input 105 characters
		if (_text.length() > 400 || (_text.length() > 105 && !containsLinkedItems()))
		{
			if (Config.BAN_CLIENT_EMULATORS)
				Util.handleIllegalPlayerAction(activeChar, "Bot usage for chatting with too long messages by " + activeChar);
			else
				requestFailed(SystemMessageId.DONT_SPAM);
			//prevent crashing official clients
			return;
		}

		int oldLength = _text.length();

		_text = _text.replaceAll("\\\\n", "");

		if (oldLength != _text.length())
			activeChar.sendPacket(SystemMessageId.DONT_SPAM);

		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
			for (String pattern : Config.FILTER_LIST)
				_text = _text.replaceAll("(?i)" + pattern, "-_-");

		if (VoicedCommandHandler.getInstance().useVoicedCommand(_text, activeChar))
		{
			sendAF();
			return;
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

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}

	private boolean containsLinkedItems()
	{
		for (int i = 0; i < LINKED_ITEM.length; i++)
			if (!_text.contains(LINKED_ITEM[i]))
				return false;
		return true;
	}
}

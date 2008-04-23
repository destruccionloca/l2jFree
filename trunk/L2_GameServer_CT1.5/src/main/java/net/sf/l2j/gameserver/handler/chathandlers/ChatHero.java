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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.instancemanager.IrcManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.util.FloodProtector;

/**
 *
 * @author  Noctarius
 */
public class ChatHero implements IChatHandler
{
	private SystemChatChannelId[] _chatTypes = { SystemChatChannelId.Chat_Hero };

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
		if(Config.IRC_ENABLED && Config.IRC_FROM_GAME_TYPE.equalsIgnoreCase("hero") || Config.IRC_ENABLED && Config.IRC_FROM_GAME_TYPE.equalsIgnoreCase("all")) // added hero voice to IRC like said in the properties files
		{
			IrcManager.getInstance().getConnection().sendChan("12%"+ activeChar.getName() +": " + text);
		}

		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);

		boolean canSpeak = activeChar.isGM();
		
		if(!canSpeak)
		{
			if (activeChar.isHero())
			{
				if(FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_HEROVOICE))
				{
					canSpeak = true;
				}
				else
				{
					activeChar.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
				}
			}
		}
		
		if (canSpeak)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (!(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar)))
				{
					player.sendPacket(cs);
					player.broadcastSnoop(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);
				}
			}
		}
	}
}

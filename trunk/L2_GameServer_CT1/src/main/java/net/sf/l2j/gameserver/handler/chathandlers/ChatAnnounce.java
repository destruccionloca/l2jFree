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
package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;

/**
 *
 * @author  Noctarius
 */
public class ChatAnnounce implements IChatHandler
{
	private SystemChatChannelId[] _chatTypes = { SystemChatChannelId.Chat_Announce, SystemChatChannelId.Chat_Critical_Announce };

	/**
	 * @see de.dc.l2j.gameserver.handler.IChatHandler#getChatType()
	 */
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see de.dc.l2j.gameserver.handler.IChatHandler#useChatHandler(de.dc.l2j.gameserver.character.player.L2PcInstance, java.lang.String, de.dc.l2j.gameserver.network.enums.SystemChatChannelId, java.lang.String)
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
		
		if (chatType == SystemChatChannelId.Chat_Critical_Announce)
			text = "** " + text;
		
		CreatureSay cs = new CreatureSay(charObjId, chatType.getId(), charName, text);
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null){
				player.sendPacket(cs);
				player.broadcastSnoop(charObjId, chatType.getId(), charName, text);
			}
	}
}

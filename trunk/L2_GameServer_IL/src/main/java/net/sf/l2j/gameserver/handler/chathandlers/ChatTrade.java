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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;

/**
 *
 * @author  Noctarius
 */
public class ChatTrade implements IChatHandler
{
	private SystemChatChannelId[] _chatTypes = { SystemChatChannelId.Chat_Market };

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
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);
		
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") ||
				Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
	        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
	            player.sendPacket(cs);
        else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
        {
            int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
	            if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY())) 
                    player.sendPacket(cs);
        }
	}

}

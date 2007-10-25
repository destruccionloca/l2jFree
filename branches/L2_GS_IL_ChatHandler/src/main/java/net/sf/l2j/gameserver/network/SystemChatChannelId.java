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
package net.sf.l2j.gameserver.network;

/**
 *
 * @author  Noctarius
 */
public enum SystemChatChannelId
{
	Chat_Normal ("ALL"),
	Chat_Shout ("SHOUT"),
	Chat_Tell ("WHISPER"),
	Chat_Party ("PARTY"),
	Chat_Clan ("CLAN"),
	Chat_System ("EMOTE"),
	Chat_User_Pet ("USERPET"),
	Chat_GM_Pet ("GMPET"),
	Chat_Market ("TRADE"),
	Chat_Alliance ("ALLIANCE"),
	Chat_Announce ("ANNOUNCE"),
	Chat_Custom ("CRASH"),
	Chat_L2Friend ("L2FRIEND"),
	Chat_MSN ("MSN"),
	Chat_Party_Room ("PARTYROOM"),
	Chat_Commander ("COMMANDER"),
	Chat_Inner_Partymaster ("INNERPARTYMASTER"),
	Chat_Hero ("HERO"),
	Chat_Critical_Announce ("CRITANNOUNCE"),
	Chat_None ("NONE");
	
	
	private String _channelName;
	
	private SystemChatChannelId(String channelName)
	{
		_channelName = channelName;
	}
	
	public int getId()
	{
		return this.ordinal();
	}
	
	public String getName()
	{
		return _channelName;
	}
	
	public static SystemChatChannelId getChatType(int channelId)
	{
		for (SystemChatChannelId channel : SystemChatChannelId.values())
	        if (channel.getId() == channelId)
				return channel;

		return SystemChatChannelId.Chat_None;
	}
}

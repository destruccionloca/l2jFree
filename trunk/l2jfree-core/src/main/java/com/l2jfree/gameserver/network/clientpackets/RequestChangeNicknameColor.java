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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Sent when a player confirms Change Name Color dialog.
 * 
 * @author savormix
 */
public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final String _C__D0_4F_REQUESTCHANGENICKNAMECOLOR = "[C] D0:4F RequestChangeNicknameColor";
	
	public static final int COLOR_NAME_1 = 13021;
	public static final int COLOR_NAME_2 = 13307;
	
	// colors harvested from client, do not modify
	private static final int COLOR_CHOICES[] = { 0x9393FF, // Pink
			0x7C49FC, // Rose Pink
			0x97F8FC, // Lemon Yellow
			0xFA9AEE, // Lilac
			0xFF5D93, // Cobalt Violet
			0x00FCA0, // Mint Green
			0xA0A601, // Peacock Green
			0x7898AF, // Yellow Ochre
			0x486295, // Chocolate
			0x999999 // Silver
	};
	
	// Name Color
	private int _color;
	private String _title;
	private int _itemObjectId;
	
	// private int _unk;
	
	@Override
	protected void readImpl()
	{
		_color = readD();
		_title = readS();
		_itemObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
			return;
		
		final L2ItemInstance item = player.getInventory().getItemByObjectId(_itemObjectId);
		if (item == null)
			return;
		
		// TODO 1.4.0
		//if (item.getEtcItem() == null || !item.getEtcItem().getHandlerName().equalsIgnoreCase("NicknameColor"))
		//	return;
		
		// TODO 1.4.0
		if (item.getItemId() != COLOR_NAME_1 && item.getItemId() != COLOR_NAME_2)
			return;
		
		if (!player.destroyItem("ChangeNickColor", item, 1, player, true))
		{
			sendAF();
			return;
		}
		
		//if (!player.destroyItemByItemId("ChangeNickColor", COLOR_NAME_1, 1, player, true)
		//		&& !player.destroyItemByItemId("ChangeNickColor", COLOR_NAME_2, 1, player, true))
		//{
		//	sendAF();
		//	return;
		//}
		
		if (Config.TITLE_PATTERN.matcher(_title).matches())
			player.setTitle(_title);
		else
			player.setTitle(""); // as in description
		sendPacket(SystemMessageId.TITLE_CHANGED);
		
		if (0 <= _color && _color < COLOR_CHOICES.length)
			player.getAppearance().setNickColor(COLOR_CHOICES[_color]);
		else
			player.getAppearance().setNickColor(-1);
		player.broadcastUserInfo();
		
		sendAF();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_4F_REQUESTCHANGENICKNAMECOLOR;
	}
}

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
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.GMHennaInfo;
import com.l2jfree.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jfree.gameserver.network.serverpackets.GMViewItemList;
import com.l2jfree.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jfree.gameserver.network.serverpackets.GMViewQuestInfo;
import com.l2jfree.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jfree.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

/**
 * This class represents a packet that is sent whenever a GM clicks something
 * player-related in the ALT+G menu.
 * 
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestGMCommand extends L2GameClientPacket
{
	private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";

	private String _targetName;
	private int _command;
	//private final int _unknown;

	/**
	 * packet type id 0x00
	 * format:	cd
	 */
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command	= readD();
		//_unknown  = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		if (activeChar.getAccessLevel() < Config.GM_ALTG_MIN_LEVEL)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		if (player == null)
		{
			requestFailed(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		switch (_command)
		{
			case 1: // player status
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_VIEW_INFO)
				{
					sendPacket(new GMViewCharacterInfo(player));
					sendPacket(new GMHennaInfo(player));
				}
				break;
			}
			case 2: // player clan
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_CLAN_VIEW && player.getClan() != null)
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				break;
			}
			case 3: // player skills
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_VIEW_SKILL)
					sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_VIEW_QUEST)
					sendPacket(new GMViewQuestInfo(player));
				break;
			}
			case 5: // player inventory
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_INVENTORY)
				{
					sendPacket(new GMViewItemList(player));
					sendPacket(new GMHennaInfo(player));
				}
				break;
			}
			case 6: // player warehouse
			{
				if (activeChar.getAccessLevel() >= Config.GM_CHAR_VIEW_WAREHOUSE)
					sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__6E_REQUESTGMCOMMAND;
	}
}

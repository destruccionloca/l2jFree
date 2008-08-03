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
package com.l2jfree.gameserver.handler.admincommandhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class AdminSendHome implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_sendhome" };
	private static final int		REQUIRED_LEVEL	= Config.GM_TELEPORT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			return false;

		if (command.startsWith("admin_sendhome"))
		{
			if (command.split(" ").length > 1)
				handleSendhome(activeChar, command.split(" ")[1]);
			else
				handleSendhome(activeChar);
		}

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}

	private void handleSendhome(L2PcInstance activeChar)
	{
		handleSendhome(activeChar, null);
	}

	private void handleSendhome(L2PcInstance activeChar, String player)
	{
		L2Object obj = activeChar.getTarget();

		if (player != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(player);

			if (plyr != null)
			{
				obj = plyr;
			}
		}

		if (obj == null)
			obj = activeChar;

		if (obj instanceof L2Character)
			doSendhome((L2Character) obj);
		else
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	private void doSendhome(L2Character targetChar)
	{
		targetChar.teleToLocation(TeleportWhereType.Town);
	}
}

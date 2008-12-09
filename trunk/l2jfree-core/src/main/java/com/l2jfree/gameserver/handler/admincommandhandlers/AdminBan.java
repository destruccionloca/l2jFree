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

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;


/**
 * This class handles following admin commands:
 * - ban account_name = changes account access level to -100 and logs him off. If no account is specified, target's account is used.
 * - unban account_name = changes account access level to 0.
  * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	= { "admin_ban", "admin_unban" };
	private static final int		REQUIRED_LEVEL	= Config.GM_BAN;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel())))
				return false;
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;
		if (command.startsWith("admin_ban"))
		{
			try
			{
				player = st.nextToken();
				plyr = L2World.getInstance().getPlayer(player);
			}
			catch (Exception e)
			{
				L2Object target = activeChar.getTarget();
				if (target != null && target instanceof L2PcInstance)
					plyr = (L2PcInstance) target;
				else
					activeChar.sendMessage("Usage: //ban [account_name] (if none, target char's account gets banned)");
			}
			if (plyr != null && plyr == activeChar)
			{
				plyr.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			}
			else if (plyr == null)
			{
				account_name = player;
				LoginServerThread.getInstance().sendAccessLevel(account_name, -100);
				activeChar.sendMessage("Ban request sent for account " + account_name + ". If you need a playername based commmand, see //ban_menu");
			}
			else
			{
				plyr.setAccountAccesslevel(-100);
				account_name = plyr.getAccountName();
				try
				{
					L2GameClient client = plyr.getClient();
					L2GameClient.saveCharToDisk(plyr, true); // Store character
					plyr.deleteMe();
					// prevent deleteMe from being called a second time on disconnection
					client.setActiveChar(null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				activeChar.sendMessage("Account " + account_name + " banned.");
			}
		}
		else if (command.startsWith("admin_unban"))
		{
			try
			{
				account_name = st.nextToken();
				LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
				activeChar.sendMessage("Unban request sent for account " + account_name + ". If you need a playername based commmand, see //unban_menu");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //unban <account_name>");
			}
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
}

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

import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands:
 * - ban account_name = changes account access level to selected [u]negative[/u] access level and logs him off. If no account is specified, target's account is used.
 * - unban account_name = changes account access level to 0.
  * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	= { "admin_ban", "admin_unban", "admin_ban_select" };

	private static final String HTML_ROOT = "data/html/admin/";

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;

		if (command.startsWith(ADMIN_COMMANDS[0]))
		{
			if (command.startsWith(ADMIN_COMMANDS[2]))
			{
				String accountOrOnlineChar;
				if (!st.hasMoreTokens() ||
						(accountOrOnlineChar = st.nextToken().trim()).length() == 0) {
					activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
					return false;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(HTML_ROOT + "BanMenu.html");
				html.replace("%account%", accountOrOnlineChar);
				activeChar.sendPacket(html); html = null;
				return true;
			}
			int aLvl = -100;
			try
			{
				player = st.nextToken();
				if (st.hasMoreTokens()) {
					aLvl = Integer.parseInt(st.nextToken());
					//prevent abuse
					if (aLvl > 0)
						aLvl *= -1;
					else if (aLvl == 0)
						aLvl = -100;
				}
				plyr = L2World.getInstance().getPlayer(player);
			}
			catch(NumberFormatException nfe) { activeChar.sendMessage("Wrong accesslevel specified!"); }
			catch (Exception e)
			{
				L2Object target = activeChar.getTarget();
				if (target != null && target instanceof L2PcInstance)
					plyr = (L2PcInstance) target;
				else
					activeChar.sendMessage("Usage: //ban [account_name] (if none, target char's account gets banned) [accesslevel]");
			}
			if (plyr != null && plyr == activeChar)
			{
				plyr.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			}
			else if (plyr == null)
			{
				account_name = player;
				LoginServerThread.getInstance().sendAccessLevel(account_name, aLvl);
				activeChar.sendMessage("Ban request sent for account " + account_name + ". If you need a playername based commmand, see //ban_menu");
			}
			else
			{
				plyr.setAccountAccesslevel(aLvl);
				account_name = plyr.getAccountName();
				try
				{
					new Disconnection(plyr).defaultSequence(false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				activeChar.sendMessage("Account " + account_name + " banned.");
			}
		}
		else if (command.startsWith(ADMIN_COMMANDS[1]))
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
}

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
package com.l2jfree.gameserver.handler.irccommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jfree.gameserver.handler.IIrcCommandHandler;
import com.l2jfree.gameserver.instancemanager.IrcManager;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.serverpackets.LeaveWorld;

public class IrcKick implements IIrcCommandHandler
{
	private static final String[]	IRC_COMMANDS	= { "!kick", "!kick_nongm", "!ban" };

	public boolean useIrcCommand(String command, String gm, String target, boolean authed)
	{
		if (!authed)
			return false;

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";

		if (command.startsWith("!kick_nongm"))
		{
			int counter = 0;
			for (L2PcInstance plyr : L2World.getInstance().getAllPlayers())
			{
				if (!plyr.isGM())
				{
					counter++;
					plyr.sendPacket(LeaveWorld.STATIC_PACKET);
					new Disconnection(plyr).defaultSequence(false);
				}
			}
			IrcManager.getInstance().getConnection().send(gm, "Kicked " + counter + " players");
		}
		else if (command.startsWith("!kick"))
		{
			try
			{
				player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					new Disconnection(plyr).defaultSequence(false);
					IrcManager.getInstance().getConnection().send(gm, "You kicked " + plyr.getName() + " from the game.");
				}
			}
			catch (NoSuchElementException nsee)
			{
				IrcManager.getInstance().getConnection().send(gm, "Specify a character name.");
			}
			catch (NumberFormatException nfe)
			{
				IrcManager.getInstance().getConnection().send(gm, "Usage: !kick <charname>");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("!ban"))
		{
			try
			{
				L2PcInstance plyr = null;
				try
				{
					player = st.nextToken();
					plyr = L2World.getInstance().getPlayer(player);
				}
				catch (Exception e)
				{
					IrcManager.getInstance().getConnection().send(gm, "Usage: !ban <Charname>");
				}
				if (plyr != null)
				{
					plyr.setAccountAccesslevel(-100);
					String account_name = plyr.getAccountName();
					new Disconnection(plyr).defaultSequence(false);
					IrcManager.getInstance().getConnection().send(gm, "Account " + account_name + " banned.");
				}
				else
					IrcManager.getInstance().getConnection().send(gm, "Char " + player + " not Online!");
			}
			catch (NoSuchElementException nsee)
			{
				IrcManager.getInstance().getConnection().send(gm, "Specify a character name.");
			}
			catch (NumberFormatException nfe)
			{
				IrcManager.getInstance().getConnection().send(gm, "Usage: !ban <charname>");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return true;
	}

	public String[] getIrcCommandList()
	{
		return IRC_COMMANDS;
	}
}
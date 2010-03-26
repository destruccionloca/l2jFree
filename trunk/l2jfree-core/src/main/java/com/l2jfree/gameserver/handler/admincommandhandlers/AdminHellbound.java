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
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.instancemanager.HellboundManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Psycho(killer1888) / L2jFree
 */


public class AdminHellbound implements IAdminCommandHandler
{
	private static int _points = 0;

	private static final String[]	ADMIN_COMMANDS	=
	{
		"admin_add_trust_points",
		"admin_remove_trust_points"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}

		if (cmd.equals("admin_add_trust_points"))
		{
			
			HellboundManager.getInstance().addTrustPoints(Integer.valueOf(val));
			activeChar.sendMessage("Added "+val+" trust points to Hellbound.");
		}
		else if (cmd.equals("admin_remove_trust_points"))
		{
			
			HellboundManager.getInstance().decreaseTrustPoints(Integer.valueOf(val));
			activeChar.sendMessage("Removed "+val+" trust points to Hellbound.");
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}

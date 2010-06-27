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

import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.instancemanager.SeedOfDestructionManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Psycho(killer1888) / L2jFree
 */
public final class AdminSeedOfDestruction implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_set_sod_state",
		"admin_set_tiat_kills" };
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		String val = st.nextToken();

		if (cmd.equals("admin_set_sod_state"))
		{
			switch (Integer.valueOf(val))
			{
			case 1:
				activeChar.sendMessage("Not implented yet");
				break;
			case 2:
				SeedOfDestructionManager.getInstance().runDefenseMode();
				break;
			default:
				SeedOfDestructionManager.getInstance().startHuntingGround();
			}
		}
		else if (cmd.equals("admin_set_tiat_kills"))
		{
			SeedOfDestructionManager.getInstance().setTiatKilled(Integer.valueOf(val));
			activeChar.sendMessage("Tiat killed " + val + " times.");
		}
		
		return true;
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}

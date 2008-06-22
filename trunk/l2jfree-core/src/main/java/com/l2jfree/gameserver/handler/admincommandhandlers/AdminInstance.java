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
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.entity.Instance;

/** 
 * @author evill33t
 * 
 */
public class AdminInstance implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_setinstance",
			"admin_ghoston",
			"admin_ghostoff",
			"admin_createinstance",
			"admin_destroyinstance",
			"admin_listinstances"					};
	private static final int		REQUIRED_LEVEL	= Config.GM_INSTANCE;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel())))
				return false;

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		// create new instance
		if (command.startsWith("admin_createinstance"))
		{
			String[] parts = command.split(" ");
			if (parts.length < 2)
			{
				activeChar.sendMessage("Example: //createinstance <id> <templatefile> ids )> 300000 are reserved for dynamic instances");
			}
			else
			{
				try
				{
					int id = Integer.parseInt(parts[1]);
					if (InstanceManager.getInstance().createInstanceFromTemplate(id, parts[2]) && id < 300000)
					{
						activeChar.sendMessage("Instance created");
						return true;
					}
					else
					{
						activeChar.sendMessage("Failed to create instance");
						return true;
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Failed loading: " + parts[2]);
					return false;
				}
			}
		}
		else if (command.startsWith("admin_listinstances"))
		{
			for (Instance temp : InstanceManager.getInstance().getInstances())
			{
				activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
			}
		}
		else if (command.startsWith("admin_setinstance"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				if (InstanceManager.getInstance().getInstance(val) == null)
				{
					activeChar.sendMessage("Instance " + val + " doesnt exist.");
					;
					return false;
				}
				else
				{
					activeChar.getTarget().setInstanceId(val);
					L2PcInstance player = ((L2PcInstance) activeChar.getTarget());
					if (activeChar.getTarget() instanceof L2PlayableInstance)
					{
						player.sendMessage("Admin setted your instance to:" + val);
						InstanceManager.getInstance().getInstance(val).addPlayer(player.getName());
						player.teleToLocation(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
						return true;
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //setinstance id");
			}
		}
		else if (command.startsWith("admin_destroyinstance"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				InstanceManager.getInstance().destroyInstance(val);
				activeChar.sendMessage("Instance destroyed");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //destroyinstance id");
			}
		}

		// set ghost mode on aka not appearing on any knownlist
		else if (command.startsWith("admin_ghoston"))
		{
			activeChar.getAppearance().setGhostMode(true);
			activeChar.sendMessage("Ghost mode enabled");
		}
		// ghost mode off
		else if (command.startsWith("admin_ghostoff"))
		{
			activeChar.getAppearance().setGhostMode(true);
			activeChar.sendMessage("Ghost mode disabled");
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

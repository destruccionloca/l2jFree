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
package com.l2jfree.status.commands;

import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.TeleportLocationTable;
import com.l2jfree.gameserver.datatables.TradeListTable;
import com.l2jfree.gameserver.instancemanager.Manager;
import com.l2jfree.gameserver.instancemanager.ZoneManager;
import com.l2jfree.gameserver.model.L2Multisell;
import com.l2jfree.status.GameStatusCommand;

public final class Reload extends GameStatusCommand
{
	public Reload()
	{
		super("", "reload");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "...";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			String type = params;
			
			if (type.equals("multisell"))
			{
				print("Reloading multisell... ");
				L2Multisell.getInstance().reload();
				println("done");
			}
			else if (type.equals("teleport"))
			{
				print("Reloading teleports... ");
				TeleportLocationTable.getInstance().reloadAll();
				println("done");
			}
			else if (type.equals("skill"))
			{
				print("Reloading skills... ");
				SkillTable.reload();
				println("done");
			}
			else if (type.equals("npc"))
			{
				print("Reloading npc templates... ");
				NpcTable.getInstance().cleanUp();
				NpcTable.getInstance().reloadAll();
				println("done");
			}
			else if (type.equals("htm"))
			{
				print("Reloading html cache... ");
				HtmCache.getInstance().reload(true);
				println("done");
			}
			else if (type.equals("item"))
			{
				print("Reloading item templates... ");
				ItemTable.reload();
				println("done");
			}
			else if (type.equals("instancemanager"))
			{
				print("Reloading instance managers... ");
				Manager.reloadAll();
				println("done");
			}
			else if (type.equals("zone"))
			{
				print("Reloading zone tables... ");
				ZoneManager.getInstance().reload();
				println("done");
			}
			else if (type.equals("tradelist"))
			{
				print("Reloading trade lists...");
				TradeListTable.getInstance().reloadAll();
				println("done");
			}
			else if (type.startsWith("door"))
			{
				print("Reloading Doors...");
				DoorTable.getInstance().reloadAll();
				println("done");
			}
			else
			{
				
				println("Usage: reload <multisell|teleport|skill|npc|htm|item|instancemanager|tradelist|zone|door>");
			}
		}
		catch (Exception e)
		{
			
			println("Usage: reload <multisell|teleport|skill|npc|htm|item|instancemanager|tradelist|zone|door>");
		}
	}
}

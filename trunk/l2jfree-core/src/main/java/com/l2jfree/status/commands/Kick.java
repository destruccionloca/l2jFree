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

import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.status.GameStatusCommand;

public final class Kick extends GameStatusCommand
{
	public Kick()
	{
		super("kick player <name> from server", "kick");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "name";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			L2PcInstance player = L2World.getInstance().getPlayer(params);
			if (player != null)
			{
				new Disconnection(player).defaultSequence(false);
				println("Player kicked");
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			println("Please enter player name to kick");
		}
	}
	
}

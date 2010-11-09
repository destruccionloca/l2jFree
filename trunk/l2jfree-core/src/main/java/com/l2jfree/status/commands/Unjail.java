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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.status.GameStatusCommand;

public final class Unjail extends GameStatusCommand
{
	public Unjail()
	{
		super("unjails given player", "unjail");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		String name = params;
		L2PcInstance playerObj = L2World.getInstance().getPlayer(name);
		
		if (playerObj != null)
		{
			playerObj.stopJailTask(false);
			playerObj.setInJail(false, 0);
			println("Character " + name + " removed from jail");
		}
		else
			unjailOfflinePlayer(name);
	}
	
	private void unjailOfflinePlayer(String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			
			int count = statement.executeUpdate();
			statement.close();
			
			if (count == 0)
				println("Character not found!");
			else
				println("Character " + name + " set free.");
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}

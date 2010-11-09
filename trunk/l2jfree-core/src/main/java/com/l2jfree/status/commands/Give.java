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
import java.util.StringTokenizer;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.status.GameStatusCommand;

public final class Give extends GameStatusCommand
{
	public Give()
	{
		super("gives item to player", "give");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player itemid amount";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		StringTokenizer st = new StringTokenizer(params);
		
		String playername = st.nextToken();
		
		L2PcInstance player = L2World.getInstance().getPlayer(playername);
		int itemId = Integer.parseInt(st.nextToken());
		int amount = Integer.parseInt(st.nextToken());
		
		if (player != null)
		{
			L2ItemInstance item = player.getInventory().addItem("Status-Give", itemId, amount, null, null);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendPacket(iu);
			player.sendItemPickedUpMessage(item);
			println("ok - was online");
		}
		else
		{
			Integer playerId = CharNameTable.getInstance().getObjectIdByName(playername);
			if (playerId != null)
			{
				addItemToInventory(playerId, IdFactory.getInstance().getNextId(), itemId, amount, 0);
				println("ok - was offline");
			}
			else
			{
				println("player not found");
			}
		}
	}
	
	private void addItemToInventory(int charId, int objectId, int currency, long count, int enchantLevel)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			PreparedStatement statement = con
					.prepareStatement("INSERT INTO items (owner_id, object_id, item_id, count, enchant_level, loc, loc_data) VALUES (?,?,?,?,?,?,?)");
			statement.setInt(1, charId);
			statement.setInt(2, objectId);
			statement.setInt(3, currency);
			statement.setLong(4, count);
			statement.setInt(5, enchantLevel);
			statement.setString(6, "INVENTORY");
			statement.setInt(7, 0);
			statement.execute();
			statement.close();
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

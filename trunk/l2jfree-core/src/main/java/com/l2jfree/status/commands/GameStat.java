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
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.TradeList.TradeItem;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.status.GameStatusCommand;

public final class GameStat extends GameStatusCommand
{
	public GameStat()
	{
		super("TODO", "gamestat");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			String type = params;
			
			// name;type;x;y;itemId:enchant:price...
			if (type.equals("privatestore"))
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player.getPrivateStoreType() == 0)
						continue;
					
					TradeList list = null;
					String content = "";
					
					if (player.getPrivateStoreType() == 1) // sell
					{
						list = player.getSellList();
						for (TradeItem item : list.getItems())
						{
							content += item.getItem().getItemId() + ":" + item.getEnchant() + ":" + item.getPrice()
									+ ":";
						}
						content = player.getName() + ";" + "sell;" + player.getX() + ";" + player.getY() + ";"
								+ content;
						println(content);
						continue;
					}
					else if (player.getPrivateStoreType() == 3) // buy
					{
						list = player.getBuyList();
						for (TradeItem item : list.getItems())
						{
							content += item.getItem().getItemId() + ":" + item.getEnchant() + ":" + item.getPrice()
									+ ":";
						}
						content = player.getName() + ";" + "buy;" + player.getX() + ";" + player.getY() + ";" + content;
						println(content);
						continue;
					}
					
				}
			}
		}
		catch (Exception e)
		{
		}
	}
}

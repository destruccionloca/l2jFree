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
package com.l2jfree.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.L2TradeList.L2TradeItem;
import com.l2jfree.gameserver.taskmanager.SQLQueue;

/**
 * This class manages buylists from database
 */
public class TradeListTable
{
	private static final Log _log = LogFactory.getLog(TradeListTable.class);
	
	private static final class SingletonHolder
	{
		public static final TradeListTable INSTANCE = new TradeListTable();
	}
	
	public static TradeListTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final Map<Integer, L2TradeList> _lists = new FastMap<Integer, L2TradeList>();
	
	private TradeListTable()
	{
		load();
	}
	
	private void load(boolean custom)
	{
		final String tablePrefix = custom ? "custom_" : "";
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			final PreparedStatement statement1 = con.prepareStatement("SELECT * FROM " + tablePrefix
					+ "merchant_shopids");
			final ResultSet rset1 = statement1.executeQuery();
			
			int count = 0;
			while (rset1.next())
			{
				final L2TradeList buylist = new L2TradeList(rset1, custom);
				
				final PreparedStatement statement = con.prepareStatement("SELECT * FROM " + tablePrefix
						+ "merchant_buylists WHERE shop_id=? ORDER BY `order` ASC");
				statement.setInt(1, buylist.getListId());
				final ResultSet rset = statement.executeQuery();
				
				try
				{
					while (rset.next())
					{
						final L2TradeItem buyItem = buylist.new L2TradeItem(rset);
						
						buylist.addItem(buyItem);
					}
				}
				catch (Exception e)
				{
					_log.warn("TradeListTable: Problem with " + buylist + ".", e);
				}
				
				if (!buylist.getItems().isEmpty())
				{
					_lists.put(buylist.getListId(), buylist);
					count++;
				}
				else
					_log.warn("TradeListTable: Empty " + buylist + ".");
				
				rset.close();
				statement.close();
			}
			
			rset1.close();
			statement1.close();
			
			_log.info("TradeListTable: Loaded " + count + (custom ? " custom" : "") + " buylists.");
		}
		catch (Exception e)
		{
			_log.warn("TradeListTable:" + (custom ? " custom" : "") + " buylists could not be initialized.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void load()
	{
		load(false); // not custom
		load(true); // custom
	}
	
	public void reloadAll()
	{
		_lists.clear();
		
		load();
	}
	
	public L2TradeList getBuyList(int listId)
	{
		return _lists.get(listId);
	}
	
	public L2TradeList getBuyListByNpcId(int listId, int npcId)
	{
		final L2TradeList list = getBuyList(listId);
		
		if (list.isGm())
			return null;
		
		if (npcId != list.getNpcId())
			return null;
		
		return list;
	}
	
	public void dataCountStore()
	{
		try
		{
			for (L2TradeList list : _lists.values())
				for (L2TradeItem item : list.getItems())
					item.updateDatabase();
		}
		catch (RuntimeException e)
		{
			_log.fatal("TradeController: Could not store Count Item", e);
		}
		
		SQLQueue.getInstance().run();
	}
}

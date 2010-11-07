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
package com.l2jfree.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket.ElementalOwner;
import com.l2jfree.gameserver.taskmanager.SQLQueue;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.sql.SQLQuery;

public final class L2TradeList
{
	private static final Log _log = LogFactory.getLog(L2TradeList.class);
	
	private final Map<Integer, L2TradeItem> _items = new FastMap<Integer, L2TradeItem>().setShared(true);
	
	private final int _listId;
	private final boolean _gm;
	private final int _npcId;
	private final boolean _custom;
	
	public L2TradeList(ResultSet rset, boolean custom) throws SQLException
	{
		_listId = rset.getInt("shop_id");
		
		final String npcId = rset.getString("npc_id");
		
		if (npcId.equalsIgnoreCase("gm"))
		{
			_gm = true;
			_npcId = 0;
		}
		else
		{
			_gm = false;
			_npcId = Integer.parseInt(npcId);
			
			if (NpcTable.getInstance().getTemplate(getNpcId()) == null)
				_log.warn("L2TradeList: Merchant id " + getNpcId() + " for " + this + " does not exist.");
		}
		
		_custom = custom;
	}
	
	public boolean isCustom()
	{
		return _custom;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public boolean isGm()
	{
		return _gm;
	}
	
	public void addItem(L2TradeItem item)
	{
		_items.put(item.getItemId(), item);
	}
	
	public void replaceItem(int itemID, long price)
	{
		final L2TradeItem item = _items.get(itemID);
		if (item == null)
			return;
		
		item.setPrice(price);
	}
	
	public void removeItem(int itemID)
	{
		_items.remove(itemID);
	}
	
	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return _listId;
	}
	
	/**
	 * @return Returns the items.
	 */
	public Collection<L2TradeItem> getItems()
	{
		return _items.values();
	}
	
	public Collection<L2TradeItem> getItems(int start, int end)
	{
		return new ArrayList<L2TradeItem>(getItems()).subList(start, end);
	}
	
	public long getPriceForItemId(int itemId)
	{
		final L2TradeItem item = _items.get(itemId);
		if (item == null)
			return -1;
		
		return item.getPrice();
	}
	
	public L2TradeItem getItemById(int itemId)
	{
		return _items.get(itemId);
	}
	
	public boolean containsItemId(int itemId)
	{
		return _items.containsKey(itemId);
	}
	
	@Override
	public String toString()
	{
		return "[L2TradeList - ID: " + getListId() + (isCustom() ? "(custom)" : "") + "]";
	}
	
	public final class L2TradeItem implements ElementalOwner
	{
		private final int _itemId;
		
		/** Price of the item (SQL: -1 for reference price, 0 for gm shop, positive for usual) */
		private long _price;
		
		/** (SQL: -1 for full/unlimited stock, non-negative for remaining limited) */
		private long _count;
		
		/** Initial Quantity of the item (SQL: -1 for unlimited stock, positive for limited) */
		private final long _initCount;
		
		/** (SQL: 0 for unlimited stock, positive for limited in hours) */
		private final int _restoreDelay;
		
		/** (SQL: 0 for unlimited stock, positive for limited in systime) */
		private long _nextRestoreTime;
		
		private boolean _databaseUpdateRequired = false;
		
		public L2TradeItem(int itemId, long price, int currentCount)
		{
			_itemId = itemId;
			_initCount = -1;
			_restoreDelay = 0;
			_nextRestoreTime = 0;
			
			setPrice(price);
			setCount(currentCount);
			
			if (getItem() == null)
				_log.warn("Non-existing itemId: " + getItemId() + " in " + L2TradeList.this + ".");
			
			_databaseUpdateRequired = false;
			restoreInitCount();
		}
		
		public L2TradeItem(ResultSet rset) throws SQLException
		{
			_itemId = rset.getInt("item_id");
			_initCount = rset.getInt("count");
			_restoreDelay = rset.getInt("time") * 60 * 60 * 1000;
			_nextRestoreTime = rset.getLong("saveTimer");
			
			setPrice(rset.getLong("price"));
			setCount(rset.getInt("currentCount"));
			
			if (getItem() == null)
				_log.warn("Non-existing itemId: " + getItemId() + " in " + L2TradeList.this + ".");
			
			_databaseUpdateRequired = false;
			restoreInitCount();
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public L2Item getItem()
		{
			return ItemTable.getInstance().getTemplate(getItemId());
		}
		
		public boolean isEquipable()
		{
			return getItem().isEquipable();
		}
		
		public int getItemDisplayId()
		{
			return getItem().getItemDisplayId();
		}
		
		public long getPrice()
		{
			return (long)(getItem().getPriceMulti() * _price);
		}
		
		public void setPrice(long price)
		{
			if (price <= -1)
				_price = (long)(getItem().getReferencePrice() / getItem().getPriceMulti());
			else
				_price = price;
			
			if (!isGm() && getItem().getReferencePrice() > getPrice())
				_log.warn("L2TradeList: The price of item " + getItemId() + " in " + L2TradeList.this
						+ " is lower than reference price!");
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public void setCount(long count)
		{
			final long oldCount = getCount();
			
			if (count > -1)
				_count = count;
			else
				_count = getInitCount();
			
			if (oldCount != getCount())
				_databaseUpdateRequired = true;
		}
		
		public boolean decreaseCount(long val)
		{
			if (!hasLimitedStock())
				return true;
			
			final long newCount = getCount() - val;
			if (newCount < 0)
				return false;
			
			setCount(newCount);
			return true;
		}
		
		public long getInitCount()
		{
			return _initCount;
		}
		
		public boolean hasLimitedStock()
		{
			return getInitCount() > -1 && _restoreDelay > 0;
		}
		
		public void restoreInitCount()
		{
			if (!hasLimitedStock())
				return;
			
			if (_nextRestoreTime <= System.currentTimeMillis())
			{
				while (_nextRestoreTime <= System.currentTimeMillis())
					_nextRestoreTime += _restoreDelay;
				
				_databaseUpdateRequired = true;
				
				setCount(getInitCount());
				
				updateDatabase();
			}
			
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run()
				{
					restoreInitCount();
				}
			}, _nextRestoreTime - System.currentTimeMillis());
		}
		
		public void updateDatabase()
		{
			if (!hasLimitedStock() || !_databaseUpdateRequired)
				return;
			
			SQLQueue.getInstance().add(new SQLQuery() {
				@Override
				public void execute(Connection con)
				{
					if (!hasLimitedStock() || !_databaseUpdateRequired)
						return;
					
					try
					{
						PreparedStatement statement = con.prepareStatement("UPDATE "
								+ (isCustom() ? "custom_merchant_buylists" : "merchant_buylists")
								+ " SET savetimer=?, currentCount=? WHERE shop_id=? and item_id=?");
						statement.setLong(1, _nextRestoreTime);
						statement.setLong(2, getCount());
						statement.setInt(3, getListId());
						statement.setInt(4, getItemId());
						statement.execute();
						statement.close();
						
						_databaseUpdateRequired = false;
					}
					catch (Exception e)
					{
						_log.warn("L2TradeItem: Could not update Timer save in Buylist", e);
					}
				}
			});
		}
		
		@Override
		public int getAttackElementPower()
		{
			return 0;
		}
		
		@Override
		public byte getAttackElementType()
		{
			return 0;
		}
		
		@Override
		public int getElementDefAttr(byte element)
		{
			return 0;
		}
	}
}

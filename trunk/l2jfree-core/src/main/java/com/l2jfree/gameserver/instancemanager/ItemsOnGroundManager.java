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
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ItemsAutoDestroy;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;

/**
 * This class manage all items on ground
 * 
 * @version $Revision: $ $Date: $
 * @author  DiezelMax - original ideea
 * @author  Enforcer  - actual build
 */
public class ItemsOnGroundManager
{
	protected static Log				_log	= LogFactory.getLog(ItemsOnGroundManager.class.getName());

	private static ItemsOnGroundManager	_instance;
	protected FastList<L2ItemInstance>	_items	= null;

	private ItemsOnGroundManager()
	{
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		_items = new FastList<L2ItemInstance>();
		load();
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StoreInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
	}

	public static final ItemsOnGroundManager getInstance()
	{
		if (_instance == null)
			_instance = new ItemsOnGroundManager();
		
		return _instance;
	}

	private void load()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
			emptyTable();

		if (!Config.SAVE_DROPPED_ITEM)
			return;

		// if DestroyPlayerDroppedItem was previously  false, items curently protected will be added to ItemsAutoDestroy
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			Connection con = null;
			try
			{
				String str = null;
				if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM) // Recycle misc. items only
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1 AND equipable=0";
				else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM) // Recycle all items including equipable
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1";
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("error while updating table ItemsOnGround " + e, e);
			}
            finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
		}

		// Add items to world
		Connection con = null;
		try
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				Statement s = con.createStatement();
				ResultSet result;
				int count = 0;
				result = s.executeQuery("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground");
				while (result.next())
				{
					L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
					L2World.getInstance().storeObject(item);
					if (item.isStackable() && result.getInt(3) > 1) // This check and..
						item.setCount(result.getInt(3));
					if (result.getInt(4) > 0) // This, are really necessary?
						item.setEnchantLevel(result.getInt(4));
					item.getPosition().setXYZ(result.getInt(5), result.getInt(6), result.getInt(7));
					item.getPosition().getWorldRegion().addVisibleObject(item);
					item.setDropTime(result.getLong(8));
					if (result.getLong(8) == -1)
						item.setProtected(true);
					else
						item.setProtected(false);
					L2World.getInstance().addVisibleObject(item, null);
					_items.add(item);
					count++;
					// Add to ItemsAutoDestroy only items not protected
					if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
					{
						if (result.getLong(8) > -1)
						{
							if ((Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB)
									|| (Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB))
								ItemsAutoDestroy.getInstance().addItem(item);
						}
					}
				}
				result.close();
				s.close();
				if (count > 0)
					_log.info("ItemsOnGroundManager: restored " + count + " items.");
				else
					_log.info("Initializing ItemsOnGroundManager.");
			}
			catch (Exception e)
			{
				_log.fatal("error while loading ItemsOnGround " + e, e);
			}
		}
        finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

        if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
			emptyTable();
	}

	public void save(L2ItemInstance item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		_items.add(item);
	}

	public void removeObject(L2Object item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		_items.remove(item);
	}

	public void saveInDb()
	{
		new StoreInDb().run();
	}

	public void cleanUp()
	{
		_items.clear();
	}

	public void emptyTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement del = con.prepareStatement("DELETE from itemsonground");
			del.execute();
			del.close();
		}
		catch (Exception e1)
		{
			_log.fatal("error while cleaning table ItemsOnGround " + e1, e1);
		}
        finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
	}

	protected class StoreInDb extends Thread
	{
		@Override
		public void run()
		{
			if (!Config.SAVE_DROPPED_ITEM)
				return;

			emptyTable();

			if (_items.isEmpty() && _log.isDebugEnabled())
			{
				_log.warn("ItemsOnGroundManager: nothing to save...");
				return;
			}

			for (L2ItemInstance item : _items)
			{
				if (item == null)
					continue;

				if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
					continue; // Cursed Items not saved to ground, prevent double save

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(con);
					PreparedStatement statement = con
							.prepareStatement("INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());

					if (item.isProtected())
						statement.setLong(8, -1); // Item will be protected
					else
						statement.setLong(8, item.getDropTime()); // Item will be added to ItemsAutoDestroy
					if (item.isEquipable())
						statement.setLong(9, 1); // Set equipable
					else
						statement.setLong(9, 0);
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.fatal("error while inserting into table ItemsOnGround " + e, e);
				}
	            finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
			}
			if (_log.isDebugEnabled())
				_log.warn("ItemsOnGroundManager: " + _items.size() + " items on ground saved");
		}
	}
}

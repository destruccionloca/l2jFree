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
package com.l2jfree.gameserver;

import com.l2jfree.Config;
import com.l2jfree.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;

public final class ItemsAutoDestroy extends AbstractIterativePeriodicTaskManager<L2ItemInstance>
{
	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private ItemsAutoDestroy()
	{
		super(5000);
	}
	
	@Override
	public void startTask(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		
		super.startTask(item);
	}
	
	@Override
	protected String getCalledMethodName()
	{
		return "removeItem()";
	}
	
	@Override
	protected void callTask(L2ItemInstance item)
	{
		if (item == null || item.getDropTime() == 0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
		{
			stopTask(item);
		}
		else if (System.currentTimeMillis() - item.getDropTime() > getDestroyTime(item))
		{
			L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
			L2World.getInstance().removeObject(item);
			stopTask(item);
			if (Config.SAVE_DROPPED_ITEM)
				ItemsOnGroundManager.getInstance().removeObject(item);
		}
	}
	
	private static int getDestroyTime(L2ItemInstance item)
	{
		return item.getItemType() == L2EtcItemType.HERB ? Config.HERB_AUTO_DESTROY_TIME : Config.AUTODESTROY_ITEM_AFTER;
	}
	
	public static void tryAddItem(L2ItemInstance item)
	{
		if (Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
			return;
		
		if (getDestroyTime(item) > 0)
		{
			if (item.isEquipable())
			{
				if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
					ItemsAutoDestroy.getInstance().startTask(item);
			}
			else
			{
				ItemsAutoDestroy.getInstance().startTask(item);
			}
		}
	}
	
	private static final class SingletonHolder
	{
		public static final ItemsAutoDestroy INSTANCE = new ItemsAutoDestroy();
	}
}

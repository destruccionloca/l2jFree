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
package com.l2jfree.gameserver.cache;

import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Nemesiss-
 */
public final class WarehouseCacheManager implements Runnable
{
	private static final Log _log = LogFactory.getLog(WarehouseCacheManager.class);
	
	public static WarehouseCacheManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<L2PcInstance, Long> _cache = new FastMap<L2PcInstance, Long>();
	
	private WarehouseCacheManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 60000, 60000);
		
		_log.info("WarehouseCacheManager: Initialized.");
	}
	
	public synchronized void add(L2PcInstance player)
	{
		_cache.put(player, System.currentTimeMillis());
	}
	
	public synchronized void remove(L2PcInstance player)
	{
		_cache.remove(player);
	}
	
	public synchronized void run()
	{
		for (Map.Entry<L2PcInstance, Long> entry : _cache.entrySet())
		{
			if (System.currentTimeMillis() > entry.getValue() + Config.WAREHOUSE_CACHE_TIME * 60000L)
			{
				final L2PcInstance player = entry.getKey();
				
				player.clearWarehouse();
				
				_cache.remove(player);
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final WarehouseCacheManager _instance = new WarehouseCacheManager();
	}
}

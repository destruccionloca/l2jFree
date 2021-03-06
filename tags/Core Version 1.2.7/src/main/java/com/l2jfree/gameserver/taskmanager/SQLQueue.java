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
package com.l2jfree.gameserver.taskmanager;

import java.sql.Connection;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * @author DiezelMax, NB4L1
 */
public final class SQLQueue implements Runnable
{
	private static final Log _log = LogFactory.getLog(SQLQueue.class);
	
	private static SQLQueue _instance;
	
	public static final SQLQueue getInstance()
	{
		if (_instance == null)
			_instance = new SQLQueue();
		
		return _instance;
	}
	
	private final FastList<SQLQuery> _queue = new FastList<SQLQuery>();
	
	private SQLQueue()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 60000, 60000);
		
		_log.info("SQLQueue: Initialized.");
	}
	
	public void execute(SQLQuery query)
	{
		add(query);
		
		run();
	}
	
	public void add(SQLQuery query)
	{
		synchronized (_queue)
		{
			_queue.add(query);
		}
	}
	
	private SQLQuery getNextQuery()
	{
		synchronized (_queue)
		{
			if (_queue.isEmpty())
				return null;
			
			return _queue.removeFirst();
		}
	}
	
	public synchronized void run()
	{
		if (_queue.isEmpty())
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			for (SQLQuery query; (query = getNextQuery()) != null;)
			{
				try
				{
					query.execute(con);
				}
				catch (Exception e)
				{
					_log.fatal("", e);
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}

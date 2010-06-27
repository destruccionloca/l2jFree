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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;

public final class AutoAnnouncements
{
	private static final Log _log = LogFactory.getLog(Announcements.class);
	
	public static AutoAnnouncements getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<Integer, AutoAnnouncer> _announcers = new FastMap<Integer, AutoAnnouncer>().setShared(true);
	
	private volatile int _nextId = 1;
	
	private AutoAnnouncements()
	{
		restore();
	}
	
	public void reload()
	{
		for (AutoAnnouncer exec : _announcers.values())
			exec.cancel();
		
		_announcers.clear();
		
		restore();
	}
	
	private void announce(String text)
	{
		Announcements.getInstance().announceToAll(text);
		
		_log.info("AutoAnnounce: " + text);
	}
	
	private void restore()
	{
		Connection conn = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement =
				conn.prepareStatement("SELECT id, initial, delay, cycle, memo FROM auto_announcements");
			ResultSet data = statement.executeQuery();
			
			while (data.next())
			{
				final int id = data.getInt("id");
				final long initial = data.getLong("initial");
				final long delay = data.getLong("delay");
				final int repeat = data.getInt("cycle");
				final String[] memo = data.getString("memo").split("\n");
				
				_announcers.put(id, new AutoAnnouncer(memo, repeat, initial, delay));
				
				_nextId = Math.max(_nextId, id + 1);
			}
			
			data.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("AutoAnnoucements: Failed to load announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
		
		_log.info("AutoAnnoucements: Loaded " + _announcers.size() + " Auto Annoucement Data.");
	}
	
	public void addAutoAnnounce(long initial, long delay, int repeat, String memo)
	{
		final int id = _nextId++;
		
		Connection conn = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = conn
					.prepareStatement("INSERT INTO auto_announcements (id, initial, delay, cycle, memo) VALUES (?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setLong(2, initial);
			statement.setLong(3, delay);
			statement.setInt(4, repeat);
			statement.setString(5, memo);
			statement.execute();
			statement.close();
			
			_announcers.put(id, new AutoAnnouncer(memo.split("\n"), repeat, initial, delay));
		}
		catch (Exception e)
		{
			_log.warn("AutoAnnoucements: Failed to add announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
	}
	
	public void deleteAutoAnnounce(int id)
	{
		final AutoAnnouncer announcer = _announcers.remove(id);
		
		if (announcer == null)
			return;
		
		announcer.cancel();
		
		Connection conn = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = conn.prepareStatement("DELETE FROM auto_announcements WHERE id = ?");
			statement.setInt(1, id);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("AutoAnnoucements: Failed to delete announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
	}
	
	private final class AutoAnnouncer implements Runnable
	{
		private final String[] _memo;
		private final Future<?> _task;
		
		private int _repeat = -1;
		
		private AutoAnnouncer(String[] memo, int repeat, long initial, long delay)
		{
			_memo = memo;
			
			if (repeat > 0)
				_repeat = repeat;
			else
				_repeat = -1;
			
			_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, initial * 1000, delay * 1000);
		}
		
		private void cancel()
		{
			_task.cancel(false);
		}
		
		@Override
		public void run()
		{
			for (String text : _memo)
				announce(text);
			
			if (_repeat > 0)
				_repeat--;
			
			if (_repeat == 0)
				cancel();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoAnnouncements _instance = new AutoAnnouncements();
	}
}

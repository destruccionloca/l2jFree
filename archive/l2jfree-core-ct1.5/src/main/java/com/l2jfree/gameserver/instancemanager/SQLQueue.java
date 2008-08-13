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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * This class manage queued sql's like INSERT/UPDATE/DELETE
 * 
 * @version $Revision: $ $Date: $
 * @author  DiezelMax
 */
public class SQLQueue
{
	protected static Log	_log	= LogFactory.getLog(SQLQueue.class.getName());

	private static SQLQueue	_instance;
	protected List<String>	_queue1	= null;
	protected List<String>	_queue2	= null;

	private SQLQueue()
	{
		_queue1 = new ArrayList<String>();
		_queue2 = new ArrayList<String>();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DoQueue(), 60000, 60000);
	}

	public static final SQLQueue getInstance()
	{
		if (_instance == null)
		{
			_instance = new SQLQueue();
		}
		return _instance;
	}

	public void add(String sql)
	{
		synchronized (_queue1)
		{
			_queue1.add(sql);
		}
	}

	protected class DoQueue extends Thread
	{
		@Override
		public void run()
		{
			flush();
		}
	}

	public void flush()
	{
		synchronized (_queue2)
		{
			synchronized (_queue1)
			{
				if (!_queue1.isEmpty())
				{
					_queue2.addAll(_queue1);
					_queue1.clear();
				}
			}

			if (_queue2.isEmpty())
				return;
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				for (String sql : _queue2)
				{
					try
					{
						statement = con.prepareStatement(sql);
						statement.executeUpdate();
						statement.close();
						sql = null;
					}
					catch (Exception e)
					{
						_log.fatal("error while porcessing sql queue " + e);
					}
				}
				_queue2.clear();
			}
			catch (Exception e)
			{
				_log.fatal("error while porcessing sql queue " + e);
			}
            finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

            if (_log.isDebugEnabled())
				_log.warn("SQLQueue: " + _queue2.size() + " sql's processed");
		}
	}
}

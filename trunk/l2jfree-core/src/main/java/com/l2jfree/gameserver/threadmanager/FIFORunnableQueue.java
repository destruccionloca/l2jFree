/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree.gameserver.threadmanager;

import javolution.util.FastList;

import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class FIFORunnableQueue<T extends Runnable> implements Runnable
{
	private static final byte NONE = 0;
	private static final byte QUEUED = 1;
	private static final byte RUNNING = 2;
	
	private final FastList<T> _queue = new FastList<T>();
	
	private volatile byte _state = NONE;
	
	public final void execute(T t)
	{
		addLast(t);
		
		synchronized (this)
		{
			if (_state != NONE)
				return;
			
			_state = QUEUED;
		}
		
		ThreadPoolManager.getInstance().execute(this);
	}
	
	private void addLast(T t)
	{
		synchronized (_queue)
		{
			_queue.addLast(t);
		}
	}
	
	private boolean isEmpty()
	{
		synchronized (_queue)
		{
			return _queue.isEmpty();
		}
	}
	
	private T removeFirst()
	{
		synchronized (_queue)
		{
			return _queue.removeFirst();
		}
	}
	
	public final void run()
	{
		while (!isEmpty())
		{
			try
			{
				synchronized (this)
				{
					if (_state == RUNNING)
						return;
					
					_state = RUNNING;
				}
				
				while (!isEmpty())
					ExecuteWrapper.execute(removeFirst());
			}
			finally
			{
				synchronized (this)
				{
					_state = NONE;
				}
			}
		}
	}
}

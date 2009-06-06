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
package com.l2jfree.gameserver.threadmanager;

import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class FIFOExecutableQueue implements Runnable
{
	private static final byte NONE = 0;
	private static final byte QUEUED = 1;
	private static final byte RUNNING = 2;
	
	private volatile byte _state = NONE;
	
	protected final void execute()
	{
		synchronized (this)
		{
			if (_state != NONE)
				return;
			
			_state = QUEUED;
		}
		
		ThreadPoolManager.getInstance().execute(this);
	}
	
	public final void run()
	{
		try
		{
			while (!isEmpty())
			{
				setState(QUEUED, RUNNING);
				
				try
				{
					while (!isEmpty())
						removeAndExecuteFirst();
				}
				finally
				{
					setState(RUNNING, QUEUED);
				}
			}
		}
		finally
		{
			setState(QUEUED, NONE);
		}
	}
	
	private synchronized void setState(byte expected, byte value)
	{
		try
		{
			if (_state != expected)
				throw new IllegalStateException("state: " + _state + ", expected: " + expected);
		}
		finally
		{
			_state = value;
		}
	}
	
	protected abstract boolean isEmpty();
	
	protected abstract void removeAndExecuteFirst();
}

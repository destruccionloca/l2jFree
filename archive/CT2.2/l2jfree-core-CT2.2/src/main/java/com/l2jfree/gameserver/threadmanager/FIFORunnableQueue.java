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
import com.l2jfree.util.concurrent.ExecuteWrapper;

/**
 * @author NB4L1
 */
public abstract class FIFORunnableQueue<T extends Runnable> extends FIFOExecutableQueue
{
	private final FastList<T> _queue = new FastList<T>();
	
	public final void execute(T t)
	{
		synchronized (_queue)
		{
			_queue.addLast(t);
		}
		
		execute();
	}
	
	@Override
	protected boolean isEmpty()
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
	
	@Override
	protected void removeAndExecuteFirst()
	{
		ExecuteWrapper.execute(removeFirst(), ThreadPoolManager.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
	}
}

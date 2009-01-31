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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class L2ThreadFactory implements ThreadFactory
{
	private final AtomicInteger _threadCount = new AtomicInteger();
	
	private final String _name;
	private final int _priority;
	private final ThreadGroup _threadGroup;
	
	public L2ThreadFactory(String name, int priority)
	{
		_name = name;
		_priority = priority;
		_threadGroup = new ThreadGroup(name);
		// _threadGroup.setMaxPriority(priority);
	}
	
	public int getThreadCount()
	{
		return _threadCount.get();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getPriority()
	{
		return _priority;
	}
	
	public ThreadGroup getThreadGroup()
	{
		return _threadGroup;
	}
	
	public Thread newThread(Runnable runnable)
	{
		Thread thread = new Thread(getThreadGroup(), runnable);
		thread.setName(getName() + "-" + _threadCount.incrementAndGet());
		thread.setPriority(getPriority());
		return thread;
	}
}
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree.util;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.l2jfree.gameserver.model.L2Object;

/**
 * @author NB4L1
 */
public final class L2ReadWriteCollection<T extends L2Object> implements L2Collection<T>
{
	private final Map<Integer, T> _map = new SingletonMap<Integer, T>();
	
	private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock _read = _lock.readLock();
	private final ReentrantReadWriteLock.WriteLock _write = _lock.writeLock();
	
	public int size()
	{
		_read.lock();
		try
		{
			return _map.size();
		}
		finally
		{
			_read.unlock();
		}
	}
	
	public boolean isEmpty()
	{
		_read.lock();
		try
		{
			return _map.isEmpty();
		}
		finally
		{
			_read.unlock();
		}
	}
	
	public boolean contains(T obj)
	{
		_read.lock();
		try
		{
			return _map.containsKey(obj.getObjectId());
		}
		finally
		{
			_read.unlock();
		}
	}
	
	public T get(Integer id)
	{
		_read.lock();
		try
		{
			return _map.get(id);
		}
		finally
		{
			_read.unlock();
		}
	}
	
	public void add(T obj)
	{
		_write.lock();
		try
		{
			_map.put(obj.getObjectId(), obj);
		}
		finally
		{
			_write.unlock();
		}
	}
	
	public void remove(T obj)
	{
		_write.lock();
		try
		{
			_map.remove(obj.getObjectId());
		}
		finally
		{
			_write.unlock();
		}
	}
	
	public void clear()
	{
		_write.lock();
		try
		{
			_map.clear();
		}
		finally
		{
			_write.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	public T[] toArray(T[] array)
	{
		_read.lock();
		try
		{
			if (array.length != _map.size())
				array = (T[])Array.newInstance(array.getClass().getComponentType(), _map.size());
			
			return _map.values().toArray(array);
		}
		finally
		{
			_read.unlock();
		}
	}
}

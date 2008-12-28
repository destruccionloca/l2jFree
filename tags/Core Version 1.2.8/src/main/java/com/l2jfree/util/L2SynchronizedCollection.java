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

import com.l2jfree.gameserver.model.L2Object;

/**
 * @author NB4L1
 */
public final class L2SynchronizedCollection<T extends L2Object> implements L2Collection<T>
{
	private final Map<Integer, T> _map = new SingletonMap<Integer, T>();
	
	public int size()
	{
		return _map.size();
	}
	
	public boolean isEmpty()
	{
		return _map.isEmpty();
	}
	
	public boolean contains(T obj)
	{
		return _map.containsKey(obj.getObjectId());
	}
	
	public T get(Integer id)
	{
		return _map.get(id);
	}
	
	public synchronized void add(T obj)
	{
		_map.put(obj.getObjectId(), obj);
	}
	
	public synchronized void remove(T obj)
	{
		_map.remove(obj.getObjectId());
	}
	
	public synchronized void clear()
	{
		_map.clear();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T[] toArray(T[] array)
	{
		if (array.length != _map.size())
			array = (T[])Array.newInstance(array.getClass().getComponentType(), _map.size());
		
		return _map.values().toArray(array);
	}
}

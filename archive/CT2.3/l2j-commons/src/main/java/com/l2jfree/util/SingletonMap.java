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
package com.l2jfree.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

/**
 * @author NB4L1
 */
public final class SingletonMap<K, V> implements Map<K, V>
{
	private boolean _initialized = false;
	private Map<K, V> _map = L2Collections.emptyMap();
	
	private boolean _shared = false;
	
	private void init()
	{
		if (!_initialized)
		{
			synchronized (this)
			{
				if (!_initialized)
				{
					_map = new FastMap<K, V>().setShared(_shared);
					_initialized = true;
				}
			}
		}
	}
	
	public SingletonMap<K, V> setShared()
	{
		_shared = true;
		
		synchronized (this)
		{
			if (_initialized)
			{
				((FastMap<K, V>)_map).setShared(true);
			}
		}
		
		return this;
	}
	
	public void clear()
	{
		_map.clear();
	}
	
	public boolean containsKey(Object key)
	{
		return _map.containsKey(key);
	}
	
	public boolean containsValue(Object value)
	{
		return _map.containsValue(value);
	}
	
	public Set<Entry<K, V>> entrySet()
	{
		return _map.entrySet();
	}
	
	public V get(Object key)
	{
		return _map.get(key);
	}
	
	public boolean isEmpty()
	{
		return _map.isEmpty();
	}
	
	public Set<K> keySet()
	{
		return _map.keySet();
	}
	
	public V put(K key, V value)
	{
		init();
		
		return _map.put(key, value);
	}
	
	public void putAll(Map<? extends K, ? extends V> m)
	{
		init();
		
		_map.putAll(m);
	}
	
	public V remove(Object key)
	{
		return _map.remove(key);
	}
	
	public int size()
	{
		return _map.size();
	}
	
	public Collection<V> values()
	{
		return _map.values();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "-" + _map.toString();
	}
}

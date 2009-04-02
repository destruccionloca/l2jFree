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
package com.l2jfree.gameserver.handler;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.lang.L2Integer;

/**
 * @author NB4L1
 */
public abstract class Handler<K, V>
{
	protected static final Log _log = LogFactory.getLog(Handler.class);
	
	private final HashMap<K, V> _map = new HashMap<K, V>();
	
	private final void put(K key, V handler)
	{
		V old = _map.put(key, handler);
		
		if (old != null)
			_log.warn(getClass().getSimpleName() + ": Replaced type(" + key + "), " + old + " -> " + handler + ".");
	}
	
	protected final void putAll(V handler, K... keys)
	{
		for (K key : keys)
			put(key, handler);
	}
	
	@SuppressWarnings("unchecked")
	protected final void putAll(V handler, int... keys)
	{
		for (int key : keys)
			put((K)L2Integer.valueOf(key), handler);
	}
	
	protected final V get(K key)
	{
		return _map.get(key);
	}
	
	protected final int size()
	{
		return _map.size();
	}
}

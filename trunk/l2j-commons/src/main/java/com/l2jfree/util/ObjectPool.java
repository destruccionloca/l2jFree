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

import java.util.ArrayList;

import com.l2jfree.tools.random.Rnd;

/**
 * @author NB4L1
 */
public abstract class ObjectPool<E>
{
	private static final L2Timer TIMER = new L2Timer(ObjectPool.class.getName());
	
	private final class ObjectPoolArrayList extends ArrayList<LinkedWrapper<E>>
	{
		public LinkedWrapper<E> removeLast()
		{
			final int size = size();
			
			if (size == 0)
				return null;
			else
				return remove(size - 1);
		}
		
		public void purge()
		{
			@SuppressWarnings("unchecked")
			final LinkedWrapper<E>[] array = (LinkedWrapper<E>[])toArray(new LinkedWrapper<?>[size()]);
			
			clear();
			
			for (final LinkedWrapper<E> wrapper : array)
			{
				if (wrapper == null)
					continue;
				
				if (wrapper._lastAccess + getMaxLifeTime() < System.currentTimeMillis())
					continue;
				
				add(wrapper);
			}
		}
	}
	
	private final ObjectPoolArrayList _wrappers = new ObjectPoolArrayList();
	private final ObjectPoolArrayList _values = new ObjectPoolArrayList();
	
	public ObjectPool()
	{
		TIMER.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				_wrappers.purge();
				_values.purge();
			}
		}, 60000, 60000 + Rnd.get(1000));
	}
	
	public final int getCurrentSize()
	{
		return _values.size();
	}
	
	protected int getMaximumSize()
	{
		return Integer.MAX_VALUE;
	}
	
	protected boolean isShared()
	{
		return true;
	}
	
	public long getMaxLifeTime()
	{
		return 600000; // 10 min
	}
	
	public void clear()
	{
		if (isShared())
		{
			synchronized (this)
			{
				_wrappers.trimToSize();
				_values.trimToSize();
			}
		}
		else
		{
			_wrappers.trimToSize();
			_values.trimToSize();
		}
	}
	
	public void store(E e)
	{
		if (isShared())
		{
			synchronized (this)
			{
				store0(e);
			}
		}
		else
		{
			store0(e);
		}
	}
	
	private void store0(E e)
	{
		if (getCurrentSize() >= getMaximumSize())
			return;
		
		reset(e);
		
		LinkedWrapper<E> wrapper = getWrapper();
		wrapper.setValue(e);
		
		_values.add(wrapper);
	}
	
	protected void reset(E e)
	{
	}
	
	public E get()
	{
		if (isShared())
		{
			synchronized (this)
			{
				return get0();
			}
		}
		else
		{
			return get0();
		}
	}
	
	private E get0()
	{
		LinkedWrapper<E> wrapper = _values.removeLast();
		
		if (wrapper == null)
			return create();
		
		final E e = wrapper.getValue();
		
		storeWrapper(wrapper);
		
		return e == null ? create() : e;
	}
	
	protected abstract E create();
	
	private void storeWrapper(LinkedWrapper<E> wrapper)
	{
		wrapper.setValue(null);
		
		_wrappers.add(wrapper);
	}
	
	private LinkedWrapper<E> getWrapper()
	{
		LinkedWrapper<E> wrapper = _wrappers.removeLast();
		
		if (wrapper == null)
			wrapper = new LinkedWrapper<E>();
		
		wrapper.setValue(null);
		
		return wrapper;
	}
	
	private static final class LinkedWrapper<E>
	{
		private long _lastAccess = System.currentTimeMillis();
		private E _value;
		
		private E getValue()
		{
			_lastAccess = System.currentTimeMillis();
			return _value;
		}
		
		private void setValue(E value)
		{
			_lastAccess = System.currentTimeMillis();
			_value = value;
		}
	}
}

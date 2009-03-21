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

/**
 * @author NB4L1
 */
public abstract class ObjectPool<E>
{
	private LinkedWrapper<E> _wrappers;
	private LinkedWrapper<E> _values;
	
	public final synchronized void store(E e)
	{
		LinkedWrapper<E> wrapper = getWrapper();
		wrapper.setValue(e);
		wrapper.setNext(_values);
		
		_values = wrapper;
	}
	
	public final synchronized E get()
	{
		LinkedWrapper<E> wrapper = _values;
		
		if (wrapper == null)
			return create();
		
		_values = _values.getNext();
		
		final E e = wrapper.getValue();
		
		storeWrapper(wrapper);
		
		return e == null ? create() : e;
	}
	
	protected abstract E create();
	
	private void storeWrapper(LinkedWrapper<E> wrapper)
	{
		wrapper.setValue(null);
		wrapper.setNext(_wrappers);
		
		_wrappers = wrapper;
	}
	
	private LinkedWrapper<E> getWrapper()
	{
		LinkedWrapper<E> wrapper = _wrappers;
		
		if (_wrappers != null)
			_wrappers = _wrappers.getNext();
		
		if (wrapper == null)
			wrapper = new LinkedWrapper<E>();
		
		wrapper.setValue(null);
		wrapper.setNext(null);
		
		return wrapper;
	}
	
	private static final class LinkedWrapper<E>
	{
		private LinkedWrapper<E> _next;
		private E _value;
		
		private E getValue()
		{
			return _value;
		}
		
		private void setValue(E value)
		{
			_value = value;
		}
		
		private LinkedWrapper<E> getNext()
		{
			return _next;
		}
		
		private void setNext(LinkedWrapper<E> next)
		{
			_next = next;
		}
	}
}

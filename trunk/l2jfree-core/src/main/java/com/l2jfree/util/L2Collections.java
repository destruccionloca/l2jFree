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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * @author NB4L1
 */
public final class L2Collections
{
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	private static final class EmptyListIterator implements ListIterator<Object>
	{
		private static final ListIterator<Object> INSTANCE = new EmptyListIterator();
		
		public boolean hasNext()
		{
			return false;
		}
		
		public Object next()
		{
			throw new UnsupportedOperationException();
		}
		
		public boolean hasPrevious()
		{
			return false;
		}
		
		public Object previous()
		{
			throw new UnsupportedOperationException();
		}
		
		public int nextIndex()
		{
			return 0;
		}
		
		public int previousIndex()
		{
			return -1;
		}
		
		public void add(Object obj)
		{
			throw new UnsupportedOperationException();
		}
		
		public void set(Object obj)
		{
			throw new UnsupportedOperationException();
		}
		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class EmptyCollection implements Collection<Object>
	{
		private static final Collection<Object> INSTANCE = new EmptyCollection();
		
		public boolean add(Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}
		
		public void clear()
		{
		}
		
		public boolean contains(Object o)
		{
			return false;
		}
		
		public boolean containsAll(Collection<?> c)
		{
			return false;
		}
		
		public boolean isEmpty()
		{
			return true;
		}
		
		public Iterator<Object> iterator()
		{
			return emptyListIterator();
		}
		
		public boolean remove(Object o)
		{
			return false;
		}
		
		public boolean removeAll(Collection<?> c)
		{
			return false;
		}
		
		public boolean retainAll(Collection<?> c)
		{
			return false;
		}
		
		public int size()
		{
			return 0;
		}
		
		public Object[] toArray()
		{
			return EMPTY_ARRAY;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a)
		{
			return (T[])toArray();
		}
	}
	
	private static final class EmptySet extends EmptyCollection implements Set<Object>
	{
		private static final Set<Object> INSTANCE = new EmptySet();
	}
	
	private static final class EmptyList extends EmptyCollection implements List<Object>
	{
		private static final List<Object> INSTANCE = new EmptyList();
		
		public void add(int index, Object element)
		{
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(int index, Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}
		
		public Object get(int index)
		{
			throw new UnsupportedOperationException();
		}
		
		public int indexOf(Object o)
		{
			return -1;
		}
		
		public int lastIndexOf(Object o)
		{
			return -1;
		}
		
		public ListIterator<Object> listIterator()
		{
			return emptyListIterator();
		}
		
		public ListIterator<Object> listIterator(int index)
		{
			return emptyListIterator();
		}
		
		public Object remove(int index)
		{
			throw new UnsupportedOperationException();
		}
		
		public Object set(int index, Object element)
		{
			throw new UnsupportedOperationException();
		}
		
		public List<Object> subList(int fromIndex, int toIndex)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static final class EmptyMap implements Map<Object, Object>
	{
		private static final Map<Object, Object> INSTANCE = new EmptyMap();
		
		public void clear()
		{
		}
		
		public boolean containsKey(Object key)
		{
			return false;
		}
		
		public boolean containsValue(Object value)
		{
			return false;
		}
		
		public Set<Map.Entry<Object, Object>> entrySet()
		{
			return emptySet();
		}
		
		public Object get(Object key)
		{
			return null;
		}
		
		public boolean isEmpty()
		{
			return true;
		}
		
		public Set<Object> keySet()
		{
			return emptySet();
		}
		
		public Object put(Object key, Object value)
		{
			throw new UnsupportedOperationException();
		}
		
		public void putAll(Map<? extends Object, ? extends Object> m)
		{
			throw new UnsupportedOperationException();
		}
		
		public Object remove(Object key)
		{
			return null;
		}
		
		public int size()
		{
			return 0;
		}
		
		public Collection<Object> values()
		{
			return emptyCollection();
		}
	}
	
	private static class EmptyBunch implements IBunch<Object>
	{
		private static final IBunch<Object> INSTANCE = new EmptyBunch();
		
		public IBunch<Object> add(Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		public IBunch<Object> addAll(Iterable<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}
		
		public IBunch<Object> addAll(Object[] array)
		{
			throw new UnsupportedOperationException();
		}
		
		public void clear()
		{
		}
		
		public boolean contains(Object o)
		{
			return false;
		}
		
		public Object get(int index)
		{
			throw new UnsupportedOperationException();
		}
		
		public boolean isEmpty()
		{
			return true;
		}
		
		public Iterator<Object> iterator()
		{
			return emptyListIterator();
		}
		
		public Object[] moveToArray()
		{
			return EMPTY_ARRAY;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T[] moveToArray(T[] array)
		{
			if (array.length != 0)
				return (T[])moveToArray();
			
			return array;
		}
		
		public List<Object> moveToList(List<Object> list)
		{
			return list;
		}
		
		public IBunch<Object> remove(Object o)
		{
			return this;
		}
		
		public Object remove(int index)
		{
			throw new UnsupportedOperationException();
		}
		
		public Object set(int index, Object value)
		{
			throw new UnsupportedOperationException();
		}
		
		public int size()
		{
			return 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> ListIterator<T> emptyListIterator()
	{
		return (ListIterator<T>)EmptyListIterator.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Collection<T> emptyCollection()
	{
		return (Collection<T>)EmptyCollection.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> emptySet()
	{
		return (Set<T>)EmptySet.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyList()
	{
		return (List<T>)EmptyList.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static final <K, V> Map<K, V> emptyMap()
	{
		return (Map<K, V>)EmptyMap.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> IBunch<T> emptyBunch()
	{
		return (IBunch<T>)EmptyBunch.INSTANCE;
	}
}

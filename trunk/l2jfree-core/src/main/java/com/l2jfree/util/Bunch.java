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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>
 * Default implementation of {@link IBunch<E>}. It uses nodes to storing objects, so no array reallocation needed -
 * like array-based collections. The created nodes are reused so garbage is reduced heavily.
 * </p>
 * 
 * @author NB4L1
 */
@SuppressWarnings("unchecked")
public class Bunch<E> implements IBunch<E>
{
	private static final class Node
	{
		private static final ObjectPool<Node> POOL = new ObjectPool<Node>() {
			@Override
			protected Node create()
			{
				return new Node();
			}
		};
		
		private static <E> Node newInstance(final Bunch<E> bunch, final Object value)
		{
			final Node node = POOL.get();
			
			node.previous = bunch._last;
			node.value = value;
			node.next = null;
			
			if (bunch._last != null)
				bunch._last.next = node;
			bunch._last = node;
			bunch._size++;
			
			return node;
		}
		
		private static <E> void recycle(final Bunch<E> bunch, final Node node)
		{
			bunch._size--;
			if (bunch._last == node)
				bunch._last = bunch._last.previous;
			
			if (node.previous != null)
				node.previous.next = node.next;
			
			node.value = null;
			
			if (node.next != null)
				node.next.previous = node.previous;
			
			POOL.store(node);
		}
		
		private Node previous;
		private Object value;
		private Node next;
		
		private Node getPreviousInBunch()
		{
			for (Node node = this; (node = node.previous) != null;)
			{
				if (node.isInBunch())
					return node;
			}
			
			throw new InternalError();
		}
		
		private Node getNextInBunch()
		{
			for (Node node = getPreviousInBunch(); (node = node.next) != null;)
			{
				if (node != this)
					return node;
			}
			
			return null;
		}
		
		private boolean isInBunch()
		{
			return value != null;
		}
	}
	
	private static final Object NULL = new Object();
	
	private final Node _first = Node.newInstance(this, NULL);
	private Node _last;
	
	private int _size = 0;
	
	private E valueOf(Node node)
	{
		return (E)node.value;
	}
	
	private void delete(Node node)
	{
		Node.recycle(this, node);
	}
	
	private Node getNode(int index)
	{
		if (index < 0 || size() <= index)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		
		int i = 0;
		for (Node node = _first; (node = node.next) != null; i++)
			if (i == index)
				return node;
		
		return null;
	}
	
	public int size()
	{
		return _size;
	}
	
	public Bunch<E> add(E value)
	{
		if (value == null)
			throw new NullPointerException();
		
		Node.newInstance(this, value);
		return this;
	}
	
	public Bunch<E> remove(Object value)
	{
		for (Node node = _first; (node = node.next) != null;)
		{
			if (equals(value, valueOf(node)))
			{
				Node tmp = node.previous;
				
				delete(node);
				
				node = tmp;
			}
		}
		
		return this;
	}
	
	public void clear()
	{
		for (Node node = _first; (node = node.next) != null;)
		{
			Node tmp = node.previous;
			
			delete(node);
			
			node = tmp;
		}
	}
	
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	public E get(int index)
	{
		return valueOf(getNode(index));
	}
	
	public E set(int index, E value)
	{
		if (value == null)
			throw new NullPointerException();
		
		final Node node = getNode(index);
		final E old = valueOf(node);
		
		node.value = value;
		
		return old;
	}
	
	public E remove(int index)
	{
		final Node node = getNode(index);
		final E value = valueOf(node);
		
		delete(node);
		
		return value;
	}
	
	public boolean contains(Object value)
	{
		for (Node node = _first; (node = node.next) != null;)
		{
			if (equals(value, valueOf(node)))
				return true;
		}
		
		return false;
	}
	
	private Bunch<E> addAll(Bunch<? extends E> b)
	{
		if (b != null)
			for (Node node = b._first; (node = node.next) != null;)
				add(valueOf(node));
		
		return this;
	}
	
	public Bunch<E> addAll(Iterable<? extends E> c)
	{
		if (c instanceof Bunch<?>)
			addAll((Bunch<? extends E>)c);
		
		if (c != null)
			for (E e : c)
				add(e);
		
		return this;
	}
	
	public Iterator<E> iterator()
	{
		return BunchIterator.newInstance(this);
	}
	
	public static <T> void recycleIterator(BunchIterator<T> bunchIterator)
	{
		BunchIterator.recycle(bunchIterator);
	}
	
	public Object[] moveToArray()
	{
		return moveToArray(new Object[size()]);
	}
	
	public <T> T[] moveToArray(T[] array)
	{
		if (array.length != size())
			array = (T[])Array.newInstance(array.getClass().getComponentType(), size());
		
		if (isEmpty() && array.length == 0)
			return array;
		
		int i = 0;
		for (Node node = _first; (node = node.next) != null && i < array.length;)
		{
			array[i++] = (T)valueOf(node);
			
			Node tmp = node.previous;
			
			delete(node);
			
			node = tmp;
		}
		
		clear();
		
		return array;
	}
	
	public List<E> moveToList(List<E> list)
	{
		for (Node node = _first; (node = node.next) != null;)
		{
			list.add(valueOf(node));
			
			Node tmp = node.previous;
			
			delete(node);
			
			node = tmp;
		}
		
		clear();
		
		return list;
	}
	
	private static boolean equals(Object o1, Object o2)
	{
		return o1 == null ? o2 == null : o1 == o2 || o1.equals(o2);
	}
	
	private static final class BunchIterator<T> implements Iterator<T>
	{
		private static final ObjectPool<BunchIterator<?>> POOL = new ObjectPool<BunchIterator<?>>() {
			@Override
			protected BunchIterator<?> create()
			{
				return new BunchIterator<Object>();
			}
		};
		
		private static <T> BunchIterator<T> newInstance(final Bunch<T> bunch)
		{
			final BunchIterator<T> bunchIterator = (BunchIterator<T>)POOL.get();
			bunchIterator._bunch = bunch;
			bunchIterator._current = null;
			bunchIterator._next = bunch._first.next;
			
			return bunchIterator;
		}
		
		private static <T> void recycle(final BunchIterator<T> bunchIterator)
		{
			bunchIterator._bunch = null;
			bunchIterator._current = null;
			bunchIterator._next = null;
			
			POOL.store(bunchIterator);
		}
		
		private Bunch<T> _bunch;
		private Node _current;
		private Node _next;
		
		public boolean hasNext()
		{
			if (_next != null && !_next.isInBunch())
				_next = _next.getNextInBunch();
			
			return _next != null;
		}
		
		public T next()
		{
			if (!hasNext())
				throw new NoSuchElementException();
			
			_current = _next;
			_next = _next.next;
			
			return _bunch.valueOf(_current);
		}
		
		public void remove()
		{
			if (_current == null)
				throw new IllegalStateException();
			
			_bunch.delete(_current);
			_current = null;
		}
	}
}

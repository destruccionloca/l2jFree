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
import java.util.List;
import java.util.ListIterator;

import javolution.util.FastList;

/**
 * @author NB4L1
 */
public final class SingletonList<E> extends SingletonCollection<E, List<E>> implements List<E>
{
	@Override
	protected List<E> emptyCollection()
	{
		return L2Collections.emptyList();
	}
	
	@Override
	protected List<E> initCollection()
	{
		return FastList.newInstance();
	}
	
	public void add(int index, E element)
	{
		init();
		
		_collection.add(index, element);
	}
	
	public boolean addAll(int index, Collection<? extends E> c)
	{
		init();
		
		return _collection.addAll(index, c);
	}
	
	public E get(int index)
	{
		return _collection.get(index);
	}
	
	public int indexOf(Object o)
	{
		return _collection.indexOf(o);
	}
	
	public int lastIndexOf(Object o)
	{
		return _collection.lastIndexOf(o);
	}
	
	public ListIterator<E> listIterator()
	{
		return _collection.listIterator();
	}
	
	public ListIterator<E> listIterator(int index)
	{
		return _collection.listIterator(index);
	}
	
	public E remove(int index)
	{
		return _collection.remove(index);
	}
	
	public E set(int index, E element)
	{
		return _collection.set(index, element);
	}
	
	public List<E> subList(int fromIndex, int toIndex)
	{
		return _collection.subList(fromIndex, toIndex);
	}
}

package com.l2jfree.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

import com.l2jfree.util.L2Collections.Filter;

/**
 * <p>
 * An implementation of {@link Bunch} backed by a {@link List}, which is reused, so garbage is reduced heavily.
 * </p>
 * 
 * @author NB4L1
 */
public abstract class AbstractListBunch<E> extends AbstractBunch<E>
{
	private List<E> _list;
	
	protected AbstractListBunch()
	{
		_list = initList();
	}
	
	protected abstract List<E> initList();
	
	protected abstract void recycleList(List<E> list);
	
	@Override
	public int size()
	{
		return _list.size();
	}
	
	@Override
	public Bunch<E> add(E value)
	{
		if (value == null)
		{
			_log.warn("Null element added!", new NullPointerException());
			return this;
		}
		
		_list.add(value);
		return this;
	}
	
	@Override
	public Bunch<E> remove(E value)
	{
		_list.remove(value);
		return this;
	}
	
	@Override
	public void clear()
	{
		recycleList(_list);
		
		_list = null;
	}
	
	@Override
	public boolean isEmpty()
	{
		return _list.isEmpty();
	}
	
	@Override
	public E get(int index)
	{
		return _list.get(index);
	}
	
	@Override
	public E set(int index, E value)
	{
		if (value == null)
		{
			_log.warn("Null element added!", new NullPointerException());
			return null;
		}
		
		return _list.set(index, value);
	}
	
	@Override
	public E remove(int index)
	{
		return _list.remove(index);
	}
	
	@Override
	public boolean contains(E value)
	{
		return _list.contains(value);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] moveToArray(T[] array)
	{
		if (array.length != size())
			array = (T[])Array.newInstance(array.getClass().getComponentType(), size());
		
		_list.toArray(array);
		
		clear();
		
		return array;
	}
	
	@Override
	public List<E> moveToList(List<E> list)
	{
		list.addAll(_list);
		
		clear();
		
		return list;
	}
	
	@Override
	public Bunch<E> cleanByFilter(Filter<E> filter)
	{
		for (Iterator<E> it = _list.iterator(); it.hasNext();)
			if (!filter.accept(it.next()))
				it.remove();
		
		return this;
	}
}

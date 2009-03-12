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

import java.util.List;

import com.l2jfree.util.L2Collections.Filter;

/**
 * <p>
 * This is a very special "collection" - made for a special purpose. There is a lot of unnecessary garbage created by
 * collections during normal execution. Collections used ONLY locally ONLY to collect objects temporarily should be
 * replaced with this. The implementations MUST reuse inner structures created for storing.
 * </p>
 * <p>
 * It's default usage is to create it, add/remove elements, and finally "clean it up", with one of the
 * {@link #moveToArray()}, {@link #moveToArray(Object[])}, {@link #moveToList(List)} methods. During that method it
 * fills up the returned array/list with the values of it, and reuses inner objects.
 * </p>
 * <br>
 * <font color=#FF0000>IMPORTANT RULES:</font>
 * <ul>
 * <li><code>null</code> elements are not allowed</li>
 * <li>proper concurrent access are not guaranteed yet - must be used only by a single thread</li>
 * </ul>
 * 
 * @author NB4L1
 */
public interface IBunch<E>
{
	public int size();
	
	public IBunch<E> add(E value);
	
	public IBunch<E> remove(E value);
	
	public void clear();
	
	public boolean isEmpty();
	
	public E get(int index);
	
	public E set(int index, E value);
	
	public E remove(int index);
	
	public boolean contains(E value);
	
	public IBunch<E> addAll(Iterable<? extends E> c);
	
	public IBunch<E> addAll(E[] array);
	
	public Object[] moveToArray();
	
	public <T> T[] moveToArray(T[] array);
	
	public List<E> moveToList(List<E> list);
	
	public IBunch<E> cleanByFilter(Filter<E> filter);
}

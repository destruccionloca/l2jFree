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

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javolution.util.FastList;

/**
 * @author NB4L1
 */
public final class L2Arrays
{
	private L2Arrays()
	{
	}
	
	public static int countNull(Object[] array)
	{
		if (array == null)
			return 0;
		
		int nullCount = 0;
		
		for (Object obj : array)
			if (obj == null)
				nullCount++;
		
		return nullCount;
	}
	
	public static int countNotNull(Object[] array)
	{
		return array == null ? 0 : array.length - countNull(array);
	}
	
	/**
	 * @param <T>
	 * @param array to remove null elements from
	 * @return an array without null elements - can be the same, if the original contains no null elements
	 * @throws NullPointerException if array is null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] compact(T[] array)
	{
		final int newSize = countNotNull(array);
		
		if (array.length == newSize)
			return array;
		
		final T[] result = (T[])Array.newInstance(array.getClass().getComponentType(), newSize);
		
		int index = 0;
		
		for (T t : array)
			if (t != null)
				result[index++] = t;
		
		return result;
	}
	
	/**
	 * @param <T>
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside
	 *         a foreach loop, and supports addition
	 */
	public static <T> List<T> asForeachSafeList(T... array)
	{
		return asForeachSafeList(true, array);
	}
	
	/**
	 * @param <T>
	 * @param allowAddition determines that list MUST support add operation or not
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside
	 *         a foreach loop, and supports addition if required
	 */
	public static <T> List<T> asForeachSafeList(boolean allowAddition, T... array)
	{
		final int newSize = countNotNull(array);
		
		if (newSize == 0 && !allowAddition)
			return L2Collections.emptyList();
		
		if (newSize <= 8)
			return new CopyOnWriteArrayList<T>(compact(array));
		
		final List<T> result = new FastList<T>(newSize);
		
		for (T t : array)
			if (t != null)
				result.add(t);
		
		return result;
	}
}

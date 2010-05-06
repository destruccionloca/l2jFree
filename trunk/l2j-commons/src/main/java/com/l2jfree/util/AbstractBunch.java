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

/**
 * @author NB4L1
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBunch<E> implements Bunch<E>
{
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	@Override
	public Object[] moveToArray()
	{
		return moveToArray(new Object[size()]);
	}
	
	@Override
	public <T> T[] moveToArray(Class<T> clazz)
	{
		return moveToArray((T[])Array.newInstance(clazz, size()));
	}
}

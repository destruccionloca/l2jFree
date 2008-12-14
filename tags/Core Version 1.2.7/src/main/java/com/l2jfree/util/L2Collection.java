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

import com.l2jfree.gameserver.model.L2Object;

/**
 * @author NB4L1
 */
public interface L2Collection<T extends L2Object>
{
	public int size();
	
	public boolean isEmpty();
	
	public boolean contains(T obj);
	
	public T get(Integer id);
	
	public void add(T obj);
	
	public void remove(T obj);
	
	public void clear();
	
	public T[] toArray(T[] array);
}

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
package com.l2jfree.gameserver.exception.clientpackets;

import com.l2jfree.gameserver.exception.L2JFunctionnalException;

public class MultiSellChooseException extends L2JFunctionnalException
{

	/**
	 * serial Version uid
	 */
	private static final long	serialVersionUID	= 5760255150312655106L;

	public MultiSellChooseException(String msg)
	{
		super(msg);
	}
}

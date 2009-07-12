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
package org.mmocore.network;

/**
 * @author KenM
 */
abstract class HeaderHandler<T extends MMOConnection<T>, H extends HeaderHandler<T, H>>
{
	private final H _subHeaderHandler;
	
	HeaderHandler(H subHeaderHandler)
	{
		_subHeaderHandler = subHeaderHandler;
	}
	
	final H getSubHeaderHandler()
	{
		return _subHeaderHandler;
	}
	
	final boolean isChildHeaderHandler()
	{
		return getSubHeaderHandler() == null;
	}
	
	private final HeaderInfo<T> _headerInfoReturn = new HeaderInfo<T>();
	
	protected final HeaderInfo<T> getHeaderInfoReturn()
	{
		return _headerInfoReturn;
	}
}

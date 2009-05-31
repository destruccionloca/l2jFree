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
package com.l2jfree.gameserver.handler;

import java.util.Map;

import javolution.util.FastMap;

/**
 * @author nBd
 */

public class IrcCommandHandler
{
	private static IrcCommandHandler		_instance;

	private Map<String, IIrcCommandHandler>	_datatable;

	public static IrcCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new IrcCommandHandler();
		}
		return _instance;
	}

	private IrcCommandHandler()
	{
		_datatable = new FastMap<String, IIrcCommandHandler>();
	}

	public void registerIrcCommandHandler(IIrcCommandHandler handler)
	{
		String[] ids = handler.getIrcCommandList();

		for (String element : ids)
			_datatable.put(element, handler);
	}

	public IIrcCommandHandler getIrcCommandHandler(String BypassCommand)
	{
		String command = BypassCommand;

		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}
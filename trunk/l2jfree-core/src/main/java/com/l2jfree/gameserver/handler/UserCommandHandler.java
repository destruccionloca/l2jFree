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

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.usercommandhandlers.ChannelDelete;
import com.l2jfree.gameserver.handler.usercommandhandlers.ChannelLeave;
import com.l2jfree.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import com.l2jfree.gameserver.handler.usercommandhandlers.ClanPenalty;
import com.l2jfree.gameserver.handler.usercommandhandlers.ClanWarsList;
import com.l2jfree.gameserver.handler.usercommandhandlers.DisMount;
import com.l2jfree.gameserver.handler.usercommandhandlers.Escape;
import com.l2jfree.gameserver.handler.usercommandhandlers.FatigueTime;
import com.l2jfree.gameserver.handler.usercommandhandlers.GraduateList;
import com.l2jfree.gameserver.handler.usercommandhandlers.InstanceZone;
import com.l2jfree.gameserver.handler.usercommandhandlers.Loc;
import com.l2jfree.gameserver.handler.usercommandhandlers.Mount;
import com.l2jfree.gameserver.handler.usercommandhandlers.OlympiadStat;
import com.l2jfree.gameserver.handler.usercommandhandlers.PartyInfo;
import com.l2jfree.gameserver.handler.usercommandhandlers.SiegeStatus;
import com.l2jfree.gameserver.handler.usercommandhandlers.Time;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class UserCommandHandler
{
	private final static Log						_log	= LogFactory.getLog(UserCommandHandler.class.getName());

	private static UserCommandHandler				_instance;

	private FastMap<Integer, IUserCommandHandler>	_datatable;

	public static UserCommandHandler getInstance()
	{
		if (_instance == null)
			_instance = new UserCommandHandler();
		return _instance;
	}

	private UserCommandHandler()
	{
		_datatable = new FastMap<Integer, IUserCommandHandler>();
		registerUserCommandHandler(new ChannelLeave());
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelListUpdate());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new DisMount());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new FatigueTime());
		registerUserCommandHandler(new GraduateList());
		registerUserCommandHandler(new InstanceZone());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new Mount());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new SiegeStatus());
		registerUserCommandHandler(new Time());

		_log.info("UserCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int element : ids)
		{
			if (_log.isDebugEnabled())
				_log.debug("Adding handler for user command " + element);
			_datatable.put(Integer.valueOf(element), handler);
		}
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		if (_log.isDebugEnabled())
			_log.debug("getting handler for user command: " + userCommand);
		return _datatable.get(Integer.valueOf(userCommand));
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}
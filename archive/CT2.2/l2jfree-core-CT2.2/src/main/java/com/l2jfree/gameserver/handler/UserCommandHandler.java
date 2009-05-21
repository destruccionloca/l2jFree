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
import com.l2jfree.util.NumberHandlerRegistry;

public final class UserCommandHandler extends NumberHandlerRegistry<IUserCommandHandler>
{
	private static UserCommandHandler _instance;
	
	public static UserCommandHandler getInstance()
	{
		if (_instance == null)
			_instance = new UserCommandHandler();
		
		return _instance;
	}
	
	private UserCommandHandler()
	{
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelLeave());
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
		
		_log.info("UserCommandHandler: Loaded " + size() + " handlers.");
	}
	
	private void registerUserCommandHandler(IUserCommandHandler handler)
	{
		registerAll(handler, handler.getUserCommandList());
	}
	
	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return get(userCommand);
	}
}

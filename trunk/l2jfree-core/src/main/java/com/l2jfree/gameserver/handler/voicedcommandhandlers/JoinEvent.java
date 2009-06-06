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
package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.AutomatedTvT;

/**
 * @author savormix
 *
 */
public class JoinEvent implements IVoicedCommandHandler
{
	private static final String[] CMDS = {
		"jointvt", "joinTvT", "joinTVT", "JOINTVT", "leavetvt", "leaveTvT", "leaveTVT", "LEAVETVT"
	};

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, com.l2jfree.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equals(CMDS[0]) || command.equals(CMDS[1]) || command.equals(CMDS[2]) || command.equals(CMDS[3]))
		{
			AutomatedTvT.getInstance().registerPlayer(activeChar);
			return true;
		}
		else if (Config.AUTO_TVT_REGISTER_CANCEL)
		{
			AutomatedTvT.getInstance().cancelRegistration(activeChar);
			return true;
		}
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return CMDS;
	}
}

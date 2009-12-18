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
package com.l2jfree.gameserver.handler.usercommandhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IUserCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

public class RemainTime implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	= {
		53
	};

	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (Config.SERVER_LIST_CLOCK)
			// we do not support limited time/week servers
			activeChar.sendPacket(SystemMessageId.WEEKS_USAGE_TIME_FINISHED);
		else // verified
			activeChar.sendPacket(SystemMessageId.RELAX_SERVER_ONLY);
		return true;
	}

	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}

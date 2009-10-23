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

import com.l2jfree.gameserver.handler.IUserCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExNotifyBirthDay;

public class Birthday implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	=
											{ 126, 161714928 };

	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		// The login server has birth day data
		// But we don't really know how the system works in retail, so
		if (activeChar.isGM())
			activeChar.sendPacket(ExNotifyBirthDay.PACKET);
		else
			activeChar.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
		return true;
	}

	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}

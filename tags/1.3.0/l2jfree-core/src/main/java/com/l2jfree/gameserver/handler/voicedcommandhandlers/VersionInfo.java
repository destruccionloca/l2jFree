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

import com.l2jfree.gameserver.CoreInfo;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author evill33t
 * 
 */
public class VersionInfo  implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "version" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("version"))
		{
			CoreInfo.versionInfo(activeChar);
			return true;
		}
		return false;
	}
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}

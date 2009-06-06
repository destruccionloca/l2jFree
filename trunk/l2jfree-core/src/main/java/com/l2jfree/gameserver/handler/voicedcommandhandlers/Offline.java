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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;

public class Offline implements IVoicedCommandHandler
{
	protected static Log			_log			= LogFactory.getLog(Offline.class);
	private static final String[]	VOICED_COMMANDS	= { "offline" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if ((activeChar.getPrivateStoreType() == 1 || activeChar.getPrivateStoreType() == 3 || (activeChar.getPrivateStoreType() == 5 && Config.ALLOW_OFFLINE_TRADE_CRAFT)))
		{
			if (activeChar.isInsideZone(L2Zone.FLAG_PEACE) || activeChar.isGM())
			{
				return activeChar.enterOfflineMode();
			}
			else
			{
				activeChar.sendMessage("You must be in a peace zone to use offline mode");
				return false;
			}
		}
		else
		{
			activeChar.sendMessage("You must be in a peace zone to use offline mode");
			return false;
		}
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}

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
package com.l2jfree.gameserver.handler.admincommandhandlers;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.GmListTable;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands:
 * - gm = turns gm mode on/off
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminGm implements IAdminCommandHandler
{
	private final static Log		_log			= LogFactory.getLog(AdminGm.class.getName());
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_gm" };
	private static final int		REQUIRED_LEVEL	= Config.GM_ACCESSLEVEL;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		//don't check for gm status ;)
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!checkLevel(activeChar.getAccessLevel()))
				return false;
		}

		if (command.equals("admin_gm"))
			handleGm(activeChar);

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}

	private void handleGm(L2PcInstance activeChar)
	{
		if (activeChar.isGM())
		{
			GmListTable.getInstance().deleteGm(activeChar);
			activeChar.setIsGM(false);

			activeChar.sendMessage("You no longer have GM status.");
			activeChar.updateNameTitleColor();

			if (_log.isDebugEnabled())
				_log.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status off");
		}
		else
		{
			GmListTable.getInstance().addGm(activeChar, false);
			activeChar.setIsGM(true);

			activeChar.sendMessage("You now have GM status.");
			activeChar.updateNameTitleColor();

			if (_log.isDebugEnabled())
				_log.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status on");
		}
	}
}

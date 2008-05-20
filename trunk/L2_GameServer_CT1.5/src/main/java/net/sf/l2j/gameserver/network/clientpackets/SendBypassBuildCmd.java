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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.AdminCommandAccessRights;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles all GM commands triggered by //command
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:29 $
 */
public class SendBypassBuildCmd extends L2GameClientPacket
{
	private final static Log _log = LogFactory.getLog(SendBypassBuildCmd.class.getName());

	private static final String _C__5B_SENDBYPASSBUILDCMD = "[C] 5b SendBypassBuildCmd";
	public final static int GM_MESSAGE = 9;
	public final static int ANNOUNCEMENT = 10;

	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = ("admin_" + readS()).trim();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// DaDummy: this way we log _every_ admincommand with all related info
		String command;
		String params;

		if (_command.indexOf(" ") != -1)
		{
			command = _command.substring(0, _command.indexOf(" "));
			params  = _command.substring(_command.indexOf(" "));
		}
		else
		{
			command = _command;
			params  = "";
		}

		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);

		if (ach == null)
		{
			if ( activeChar.isGM() )
				activeChar.sendMessage("The command " + command + " does not exists!");

			_log.warn("No handler registered for admin command '" + command + "'");
			return;
		}

		if (!AdminCommandAccessRights.getInstance().hasAccess(command , activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			_log.warn("Character " + activeChar.getName() + " tried to use admin command " + command + ", but doesn't have access to it!");
			return;
		}

		GMAudit.auditGMAction(activeChar, "admincommand", command, params);
		ach.useAdminCommand("admin_"+_command, activeChar);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__5B_SENDBYPASSBUILDCMD;
	}
}

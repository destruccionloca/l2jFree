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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;

/**
 * 
 *
 */
public class CastleDoors implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "open", "close" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("open") && target.equals("doors") && (activeChar.isClanLeader()))
		{
			if (activeChar.getTarget() instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
				Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
				if (door == null || castle == null)
					return false;
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				{
					door.openMe();
				}
			}
			else
				return false;
		}
		else if (command.startsWith("close") && target.equals("doors") && (activeChar.isClanLeader()))
		{
			if (activeChar.getTarget() instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
				Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
				if (door == null || castle == null)
					return false;
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				{
					door.closeMe();
				}
			}
			else
				return false;
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}

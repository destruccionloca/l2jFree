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
package com.l2jfree.gameserver.model.zone;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.GmListTable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class L2JailZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_JAIL, true);
			character.setInsideZone(FLAG_NOSUMMON, true);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(FLAG_PVP, true);
				((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_JAIL, false);
			character.setInsideZone(FLAG_NOSUMMON, false);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(FLAG_PVP, false);
				((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
			}
			if (((L2PcInstance) character).isInJail())
			{// This is for when a player tries to bug his way out of jail
				((L2PcInstance) character).teleToLocation(-114356, -249645, -2984, false); // Jail
				((L2PcInstance) character).sendMessage("You dare try and escape from jail before your time is up? Think again!");
				String msg = "Player: " + ((L2PcInstance) character).getName() + " tried to escape from jail.";
				_log.warn(msg);
				GmListTable.broadcastMessageToGMs(msg);
			}
		}

		super.onExit(character);
	}
}

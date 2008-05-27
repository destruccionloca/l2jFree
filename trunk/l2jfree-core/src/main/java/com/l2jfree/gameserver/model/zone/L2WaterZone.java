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

import com.l2jfree.gameserver.instancemanager.ZoneManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.NpcInfo;

public class L2WaterZone extends L2DefaultZone
{
	@Override
	protected void register()
	{
		// Required for fishing
		ZoneManager.getInstance().getZones(ZoneType.Water).add(this);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && ((L2PcInstance)character).isInBoat())
		{
			return;
		}

		character.setInsideZone(FLAG_WATER, true);

		if (character instanceof L2PcInstance)
		{
			if (((L2PcInstance) character).isMounted())
				((L2PcInstance) character).dismount();

			if (((L2PcInstance) character).isTransformed()
				&& !((L2PcInstance) character).isCursedWeaponEquipped())
			{
				character.stopTransformation(null);
			}
			// TODO: update to only send speed status when that packet is known
			else
				((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2NpcInstance)
		{
			for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
				if (player != null)
					player.sendPacket(new NpcInfo((L2NpcInstance)character, player));
		}

		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_WATER, false);

		// TODO: update to only send speed status when that packet is known
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2NpcInstance)
		{
			for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
				if (player != null)
					player.sendPacket(new NpcInfo((L2NpcInstance)character, player));
		}

		super.onExit(character);
	}
}

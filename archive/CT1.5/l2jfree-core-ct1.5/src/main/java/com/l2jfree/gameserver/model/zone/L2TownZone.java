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
import com.l2jfree.gameserver.instancemanager.TownManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class L2TownZone extends L2DefaultZone
{
	@Override
	protected void register()
	{
		TownManager.getInstance().registerTown(this);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		boolean peace = true;
		if (character instanceof L2PcInstance)
		{
			if (((L2PcInstance)character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
			{
				// PvP allowed for siege participants
				// TODO: PvP zone with debuffs etc. allowed or just general zone?
				character.setInsideZone(FLAG_PVP, true);
				peace = false;
			}
		}

		// PvP in towns all the time
		if (Config.ZONE_TOWN == 2)
		{
			peace = false;

			// TODO: PvP zone with debuffs etc. allowed or just general zone?
			character.setInsideZone(FLAG_PVP, true);
		}

		if (peace == true)
			character.setInsideZone(FLAG_PEACE, true);

		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isInsideZone(FLAG_PVP))
			character.setInsideZone(FLAG_PVP, false);

		if (character.isInsideZone(FLAG_PEACE))
			character.setInsideZone(FLAG_PEACE, false);

		super.onExit(character);
	}
}

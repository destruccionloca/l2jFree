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

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;

public class L2StadiumZone extends L2Zone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_STADIUM, true);
		character.setInsideZone(FLAG_PVP, true);
		character.setInsideZone(FLAG_NOLANDING, true);
		character.setInsideZone(FLAG_NOSUMMON, true);
		
		super.onEnter(character);
		
		if (character instanceof L2Playable)
		{
			final L2PcInstance player = character.getActingPlayer();
			if (player != null)
			{
				// only participants, observers and GMs allowed
				if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode())
				{
					if (character instanceof L2Summon)
						((L2Summon)character).unSummon(player);
					player.teleToLocation(TeleportWhereType.Town);
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_STADIUM, false);
		character.setInsideZone(FLAG_PVP, false);
		character.setInsideZone(FLAG_NOLANDING, false);
		character.setInsideZone(FLAG_NOSUMMON, false);
		
		super.onExit(character);
	}
}

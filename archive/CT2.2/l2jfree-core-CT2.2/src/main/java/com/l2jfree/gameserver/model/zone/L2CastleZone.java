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
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Siege;

public class L2CastleZone extends SiegeableEntityZone
{
	@Override
	protected void register() throws Exception
	{
		_entity = initCastle();
		_entity.registerZone(this);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_CASTLE, true);
		
		super.onEnter(character);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)character;
			L2Clan clan = player.getClan();
			if (clan != null)
			{
				Siege s = getSiege();
				if (s.getIsInProgress() && (s.checkIsAttacker(clan) || s.checkIsDefender(clan)))
				{
					player.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_CASTLE, false);
		
		super.onExit(character);
		
		if (character instanceof L2PcInstance)
			((L2PcInstance)character).stopFameTask();
	}
}

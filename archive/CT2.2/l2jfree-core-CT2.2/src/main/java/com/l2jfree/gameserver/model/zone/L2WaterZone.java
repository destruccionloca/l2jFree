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
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class L2WaterZone extends L2Zone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_WATER, true);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)character;
			if (player.isTransformed() && !player.isCursedWeaponEquipped())
				character.stopTransformation(true);
		}
		
		super.onEnter(character);
		
		character.broadcastFullInfo();
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_WATER, false);
		
		super.onExit(character);
		
		character.broadcastFullInfo();
	}
	
	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (character instanceof L2PcInstance && ((L2PcInstance)character).isInBoat())
			return false;
		
		return super.checkDynamicConditions(character);
	}
}

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

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * When a player is in this zone, the danger effect icon is shown.
 * 
 * @author Savormix
 * @since 2009.04.19
 */
public class L2DangerZone extends L2DynamicZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, true);
		
		super.onEnter(character);
		
		if (character instanceof L2PcInstance)
			((L2PcInstance)character).sendEtcStatusUpdate();
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, false);
		
		super.onExit(character);
		
		if (character instanceof L2PcInstance)
			((L2PcInstance)character).sendEtcStatusUpdate();
	}
}

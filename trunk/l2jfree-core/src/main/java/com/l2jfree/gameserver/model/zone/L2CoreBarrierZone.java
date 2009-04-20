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
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * Taken from L2EP, adapted to current revision
 * @author Savormix
 * @since 2009.04.19
 */
public final class L2CoreBarrierZone extends L2DamageZone {
	@Override
	protected void checkForDamage(L2Character character)
	{
		super.checkForDamage(character);
		if (_hpDamage > 0)
			character.sendPacket(new SystemMessage(SystemMessageId.S1_DAMAGE_BY_CORE_BARRIER).addNumber(_hpDamage));
	}
}

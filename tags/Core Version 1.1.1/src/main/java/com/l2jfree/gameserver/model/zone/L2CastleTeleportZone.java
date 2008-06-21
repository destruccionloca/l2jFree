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

import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Character;

public class L2CastleTeleportZone extends EntityZone
{
	@Override
	protected void register()
	{
		_entity = CastleManager.getInstance().getCastleById(_castleId);
		if (_entity != null)
			_entity.registerTeleportZone(this);
		else
			_log.warn("Invalid castleId: "+_castleId);
	}

	// They just define a teleport area
	@Override
	protected void onEnter(L2Character character){}

	@Override
	protected void onExit(L2Character character){}
}

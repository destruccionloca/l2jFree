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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.entity.Fort;

public class L2FortZone extends EntityZone
{
	@Override
	protected void register()
	{
		_entity = FortManager.getInstance().getFortById(_fortId);
		if (_entity != null)
		{
			// Forts: One zone for multiple purposes (could expand this later and add defender spawn areas)
			_entity.registerZone(this);
			_entity.registerHeadquartersZone(this);
		}
		else
			_log.warn("Invalid fortId: "+_fortId);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		super.onExit(character);
	}
}

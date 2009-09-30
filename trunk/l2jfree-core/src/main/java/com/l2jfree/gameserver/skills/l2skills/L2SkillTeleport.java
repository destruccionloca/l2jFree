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
package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final int[] _teleportCoords;
	
	public L2SkillTeleport(StatsSet set)
	{
		super(set);
		
		_recallType = set.getString("recallType", "");
		
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_teleportCoords = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
				_teleportCoords[i] = Integer.parseInt(valuesSplit[i]);
		}
		else
			_teleportCoords = null;
	}
	
	public final String getRecallType()
	{
		return _recallType;
	}
	
	public final int[] getTeleportCoords()
	{
		return _teleportCoords;
	}
}

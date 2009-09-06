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
package com.l2jfree.gameserver.model;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public final class L2CertificationSkillsLearn
{
	private final int _skill_id;
	private final int _item_id;
	private final int _level;
	private final String _name;

	public L2CertificationSkillsLearn(int skill_id, int item_id, int level, String name)
	{
		_skill_id = skill_id;
		_item_id = item_id;
		_level = level;
		_name = name.intern();
	}

	/**
	 * @return Returns the skill_id.
	 */
	public int getId()
	{
		return _skill_id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	
	public int getItemId()
	{
		return _item_id;
	}
}

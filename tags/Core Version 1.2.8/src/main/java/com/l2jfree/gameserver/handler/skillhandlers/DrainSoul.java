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
package com.l2jfree.gameserver.handler.skillhandlers;

import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/** 
 * @author _drunk_ 
 */
public class DrainSoul implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.DRAIN_SOUL };

	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused")
	L2Object... targets)
	{
		if (_log.isDebugEnabled())
			_log.debug("Soul Crystal casting succeded.");

		// This is just a dummy skill handler for the soul crystal skill,
		// since the Soul Crystal item handler already does everything.
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

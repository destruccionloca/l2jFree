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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.tools.random.Rnd;

/**
 * @author evill33t
 * 
 */
public class SummonTreasureKey implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.SUMMON_TREASURE_KEY };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		int item_id = 0;

		switch (skill.getLevel())
		{
			case 1:
			{
				item_id = Rnd.get(6667, 6669);
				break;
			}
			case 2:
			{
				item_id = Rnd.get(6668, 6670);
				break;
			}
			case 3:
			{
				item_id = Rnd.get(6669, 6671);
				break;
			}
			case 4:
			{
				item_id = Rnd.get(6670, 6672);
				break;
			}
		}
		
		if (item_id != 0)
			player.addItem("Skill", item_id, Rnd.get(2, 3), player, false);
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

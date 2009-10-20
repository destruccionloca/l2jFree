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
import com.l2jfree.gameserver.skills.l2skills.L2SkillAppearance;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public final class ChangeFace implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.CHANGE_APPEARANCE };
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill0, L2Character... targets)
	{
		L2SkillAppearance skill = (L2SkillAppearance)skill0;
		
		for (L2Character target : targets)
		{
			if (!(target instanceof L2PcInstance))
				continue;
			
			L2PcInstance player = (L2PcInstance)target;
			
			if (skill.getFaceId() >= 0)
				player.getAppearance().setFace(skill.getFaceId());
			if (skill.getHairColorId() >= 0)
				player.getAppearance().setHairColor(skill.getHairColorId());
			if (skill.getHairStyleId() >= 0)
				player.getAppearance().setHairStyle(skill.getHairStyleId());
			
			// Update the changed stat for the character in the DB.
			player.store();
			
			// Broadcast the changes to the char and all those nearby.
			player.broadcastFullInfo();
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

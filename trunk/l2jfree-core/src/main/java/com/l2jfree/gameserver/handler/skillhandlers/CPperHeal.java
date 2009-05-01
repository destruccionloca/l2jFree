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
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author -Nemesiss-
 *
 */

public class CPperHeal implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final L2SkillType[]	SKILL_IDS	= { L2SkillType.COMBATPOINTPERCENTHEAL };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(@SuppressWarnings("unused") L2Character actChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF).useSkill(actChar, skill, targets);
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			double perCp = target.getMaxCp() * skill.getPower();
			double newCp = target.getStatus().getCurrentCp() + perCp;
			if (newCp > target.getMaxCp())
				perCp = target.getMaxCp() - target.getStatus().getCurrentCp();
			target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() + perCp);
			StatusUpdate sucp = new StatusUpdate(target.getObjectId());
			sucp.addAttribute(StatusUpdate.CUR_CP, (int) target.getStatus().getCurrentCp());
			target.sendPacket(sucp);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int) perCp);
			target.sendPacket(sm);
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

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

import com.l2jfree.gameserver.RecipeController;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Craft implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.COMMON_CRAFT, L2SkillType.DWARVEN_CRAFT };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused")
	L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING);
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, (skill.getSkillType() == L2SkillType.DWARVEN_CRAFT));
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

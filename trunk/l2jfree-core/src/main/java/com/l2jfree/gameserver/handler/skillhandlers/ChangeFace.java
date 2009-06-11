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
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class ChangeFace implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	= { L2SkillType.CHANGE_APPEARANCE, };

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#useSkill(com.l2jfree.gameserver.model.actor.L2Character,
	 *      com.l2jfree.gameserver.model.L2Skill,
	 *      com.l2jfree.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		if (targets == null)
			return;
		L2PcInstance player = (L2PcInstance) targets[0];
		if (skill.getFaceId() >= 0)
			player.getAppearance().setFace(skill.getFaceId());
		if (skill.getHairColorId() >= 0)
			player.getAppearance().setHairColor(skill.getHairColorId());
		if (skill.getHairStyleId() >= 0)
			player.getAppearance().setHairStyle(skill.getHairStyleId());

		// Update the changed stat for the character in the DB.
		player.store();

		// Broadcast the changes to the char and all those nearby.
		player.broadcastPacket(new UserInfo(player));
		player.broadcastPacket(new ExBrExtraUserInfo(player));
	}

	/**
	 * 
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		// TODO Auto-generated method stub

	}
}
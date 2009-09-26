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
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author Ahmed
 */
public class TransformDispel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.TRANSFORMDISPEL };
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		final L2PcInstance pc = (L2PcInstance)activeChar;
		
		if (pc.isAlikeDead() || pc.isCursedWeaponEquipped())
			return;
		
		if (pc.isTransformed() || pc.isInStance())
		{
			if (pc.isFlyingMounted() && !pc.isInsideZone(L2Zone.FLAG_LANDING))
				pc.sendPacket(SystemMessageId.BOARD_OR_CANCEL_NOT_POSSIBLE_HERE);
			else
				pc.stopTransformation(true);
		}
	}
	
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

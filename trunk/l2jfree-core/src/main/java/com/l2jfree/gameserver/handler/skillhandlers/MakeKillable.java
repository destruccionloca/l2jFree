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
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 *
 * @author evill33t
 */
public class MakeKillable implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.MAKE_KILLABLE };

	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused")
	L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		for (L2Character element : targets)
		{
			if (!(element instanceof L2MonsterInstance))
				continue;
			
			L2MonsterInstance target = (L2MonsterInstance) element;
			
			if (!target.getKillable())
				target.setKillable(true);
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

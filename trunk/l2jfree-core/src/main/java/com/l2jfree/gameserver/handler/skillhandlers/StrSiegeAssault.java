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

import com.l2jfree.gameserver.handler.ISkillConditionChecker;
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.instancemanager.CCHManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author _tomciaaa_
 */
public final class StrSiegeAssault extends ISkillConditionChecker
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.STRSIEGEASSAULT };
	
	@Override
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!checkIfOkToUseStriderSiegeAssault(player, false))
			return false;
		
		return super.checkConditions(activeChar, skill);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance)activeChar;
		
		if (checkIfOkToUseStriderSiegeAssault(player, false))
		{
			SkillHandler.getInstance().useSkill(L2SkillType.PDAM, activeChar, skill, targets);
		}
	}
	
	// FIXME: 1.4.0
	public static boolean checkIfOkToUseStriderSiegeAssault(L2PcInstance player, boolean isCheckOnly)
	{
		return SiegeManager.checkIfOkToUseStriderSiegeAssault(player, isCheckOnly)
				|| FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, isCheckOnly)
				|| CCHManager.checkIfOkToUseStriderSiegeAssault(player, isCheckOnly);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

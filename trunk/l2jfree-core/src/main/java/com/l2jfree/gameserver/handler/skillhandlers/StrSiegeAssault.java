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
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author _tomciaaa_
 * 
 */
public class StrSiegeAssault implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.STRSIEGEASSAULT };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (SiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) || FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, false))
		{
			//TODO: damage calculation below is crap - needs rewrite
			int damage = 0;

			for (L2Character target : targets)
			{
				if (target == null)
					continue;
				
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
				{
					target.stopFakeDeath(true);
				}
				else if (target.isDead())
					continue;

				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Formulas.calcShldUse(activeChar, target);
				boolean crit = Formulas.calcCrit(activeChar, target, activeChar.getCriticalHit(target, skill));
				boolean soul = (weapon != null && weapon.isSoulshotCharged() && weapon.getItemType() != L2WeaponType.DAGGER);

				if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
					damage = 0;
				else
					damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);

				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar, skill);
					if (soul && weapon != null)
						weapon.useSoulshotCharge();
					activeChar.sendDamageMessage(target, damage, false, false, false);
				}
				else
					activeChar.sendMessage(skill.getName() + " failed.");
			}
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

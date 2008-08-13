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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.L2WeaponType;

/**
 * @author _tomciaaa_
 * 
 */
public class StrSiegeAssault implements ISkillHandler
{
	private final static Log			_log		= LogFactory.getLog(StrSiegeAssault.class);

	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.STRSIEGEASSAULT };

	public void useSkill(L2Character activeChar, @SuppressWarnings("unused")
	L2Skill skill, @SuppressWarnings("unused")
	L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (SiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) || FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, false))
		{
			try
			{
				//TODO: damage calculation below is crap - needs rewrite
				int damage = 0;

				for (L2Object element : targets)
				{
					L2Character target = (L2Character) element;
					L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
					if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
					{
						target.stopFakeDeath(null);
					}
					else if (target.isDead())
						continue;

					boolean dual = activeChar.isUsingDualWeapon();
					boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
					boolean crit = Formulas.getInstance().calcCrit(activeChar, target, activeChar.getCriticalHit(target, skill));
					boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER);

					if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
						damage = 0;
					else
						damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);

					if (damage > 0)
					{
						target.reduceCurrentHp(damage, activeChar);
						if (soul && weapon != null)
							weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
						activeChar.sendDamageMessage(target, damage, false, false, false);
					}
					else
						activeChar.sendMessage(skill.getName() + " failed.");
				}
			}
			catch (Exception e)
			{
				_log.error(e);
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

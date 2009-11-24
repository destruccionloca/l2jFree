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
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.l2skills.L2SkillPdam;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public final class Pdam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.PDAM, L2SkillType.FATALCOUNTER, L2SkillType.BLOW,
		L2SkillType.CHARGEDAM };
	
	public void useSkill(L2Character activeChar, L2Skill skill0, L2Character... targets)
	{
		// support for Double Shot, etc
		final L2SkillPdam skill = (L2SkillPdam)skill0;
		final int numberOfHits = skill.getNumberOfHits();
		
		if (activeChar.isAlikeDead())
			return;
		
		final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
		final boolean soul = (weapon != null && weapon.isSoulshotCharged() &&
				weapon.getItemType() != L2WeaponType.DAGGER);
		final boolean isBlow = (skill.getSkillType() == L2SkillType.BLOW);
		final boolean isCharge = (skill.getSkillType() == L2SkillType.CHARGEDAM);
		
		final double modifier;
		
		if (activeChar instanceof L2PcInstance && isCharge)
		{
			final L2PcInstance player = (L2PcInstance)activeChar;
			
			modifier = 0.8 + 0.201 * player.getCharges(); // thanks Diego Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF
			
			if (skill.getTargetType() != SkillTargetType.TARGET_AREA && skill.getTargetType() != SkillTargetType.TARGET_MULTIFACE)
				player.decreaseCharges(skill.getNeededCharges());
		}
		else
			modifier = 1.0;
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			// TODO: should be moved to skill target list generation
			if (GlobalRestrictions.isProtected(activeChar, target, skill, true))
				continue;
			
			if (target.isAlikeDead())
			{
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
					target.stopFakeDeath(true);
				else
					continue;
			}
			
			// support for Double Shot, etc
			for (int i = 0; i < numberOfHits; i++)
			{
				if (isBlow && !Formulas.calcBlow(activeChar, target, skill))
				{
					activeChar.sendMissedDamageMessage(target);
					continue;
				}
				
				// Check firstly if target dodges skill
				if (Formulas.calcPhysicalSkillEvasion(target, skill))
				{
					activeChar.sendMissedDamageMessage(target);
					continue;
				}
				
				final byte shld = Formulas.calcShldUse(activeChar, target, skill);
				double damage = Formulas.calcPhysDam(activeChar, target, skill, shld, isBlow, soul);
				
				// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
				final boolean crit = Formulas.calcSkillCrit(activeChar, target, skill);
				if (crit)
				{
					damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
					
					if (isBlow)
					{
						// Vicious Stance is special after C5, and only for BLOW skills
						// Adds directly to damage
						final L2Effect vicious = activeChar.getFirstEffect(312);
						if (vicious != null)
						{
							for (Func func : vicious.getStatFuncs())
							{
								Env env = new Env();
								env.player = activeChar;
								env.target = target;
								env.skill = skill;
								env.value = damage;
								func.calcIfAllowed(env);
								damage = (int)env.value;
							}
						}
					}
				}
				
				if (isCharge)
				{
					damage *= modifier;
				}
				
				// support for Double Shot, etc
				damage /= numberOfHits;
				
				final byte reflect = Formulas.calcSkillReflect(target, skill);
				
				skill.dealDamage(activeChar, target, skill, damage, reflect, false, crit || isBlow);
				skill.getEffects(activeChar, target, reflect, shld, false, false, true);
			}
		}
		
		if (soul && weapon != null)
			weapon.useSoulshotCharge();
	}
	
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

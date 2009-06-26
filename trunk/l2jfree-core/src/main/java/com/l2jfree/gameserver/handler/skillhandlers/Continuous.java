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

import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.ICubicSkillHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ICubicSkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{
			L2SkillType.BUFF,
			L2SkillType.DEBUFF,
			L2SkillType.DOT,
			L2SkillType.MDOT,
			L2SkillType.POISON,
			L2SkillType.BLEED,
			L2SkillType.HOT,
			L2SkillType.CPHOT,
			L2SkillType.MPHOT,
			L2SkillType.FEAR,
			L2SkillType.CONT,
			L2SkillType.WEAKNESS,
			L2SkillType.REFLECT,
			L2SkillType.AGGDEBUFF,
			L2SkillType.FUSION			};

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		boolean acted = true;

		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;

		if (skill.getEffectId() != 0)
		{
			int skillLevel = (int)skill.getEffectLvl();
			int skillEffectId = skill.getEffectId();
			
			L2Skill skill2;
			if (skillLevel == 0)
			{
				skill2 = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				skill2 = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if (skill2 != null)
				skill = skill2;
		}

		for (L2Character target : targets)
		{
			if (target == null)
				continue;

			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
				target = activeChar;

			// With Mystic Immunity you can't be buffed/debuffed
			if (target.isPreventedFromReceivingBuffs())
				continue;

			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
						continue;
					else if (player != null && player.isCursedWeaponEquipped())
						continue;
				}
			}

			if (skill.isOffensive() || skill.isDebuff())
			{
				boolean ss = false;
				boolean sps = false;
				boolean bss = false;
				
				if (skill.useSpiritShot())
				{
					if (activeChar.isBlessedSpiritshotCharged())
					{
						bss = true;
						activeChar.useBlessedSpiritshotCharge();
					}
					else if (activeChar.isSpiritshotCharged())
					{
						sps = true;
						activeChar.useSpiritshotCharge();
					}
				}
				else if (activeChar.isSoulshotCharged())
				{
					ss = true;
					activeChar.useSoulshotCharge();
				}
				
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
			}

			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect e = target.getEffects().getFirstEffect(skill);
					
					if (e != null)
					{
						e.exit();
						return;
					}
				}

				skill.getEffects(activeChar, target);

				if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
				{
					if (target instanceof L2Attackable)
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					else if (target instanceof L2Playable)
					{
						if (target.getTarget() == activeChar)
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						else
							target.setTarget(activeChar);
					}
				}
			}
			else if (activeChar instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addString(target.getName());
				sm.addSkillName(skill.getId());
				((L2PcInstance)activeChar).sendPacket(sm);
			}
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}

		// Increase Charges
		if (activeChar instanceof L2PcInstance && skill.getGiveCharges() > 0)
		{
			((L2PcInstance)activeChar).increaseCharges(skill.getGiveCharges(), skill.getMaxCharges());
		}

		// Self Effect :]
		skill.getEffectsSelf(activeChar);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (skill.isOffensive())
			{
				byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
					continue;
				}
			}
			
			skill.getEffects(activeCubic, target);
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

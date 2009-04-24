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

import com.l2jfree.gameserver.handler.ICubicSkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Mdam implements ICubicSkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.MDAM, L2SkillType.DEATHLINK };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;
		
		if (activeChar.isBlessedSpiritshotCharged())
		{
			bss = true;
			activeChar.useBlessedSpiritshotCharge();
		}
		else if (activeChar.isSpiritshotCharged())
		{
			ss = true;
			activeChar.useSpiritshotCharge();
		}
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
			{
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			byte shld = Formulas.calcShldUse(activeChar, target);
			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, shld, ss, bss, mcrit);

			if (skill.getMaxSoulConsumeCount() > 0 && activeChar instanceof L2PcInstance)
			{
				switch (((L2PcInstance) activeChar).getLastSoulConsume())
				{
					case 0:
						break;
					case 1:
						damage *= 1.10;
						break;
					case 2:
						damage *= 1.12;
						break;
					case 3:
						damage *= 1.15;
						break;
					case 4:
						damage *= 1.18;
						break;
					default:
						damage *= 1.20;
						break;
				}
			}

			if (mcrit)
				activeChar.sendPacket(SystemMessageId.CRITICAL_HIT);

			if (damage < 1)
				damage = 1;

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (activeChar instanceof L2SummonInstance)
					((L2SummonInstance) activeChar).getOwner().sendPacket(new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1).addNumber(damage));

				// Activate attacked effects, if any
				if (skill.getId() == 4139 && activeChar instanceof L2Summon) //big boom unsummon-destroy
				{
					L2PcInstance Owner = null;
					Owner = ((L2Summon) activeChar).getOwner();
					L2Summon Pet = null;
					Pet = Owner.getPet();
					if (Pet != null)
						Pet.unSummon(Owner);
				}
				if (skill.hasEffects())
				{
					if (target.reflectSkill(skill))
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(skill.getId());
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, false, ss, bss))
						{
							skill.getEffects(activeChar, target);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
				}
				target.reduceCurrentHp(damage, activeChar, skill);
				if (damage > 5000 && activeChar instanceof L2PcInstance)
				{
					String name = "";
					if (target instanceof L2RaidBossInstance)
						name = "RaidBoss ";
					if (target instanceof L2NpcInstance)
						name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().getNpcId() + ")";
					if (target instanceof L2PcInstance)
						name = target.getName() + "(" + target.getObjectId() + ") ";
					name += target.getLevel() + " lvl";
					if (_log.isDebugEnabled())
						_log.info(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage
								+ " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name);
				}
			}
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		// Self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);

		if (skill.isSuicideAttack())
		{
			L2Character target = null;
			
			for (L2Character tmp : targets)
			{
				if (tmp != null && !(tmp instanceof L2PlayableInstance))
				{
					target = tmp;
					break;
				}
			}
			
			activeChar.doDie(target);
		}
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
				target.stopFakeDeath(true);
			else if (target.isAlikeDead())
				continue;

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target);
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);

			// If target is reflecting the skill then no damage is done
			if (target.reflectSkill(skill))
				damage = 0;

			if (_log.isDebugEnabled())
				_log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						skill.getEffects(activeCubic, target);
					}
				}

				target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
			}
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

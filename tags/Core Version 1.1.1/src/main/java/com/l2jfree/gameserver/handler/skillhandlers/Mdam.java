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
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Mdam implements ISkillHandler
{
	private final static Log			_log		= LogFactory.getLog(Mdam.class);

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.MDAM, SkillType.DEATHLINK };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		/*if (activeChar instanceof L2PcInstance)
		{
		    if (weaponInst == null)
		    {
		        SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S2);
		        sm2.addString("You must equip a weapon before casting a spell.");
		        activeChar.sendPacket(sm2);
		        return;
		    }
		}*/
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}

		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;

			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2NpcInstance)
		{
			bss = ((L2NpcInstance) activeChar).isUsingShot(false);
			ss = ((L2NpcInstance) activeChar).isUsingShot(true);
		}

		for (L2Object element : targets)
		{
			L2Character target = (L2Character) element;

			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isDead())
			{
				continue;
			}

			boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, skill));
			int damage = (int) Formulas.getInstance().calcMagicDam(activeChar, target, skill, ss, bss, mcrit);

			if (skill.isCritical() && !mcrit)
				damage = 0;
			else if (mcrit)
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));

			if (damage < 1)
				damage = 1;

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

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (activeChar instanceof L2SummonInstance)
					((L2SummonInstance) activeChar).getOwner().sendPacket(new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1).addNumber(damage));
				//if (activeChar instanceof L2PetInstance)
				//    ((L2PetInstance)activeChar).getOwner().sendPacket(new SystemMessage(SystemMessageId.PET_GAVE_DAMAGE_OF_S1).addNumber(damage));

				// activate attacked effects, if any
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
						skill.getEffects((L2Character) null, activeChar);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(skill.getId());
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
							skill.getEffects(activeChar, target);
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
				}
				target.reduceCurrentHp(damage, activeChar);
			}
			// Possibility of a lethal strike
			Formulas.getInstance().calcLethalHit(activeChar, target, skill);
		}
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);

		if (skill.isSuicideAttack())
			activeChar.doDie(null);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (int index = 0; index < targets.length; index++)
		{
			L2Character target = (L2Character) targets[index];

			if (target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
				target.stopFakeDeath(null);
			else if (target.isAlikeDead())
				continue;

			boolean mcrit = Formulas.getInstance().calcMCrit(activeCubic.getMCriticalHit(target, skill));
			int damage = (int) Formulas.getInstance().calcMagicDam(activeCubic, target, skill, mcrit);

			// if target is reflecting the skill then no damage is done
			if (target.reflectSkill(skill))
				damage = 0;

			if (_log.isDebugEnabled())
				_log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if (target.getFirstEffect(skill) != null)
						target.removeEffect(target.getFirstEffect(skill));
					if (Formulas.getInstance().calcCubicSkillSuccess(activeCubic, target, skill))
						skill.getEffects(activeCubic, target);
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeCubic.getOwner().sendPacket(sm);
					}
				}

				target.reduceCurrentHp(damage, activeCubic.getOwner());
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
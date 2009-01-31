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

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.Util;

/**
 *
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.BLOW };

	public final static byte			FRONT		= 50;
	public final static byte			SIDE		= 60;
	public final static byte			BEHIND		= 70;

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		for (L2Object element : targets)
		{
			if (!(element instanceof L2Character))
				continue;

			L2Character target = (L2Character) element;

			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance)
			{
				if (((L2PcInstance) activeChar).getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					((L2PcInstance) activeChar).sendMessage("You are unable to attack players until level "
							+ String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
					continue;
				}
				else if (((L2PcInstance) target).getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					((L2PcInstance) target).sendMessage("Player's level is below " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL)
							+ ", so he cannot be attacked.");
					continue;
				}
			}

			if (target.isAlikeDead())
				continue;

			// Check firstly if target dodges skill
			boolean skillIsEvaded = Formulas.getInstance().calcPhysicalSkillEvasion(target, skill);

			byte _successChance = SIDE;

			if (activeChar.isBehindTarget())
				_successChance = BEHIND;
			else if (activeChar.isInFrontOfTarget())
				_successChance = FRONT;

			// If skill requires Crit or skill requires behind, 
			// Calculate chance based on DEX, Position and on self BUFF
			boolean success = true;
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
				success = (_successChance == BEHIND);
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				success = (success && Formulas.getInstance().calcBlow(activeChar, target, _successChance));
			if (!skillIsEvaded && success)
			{
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
				}
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER);
				byte shld = Formulas.getInstance().calcShldUse(activeChar, target);

				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = false;
				if (Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
					crit = true;
				double damage = (int) Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
				if (crit)
				{
					damage *= 2;
					// Vicious Stance is special after C5, and only for BLOW skills
					// Adds directly to damage
					L2Effect vicious = activeChar.getFirstEffect(312);
					if (vicious != null && damage > 1)
					{
						for (Func func : vicious.getStatFuncs())
						{
							Env env = new Env();
							env.player = activeChar;
							env.target = target;
							env.skill = skill;
							env.value = damage;
							func.calc(env);
							damage = (int) env.value;
						}
					}
				}

				if (soul && weapon != null)
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					if (!player.isInvul() && !player.isPetrified())
					{
						// Check and calculate transfered damage
						L2Summon summon = player.getPet();
						if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
						{
							int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

							// Only transfer dmg up to current HP, it should not be killed
							if (summon.getStatus().getCurrentHp() < tDmg)
								tDmg = (int) summon.getStatus().getCurrentHp() - 1;
							if (tDmg > 0)
							{
								summon.reduceCurrentHp(tDmg, activeChar);
								damage -= tDmg;
							}
						}

						if (damage >= player.getStatus().getCurrentHp())
						{
							if (player.isInDuel())
								player.getStatus().setCurrentHp(1);
							else
							{
								player.getStatus().setCurrentHp(0);
								if (player.isInOlympiadMode())
								{
									player.abortAttack();
									player.abortCast();
									player.getStatus().stopHpMpRegeneration();
									player.setIsDead(true);
									player.setIsPendingRevive(true);
								}
								else
									player.doDie(activeChar);
							}
						}
						else
						{
							player.getStatus().setCurrentHp(player.getStatus().getCurrentHp() - damage);
							// Add Olympiad damage
							if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isInOlympiadMode())
								((L2PcInstance) activeChar).addOlyDamage((int) damage);
							else if (activeChar instanceof L2Summon && ((L2Summon) activeChar).getOwner().isInOlympiadMode()
									&& Config.ALT_OLY_SUMMON_DAMAGE_COUNTS)
								((L2Summon) activeChar).getOwner().addOlyDamage((int) damage);
						}
					}
					SystemMessage smsg = new SystemMessage(SystemMessageId.S1_RECEIVED_DAMAGE_OF_S3_FROM_S2);
					smsg.addPcName(player);
					smsg.addCharName(activeChar);
					smsg.addNumber((int) damage);
					player.sendPacket(smsg);
				}
				else
					target.reduceCurrentHp(damage, activeChar);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT);
					if (target instanceof L2PcInstance)
						activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_HAD_CRITICAL_HIT).addPcName((L2PcInstance) activeChar));
				}

				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
				sm.addNumber((int) damage);
				activeChar.sendPacket(sm);
			}

			// Sending system messages
			if (skillIsEvaded)
			{
				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DODGES_ATTACK);
					sm.addCharName(target);
					((L2PcInstance) activeChar).sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
					sm.addCharName(activeChar);
					((L2PcInstance) target).sendPacket(sm);
				}
			}

			// Possibility of a lethal strike
			Formulas.getInstance().calcLethalHit(activeChar, target, skill);

			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			// Self Effect
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			skill.getEffectsSelf(activeChar);
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

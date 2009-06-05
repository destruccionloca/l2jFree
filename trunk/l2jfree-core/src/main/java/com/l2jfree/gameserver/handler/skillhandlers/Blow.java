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
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
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

	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		for (L2Character target : targets)
		{
			if (target == null)
				continue;

			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance)
			{
				if (activeChar.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					activeChar.sendMessage("You are unable to attack players until level "
							+ String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
					continue;
				}
				else if (target.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					target.sendMessage("Player's level is below " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL)
							+ ", so he cannot be attacked.");
					continue;
				}
			}

			if (target.isAlikeDead())
				continue;

			// Check firstly if target dodges skill
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);

			// If skill requires Crit or skill requires behind,
			// Calculate chance based on DEX, Position and on self BUFF
			boolean success = Formulas.calcBlow(activeChar, target, skill);
			
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
				boolean soul = (weapon != null && weapon.isSoulshotCharged() && weapon.getItemType() == L2WeaponType.DAGGER);
				byte shld = Formulas.calcShldUse(activeChar, target, skill);

				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = Formulas.calcSkillCrit(activeChar, target, skill);
				
				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, soul);
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
							func.calcIfAllowed(env);
							damage = (int) env.value;
						}
					}
				}

				if (soul && weapon != null)
					weapon.useSoulshotCharge();

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
								summon.reduceCurrentHp(tDmg, activeChar, skill);
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
									
									if (player.getPet() != null)
										player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
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
					SystemMessage smsg = new SystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_S2);
					smsg.addPcName(player);
					smsg.addCharName(activeChar);
					smsg.addNumber((int) damage);
					player.sendPacket(smsg);
				}
				else
					target.reduceCurrentHp(damage, activeChar, skill);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				if (activeChar instanceof L2PcInstance)
				{
					L2PcInstance activePlayer = (L2PcInstance) activeChar;
					
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT);
					if (target instanceof L2PcInstance)
						activeChar.sendPacket(new SystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT).addPcName((L2PcInstance) activeChar));
				
					if (activePlayer.isInOlympiadMode() &&
			        		target instanceof L2PcInstance &&
			        		((L2PcInstance)target).isInOlympiadMode() &&
			        		((L2PcInstance)target).getOlympiadGameId() == activePlayer.getOlympiadGameId())
			        {
			        	Olympiad.getInstance().notifyCompetitorDamage(activePlayer, (int) damage, activePlayer.getOlympiadGameId());
			        }
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
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addCharName(target);
					activeChar.sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_C1_ATTACK);
					sm.addCharName(activeChar);
					target.sendPacket(sm);
				}
			}

			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);

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

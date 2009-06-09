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
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.PDAM, L2SkillType.FATALCOUNTER };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		if (_log.isDebugEnabled())
			_log.info("Begin Skill processing in Pdam.java " + skill.getSkillType());

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

			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
				continue;

			boolean dual = activeChar.isUsingDualWeapon();
			byte shld = Formulas.calcShldUse(activeChar, target, skill);
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = Formulas.calcSkillCrit(activeChar, target, skill);
			
			boolean soul = (weapon != null && weapon.isSoulshotCharged() && weapon.getItemType() != L2WeaponType.DAGGER);

			if (skill.ignoreShld())
				shld = 0;

			int damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);

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
			if (crit)
				damage *= 2; // PDAM Critical damage always 2x and not affected by buffs

			if (soul && weapon != null)
				weapon.useSoulshotCharge();

			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			if (!skillIsEvaded)
			{
				if (damage > 0)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);

					if (skill.hasEffects())
					{
						if (target.reflectSkill(skill))
						{
							skill.getEffects(target, activeChar);
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
						else
						{
							// Activate attacked effects, if any
							if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, false, false, false))
							{
								skill.getEffects(activeChar, target);
									
								SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
								sm.addSkillName(skill);
								target.sendPacket(sm);
							}
							else
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
								sm.addCharName(target);
								sm.addSkillName(skill);
								activeChar.sendPacket(sm);
							}
						}

						if (damage > 5000 && activeChar instanceof L2PcInstance)
						{
							String name = "";
							if (target instanceof L2RaidBossInstance)
								name = "RaidBoss ";
							if (target instanceof L2Npc)
								name += target.getName() + "(" + ((L2Npc)target).getTemplate().getNpcId() + ")";
							if (target instanceof L2PcInstance)
								name = target.getName() + "(" + target.getObjectId() + ") ";
							name += target.getLevel() + " lvl";
							if (_log.isDebugEnabled())
								_log.info(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name);
						}
					}

					// Possibility of a lethal strike
					boolean lethal = Formulas.calcLethalHit(activeChar, target, skill);

					// Make damage directly to HP
					if (!lethal && skill.getDmgDirectlyToHP())
					{
						if (target instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) target;
							if (!player.isInvul())
							{
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
									player.getStatus().setCurrentHp(player.getStatus().getCurrentHp() - damage);
							}

							SystemMessage smsg = new SystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_S2);
							smsg.addPcName(player);
							smsg.addCharName(activeChar);
							smsg.addNumber(damage);
							player.sendPacket(smsg);
								
						}
						else
							target.reduceCurrentHp(damage, activeChar, skill);
					}
					else
					{
						target.reduceCurrentHp(damage, activeChar, skill);
					}
				}
				else
				// No damage
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
				}
			}
			else
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

				// Possibility of a lethal strike despite skill is evaded
				Formulas.calcLethalHit(activeChar, target, skill);
			}

			// Increase Charges
			if (activeChar instanceof L2PcInstance && skill.getGiveCharges() > 0)
			{
				((L2PcInstance)activeChar).increaseCharges(skill.getGiveCharges(), skill.getMaxCharges());
			}

			if (activeChar instanceof L2PcInstance)
			{
				L2Skill soulmastery = SkillTable.getInstance().getInfo(467, ((L2PcInstance) activeChar).getSkillLevel(467));
				if (soulmastery != null)
				{
					if (((L2PcInstance) activeChar).getSouls() < soulmastery.getNumSouls())
					{
						int count = 0;
						if (((L2PcInstance) activeChar).getSouls() + skill.getNumSouls() <= soulmastery.getNumSouls())
							count = skill.getNumSouls();
						else
							count = soulmastery.getNumSouls() - ((L2PcInstance) activeChar).getSouls();
						((L2PcInstance) activeChar).increaseSouls(count);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
						((L2PcInstance) activeChar).sendPacket(sm);
						return;
					}
				}
			}

			// Self Effect :]
			skill.getEffectsSelf(activeChar);
		}

		if (skill.isSuicideAttack())
		{
			L2Character target = null;
			
			for (L2Character tmp : targets)
			{
				if (tmp != null && !(tmp instanceof L2Playable))
				{
					target = tmp;
					break;
				}
			}
			
			activeChar.doDie(target);
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

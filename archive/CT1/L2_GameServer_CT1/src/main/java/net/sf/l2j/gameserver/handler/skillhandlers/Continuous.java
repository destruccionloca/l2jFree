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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Continuous.class.getName());
	
	private static final SkillType[] SKILL_IDS =
	{
		L2Skill.SkillType.BUFF,
		L2Skill.SkillType.DEBUFF,
		L2Skill.SkillType.DOT,
		L2Skill.SkillType.MDOT,
		L2Skill.SkillType.POISON,
		L2Skill.SkillType.BLEED,
		L2Skill.SkillType.HOT,
		L2Skill.SkillType.CPHOT,
		L2Skill.SkillType.MPHOT,
		L2Skill.SkillType.FEAR,
		L2Skill.SkillType.CONT,
		L2Skill.SkillType.WEAKNESS,
		L2Skill.SkillType.REFLECT,
		L2Skill.SkillType.UNDEAD_DEFENSE,
		L2Skill.SkillType.AGGDEBUFF,
		L2Skill.SkillType.FORCE_BUFF
	};
	private L2Skill _skill;
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;
		boolean acted = true;
		
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance)activeChar;

		if (skill.getEffectId() != 0)
		{
			int skillLevel = (int)skill.getEffectLvl();
			int skillEffectId = skill.getEffectId();

			if (skillLevel == 0)
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if (_skill != null)
				skill = _skill;
		}

		for (L2Object element : targets)
		{
			target = (L2Character)element;
			//check if skill is allowed on other.properties for raidbosses
			if(!target.checkSkillCanAffectMyself(skill))
				continue;

			switch(skill.getSkillType())
			{
				case BUFF: case HOT: case CPHOT: case MPHOT:
				case AGGDEBUFF: case CONT: case UNDEAD_DEFENSE:
					// No reflect possible
					break;
				default:
					if(target.reflectSkill(skill))
						target = activeChar;
					break;
			}

			// Walls and Door should not be buffed
			if(target instanceof L2DoorInstance && (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.HOT))
				continue;

			// With Mystic Immunity you can't be buffed/debuffed
			if (target.isPreventedFromReceivingBuffs())
				continue;

			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2Skill.SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target instanceof L2PcInstance && ((L2PcInstance)target).isCursedWeaponEquipped())
						continue;
					else if (player != null && player.isCursedWeaponEquipped())
						continue;
				}
			}

			if (skill.isOffensive())
			{
				boolean ss = false;
				boolean sps = false;
				boolean bss = false;
				if (player != null)
				{
					L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
					if (weaponInst != null)
					{
						if (skill.isMagic())
						{
							if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
							{
								bss = true;
								if (skill.getId() != 1020) // vitalize
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
							}
							else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
							{
								sps = true;
								if (skill.getId() != 1020) // vitalize
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
							}
						}
						else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
						{
							ss = true;
							if (skill.getId() != 1020) // vitalize
								weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
						}
					}
				}
				else if (activeChar instanceof L2Summon)
				{
					L2Summon activeSummon = (L2Summon) activeChar;

					if (skill.isMagic())
					{
						if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							bss = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
						else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							sps = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
					}
					else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
					{
						ss = true;
						activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (activeChar instanceof L2NpcInstance)
				{
					bss = ((L2NpcInstance)activeChar).isUsingShot(false);
					ss = ((L2NpcInstance)activeChar).isUsingShot(true);
				}

				acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
			}

			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect[] effects = target.getAllEffects();
					if (effects != null)
					{
						for (L2Effect e : effects)
						{
							if (e != null)
							{
								if (e.getSkill().getId() == skill.getId())
								{
									e.exit();
									return;
								}
							}
						}
					}
				}

				// if this is a debuff let the duel manager know about it
				// so the debuff can be removed after the duel
				// (player & target must be in the same duel)
				if (target instanceof L2PcInstance && ((L2PcInstance)target).isInDuel()
						&& (skill.getSkillType() == L2Skill.SkillType.DEBUFF ||
						skill.getSkillType() == L2Skill.SkillType.BUFF) && player != null
						&& player.getDuelId() == ((L2PcInstance)target).getDuelId())
				{
					DuelManager dm = DuelManager.getInstance();
					for (L2Effect buff : skill.getEffects(activeChar, target))
						if (buff != null) dm.onBuff(((L2PcInstance)target), buff);
				}
				else
					skill.getEffects(activeChar, target);

				if (skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF)
				{
					if (target instanceof L2Attackable)
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int)skill.getPower());
					else if (target instanceof L2PlayableInstance)
					{
						if (target.getTarget() == activeChar)
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK,activeChar);
						else
							target.setTarget(activeChar);
					}
				}
			}
			else if(activeChar instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
				sm.addString(target.getName());
				sm.addSkillName(skill.getId());
				activeChar.sendPacket(sm);
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
	}
	
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

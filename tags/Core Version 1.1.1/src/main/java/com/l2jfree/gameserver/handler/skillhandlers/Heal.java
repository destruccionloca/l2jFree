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
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Stats;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{
	// all the items ids that this handler knowns

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.HEAL, SkillType.HEAL_PERCENT, SkillType.HEAL_STATIC };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		try
		{
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			if (handler != null)
				handler.useSkill(activeChar, skill, targets);
		}
		catch (Exception e)
		{
		}

		L2Character target = null;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;

		for (L2Object element : targets)
		{
			target = (L2Character) element;
			//We should not heal if char is dead
			if (target == null || target.isDead())
				continue;

			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					continue;
				else if (player != null && player.isCursedWeaponEquipped())
					continue;
			}

			double hp = skill.getPower();

			if (skill.getSkillType() == SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else
			{
				//Added effect of SpS and Bsps
				if (weaponInst != null)
				{
					if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						hp *= 1.5;
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
					else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					{
						hp *= 1.3;
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
				}

				else if (activeChar instanceof L2Summon)
				{
					L2Summon activeSummon = (L2Summon) activeChar;

					if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						hp *= 1.5;
						activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					}
					else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					{
						hp *= 1.3;
						activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (activeChar instanceof L2NpcInstance)
				{
					if (((L2NpcInstance) activeChar).isUsingShot(false))
						hp *= 1.5;
				}
			}

			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			{
				hp = 0;
			}
			else
			{
				if (skill.getSkillType() == SkillType.HEAL_STATIC)
				{
					hp = skill.getPower();
				}
				else if (skill.getSkillType() != SkillType.HEAL_PERCENT)
				{
					hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
					// Healer proficiency (since CT1)
					hp *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
				}
			}

			//target.getStatus().setCurrentHp(hp + target.getStatus().getCurrentHp());
			target.getStatus().increaseHp(hp);
			target.setLastHealAmount((int) hp);
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
			target.sendPacket(su);
			if (target instanceof L2PcInstance)
			{
				if (skill.getId() == 4051)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.REJUVENATING_HP);
					target.sendPacket(sm);
				}
				else
				{
					if (activeChar instanceof L2PcInstance && activeChar != target)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
				}
			}
		}

	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

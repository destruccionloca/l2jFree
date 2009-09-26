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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.HEAL, L2SkillType.HEAL_PERCENT, L2SkillType.HEAL_STATIC, L2SkillType.HEAL_MOB };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IItemHandler#useItem(com.l2jfree.gameserver.model.L2PcInstance, com.l2jfree.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().useSkill(L2SkillType.BUFF, activeChar, skill, targets);

		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
						
			// We should not heal if char is dead
			if (target.isDead())
				continue;

			if (target.isInsideZone(L2Zone.FLAG_NOHEAL))
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

			if (skill.getSkillType() == L2SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else if (!skill.isPotion())
			{
				if (activeChar.isBlessedSpiritshotCharged())
				{
					hp *= 1.5;
					activeChar.useBlessedSpiritshotCharge();
				}
				else if (activeChar.isSpiritshotCharged())
				{
					hp *= 1.3;
					activeChar.useSpiritshotCharge();
				}
			}
			
			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			{
				hp = 0;
			}
			else
			{
				if (skill.getSkillType() == L2SkillType.HEAL_STATIC)
				{
					hp = skill.getPower();
				}
				else if (skill.getSkillType() != L2SkillType.HEAL_PERCENT)
				{
					hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
					// Healer proficiency (since CT1)
					hp *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
					// Extra bonus (since CT1.5)
					hp += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
				}
			}
			
			// Heal critic, since CT2.3 Gracia Final
			if (skill.getSkillType() == L2SkillType.HEAL && !skill.isPotion()
					&& Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
			{
				hp *= 3;
				activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
			}

			// From CT2 you will receive exact HP, you can't go over it, if you have full HP and you get HP buff, you will receive 0HP restored message
			if ((target.getStatus().getCurrentHp() + hp) >= target.getMaxHp())
				hp = target.getMaxHp() - target.getStatus().getCurrentHp();
	
			if (hp > 0)
			{
				target.getStatus().increaseHp(hp);
			}

			if (target instanceof L2PcInstance)
			{
				if (skill.getId() == 4051)
				{
					target.sendPacket(SystemMessageId.REJUVENATING_HP);
				}
				else
				{
					if (activeChar instanceof L2PcInstance && activeChar != target)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						target.getActingPlayer().sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						target.getActingPlayer().sendPacket(sm);
					}
				}
			}
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

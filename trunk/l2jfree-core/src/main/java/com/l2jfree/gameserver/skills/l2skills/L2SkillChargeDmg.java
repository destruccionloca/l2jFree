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
package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.effects.EffectCharge;
import com.l2jfree.gameserver.templates.L2WeaponType;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
	final int	chargeSkillId;

	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
		chargeSkillId = set.getInteger("charge_skill_id");
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;
			EffectCharge e = (EffectCharge) player.getFirstEffect(chargeSkillId);
			if (e == null || e.numCharges < getNumCharges())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(this);
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(activeChar, target, itemOrWeapon);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		// get the effect
		EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(chargeSkillId);
		if (effect == null || effect.numCharges < getNumCharges())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(this);
			activeChar.sendPacket(sm);
			return;
		}
		double modifier = 0.8 + 0.201 * effect.numCharges; // thanks Diego Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF

		if (getTargetType() != SkillTargetType.TARGET_AREA && getTargetType() != SkillTargetType.TARGET_MULTIFACE)
			effect.numCharges -= getNumCharges();
		if (activeChar instanceof L2PcInstance)
			activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
		if (effect.numCharges == 0)
			effect.exit();
		for (L2Object element : targets)
		{
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			L2Character target = (L2Character) element;
			if (target.isAlikeDead())
				continue;

			boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
			boolean crit = false;
			if (getBaseCritRate() > 0)
				crit = Formulas.getInstance().calcCrit(getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar));

			boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER);

			// damage calculation, crit is static 2x
			int damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, this, shld, false, false, soul);
			if (crit)
				damage *= 2;

			if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance activeCaster = (L2PcInstance) activeChar;

				if (activeCaster.isGM() && activeCaster.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					damage = 0;
			}

			if (damage > 0)
			{
				double finalDamage = damage * modifier;
				target.reduceCurrentHp(finalDamage, activeChar);

				activeChar.sendDamageMessage(target, (int) finalDamage, false, crit, false);

				if (soul && weapon != null)
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				activeChar.sendDamageMessage(target, 0, false, false, true);
			}
		} // effect self :]
		L2Effect seffect = activeChar.getFirstEffect(getId());
		if (seffect != null && seffect.isSelfEffect())
		{
			//Replace old effect with new one.
			seffect.exit();
		}
		// cast self effect if any
		getEffectsSelf(activeChar);
	}
}

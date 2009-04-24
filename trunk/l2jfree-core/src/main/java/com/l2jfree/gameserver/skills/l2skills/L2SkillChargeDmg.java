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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.item.L2WeaponType;

public class L2SkillChargeDmg extends L2Skill
{
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead() || !(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		double modifier = 0.8 + 0.201 * player.getCharges(); // thanks Diego Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF
		player.decreaseCharges(getNeededCharges());

		for (L2Character target : targets)
		{
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			if (target.isAlikeDead())
				continue;

			byte shld = Formulas.calcShldUse(activeChar, target);
			boolean crit = false;
			if (getBaseCritRate() > 0)
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar));

			boolean soul = (weapon != null && weapon.isSoulshotCharged() && weapon.getItemType() != L2WeaponType.DAGGER);

			// damage calculation, crit is static 2x
			int damage = (int) Formulas.calcPhysDam(activeChar, target, this, shld, false, false, soul);
			if (crit)
				damage *= 2;

			if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance activeCaster = (L2PcInstance) activeChar;

				if (activeCaster.isGM() && activeCaster.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					damage = 0;
			}

			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if (skillIsEvaded)
			{
				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DODGES_ATTACK);
					sm.addCharName(target);
					activeChar.sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
					sm.addCharName(activeChar);
					target.sendPacket(sm);
				}
			}
			else if (damage > 0)
			{
				double finalDamage = damage * modifier;
				target.reduceCurrentHp(finalDamage, activeChar, this);

				activeChar.sendDamageMessage(target, (int) finalDamage, false, crit, false);

				if (soul && weapon != null)
					weapon.useSoulshotCharge();
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

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

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.effects.EffectCharge;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill
{
	final int	numCharges;

	public L2SkillCharge(StatsSet set)
	{
		super(set);
		numCharges = set.getInteger("num_charges", getLevel());
	}

	@Override
	public void useSkill(L2Character caster, @SuppressWarnings("unused")
	L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;

		// get the effect
		EffectCharge effect = (EffectCharge) caster.getFirstEffect(L2Effect.EffectType.CHARGE);
		if (effect != null)
		{
			if (effect.numCharges < numCharges)
			{
				effect.numCharges++;
				if (caster instanceof L2PcInstance)
				{
					caster.sendPacket(new EtcStatusUpdate((L2PcInstance) caster));
					SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
					sm.addNumber(effect.numCharges);
					caster.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXIMUM);
				caster.sendPacket(sm);
			}
		}
		else
			getEffects(caster, caster);
	}
}

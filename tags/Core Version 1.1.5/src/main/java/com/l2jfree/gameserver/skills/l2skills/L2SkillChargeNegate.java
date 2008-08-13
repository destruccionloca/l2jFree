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
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.effects.EffectCharge;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.templates.StatsSet;

/**
 * Used for Break Duress skill mainly
 * uses number of charges to negate, negate number depends on charge consume  
 * @author Darki699
 */
public class L2SkillChargeNegate extends L2Skill
{
	final int	chargeSkillId;

	public L2SkillChargeNegate(StatsSet set)
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
				sm.addSkillName(getId());
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
			sm.addSkillName(getId());
			activeChar.sendPacket(sm);
			return;
		}

		effect.numCharges -= getNumCharges();

		if (activeChar instanceof L2PcInstance)
		{
			activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
		}

		if (effect.numCharges == 0)
			effect.exit();

		for (L2Object element : targets)
		{
			L2Character target = (L2Character) element;
			if (target.isAlikeDead())
				continue;

			String[] _negateStats = getNegateStats();
			int count = 0;
			for (String stat : _negateStats)
			{
				count++;
				if (count > getNumCharges())
				{
					// ROOT=1 PARALYZE=2 SLOW=3 
					return;
				}

				stat = stat.toLowerCase().intern();
				if (stat == "root")
				{
					negateEffect(target, SkillType.ROOT);
				}
				if (stat == "slow")
				{
					negateEffect(target, SkillType.DEBUFF);
					negateEffect(target, SkillType.WEAKNESS);
				}
				if (stat == "paralyze")
				{
					negateEffect(target, SkillType.PARALYZE);
				}
			}
		}

	}

	private void negateEffect(L2Character target, SkillType type)
	{
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
		{
			if (type == SkillType.DEBUFF || type == SkillType.WEAKNESS)
			{
				if (e.getSkill().getSkillType() == type)
				{
					// Only exit debuffs and weaknesses affecting runSpd 
					for (Func f : e.getStatFuncs())
					{
						if (f.stat == Stats.RUN_SPEED)
						{
							if (target instanceof L2PcInstance)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
								sm.addSkillName(e.getSkill().getId());
								((L2PcInstance) target).sendPacket(sm);
							}

							e.exit();
							break;
						}
					}

				}
			}

			else if (e.getSkill().getSkillType() == type)
			{
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					sm.addSkillName(e.getSkill().getId());
					((L2PcInstance) target).sendPacket(sm);
				}

				e.exit();
			}
		}
	}
}

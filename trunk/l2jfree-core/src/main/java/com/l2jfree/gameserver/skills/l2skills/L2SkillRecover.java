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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.tools.random.Rnd;

/**
 * For skills like "Blessing of Eva". Each part is <U>optional</U>.
 * <LI>Restores CP (may not specify)</LI>
 * <LI>Restores HP (may not specify)</LI>
 * <LI>Restores MP (may not specify)</LI>
 * <LI>Cancels bad effects (power = chance, set 0 to disable)</LI>
 * @author Savormix
 */
public class L2SkillRecover extends L2Skill
{
	private final double	_cp;
	private final double	_hp;
	private final double	_mp;
	private final double	_power;

	public L2SkillRecover(StatsSet set)
	{
		super(set);
		_cp = set.getFloat("restoredCP", 0);
		_hp = set.getFloat("restoredHP", 0);
		_mp = set.getFloat("restoredMP", 0);
		if (getPower() > 95)
			_power = 95;
		else
			_power = getPower();
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		for (L2Character target: targets)
		{
			if (target == null || target.isDead() ||
					target.isInsideZone(L2Zone.FLAG_NOHEAL))
				continue;
			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
				continue;
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof L2PcInstance &&
						((L2PcInstance) target).isCursedWeaponEquipped())
					continue;
				else if (activeChar instanceof L2PcInstance &&
						((L2PcInstance) activeChar).isCursedWeaponEquipped())
					continue;
			}

			StatusUpdate su = new StatusUpdate(target.getObjectId());
			if (_cp > 0)
			{
				target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() + _cp);
				su.addAttribute(StatusUpdate.CUR_CP, (int) target.getStatus().getCurrentCp());
			}
			if (_hp > 0)
			{
				target.getStatus().increaseHp(_hp);
				su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
			}
			if (_mp > 0)
			{
				target.getStatus().increaseMp(_mp);
				su.addAttribute(StatusUpdate.CUR_MP, (int) target.getStatus().getCurrentMp());
			}
			target.sendPacket(su);
			
			if (_power <= 0)
				continue; //do not negate anything

			L2Effect[] effects = target.getAllEffects();
			for (L2Effect e : effects)
				tryNegate(e);
		}
		//effect self :]
		L2Effect effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		// cast self effect if any
		getEffectsSelf(activeChar);
	}

	private final boolean isBadAbnormal(L2Effect e)
	{
		//optimized checks
		if (e.getEffectType() == L2EffectType.ENVIRONMENT)
			return false;
		if (e.getSkill().isDebuff())
			return true;
		//in-depth check
		switch (e.getSkill().getSkillType())
		{
		case DEBUFF:
		case WEAKNESS:
		case STUN:
		case SLEEP:
		case CONFUSION:
		case MUTE:
		case FEAR:
		case POISON:
		case BLEED:
		case PARALYZE:
		case ROOT:
			return true;
		default:
			return false;
		}
	}

	private final void tryNegate(L2Effect e)
	{
		if (isBadAbnormal(e) && Rnd.get(100) < _power)
			e.exit();
	}
}

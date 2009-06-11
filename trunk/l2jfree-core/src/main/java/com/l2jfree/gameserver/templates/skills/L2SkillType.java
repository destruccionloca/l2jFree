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
package com.l2jfree.gameserver.templates.skills;

import java.lang.reflect.Constructor;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.l2skills.L2SkillAgathion;
import com.l2jfree.gameserver.skills.l2skills.L2SkillChangeWeapon;
import com.l2jfree.gameserver.skills.l2skills.L2SkillChargeDmg;
import com.l2jfree.gameserver.skills.l2skills.L2SkillChargeNegate;
import com.l2jfree.gameserver.skills.l2skills.L2SkillCreateItem;
import com.l2jfree.gameserver.skills.l2skills.L2SkillDecoy;
import com.l2jfree.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jfree.gameserver.skills.l2skills.L2SkillMount;
import com.l2jfree.gameserver.skills.l2skills.L2SkillRecover;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSweep;
import com.l2jfree.gameserver.skills.l2skills.L2SkillTrap;
import com.l2jfree.gameserver.templates.StatsSet;

public enum L2SkillType
{
	PDAM,
	MDAM,
	CPDAM,
	AGGDAMAGE,
	DOT,
	HOT,
	BLEED,
	POISON,
	CPHOT,
	MPHOT,
	BUFF,
	DEBUFF,
	STUN,
	ROOT,
	CONT,
	CONFUSION,
	FUSION,
	PARALYZE,
	FEAR,
	SLEEP,
	HEAL,
	HEAL_MOB,
	COMBATPOINTHEAL,
	MANAHEAL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	RESURRECT,
	PASSIVE,
	UNLOCK,
	GIVE_SP,
	NEGATE,
	CANCEL,
	CANCEL_DEBUFF,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	CONFUSE_MOB_ONLY,
	DEATHLINK,
	BLOW,
	FATALCOUNTER,
	DETECT_WEAKNESS,
	ENCHANT_ARMOR, // should be deprecated
	ENCHANT_WEAPON, // should be deprecated
	FEED_PET, // should be deprecated
	HEAL_PERCENT,
	HEAL_STATIC,
	LUCK, // should be deprecated
	MANADAM,
	MAKE_KILLABLE,
	MAKE_QUEST_DROPABLE,
	MDOT,
	MUTE,
	RECALL,
	REFLECT, // should be depreacted
	SUMMON_FRIEND,
	SOULSHOT, // should be deprecated
	SPIRITSHOT, // should be deprecated
	SPOIL,
	WEAKNESS, // should be deprecated
	DISARM,
	STEAL_BUFF,
	FAKE_DEATH,
	SIEGEFLAG,
	TAKECASTLE,
	TAKEFORT,
	BEAST_FEED, // should be deprecated
	DRAIN_SOUL, // should be deprecated
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	WEAPON_SA, // should be depreacted and replaced with BUFF
	DELUXE_KEY_UNLOCK, // should be deprecated
	SOW,
	HARVEST,
	CHARGESOUL,
	GET_PLAYER,
	FISHING,
	PUMPING,
	REELING,
	AGGDEBUFF,
	CPHEAL_PERCENT,
	SUMMON_TREASURE_KEY,
	ERASE,
	MAGE_BANE,
	WARRIOR_BANE,
	STRSIEGEASSAULT,
	UNSUMMON_ENEMY_PET,
	BETRAY,
	BALANCE_LIFE,
	TRANSFORMDISPEL,
	DETECT_TRAP,
	REMOVE_TRAP,
	SHIFT_TARGET,
	INSTANT_JUMP,
	BALLISTA,
	EXTRACTABLE,
	LEARN_SKILL,
	CLAN_GATE,
	UNDEAD_DEFENSE,
	CANCEL_STATS,

	AGATHION(L2SkillAgathion.class),
	MOUNT(L2SkillMount.class),
	CHANGEWEAPON(L2SkillChangeWeapon.class),
	CHARGEDAM(L2SkillChargeDmg.class),
	CHARGE_NEGATE(L2SkillChargeNegate.class), // should be merged into NEGATE
	CREATE_ITEM(L2SkillCreateItem.class),
	DECOY(L2SkillDecoy.class),
	DRAIN(L2SkillDrain.class),
	SWEEP(L2SkillSweep.class),
	RECOVER(L2SkillRecover.class),
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	SUMMON(L2SkillSummon.class),
	SUMMON_TRAP(L2SkillTrap.class),
	FATAL,
	// Skill that has no effect.
	DUMMY,
	// Skill is done within the core.
	COREDONE,
	// Unimplemented
	NOTDONE,
	TELEPORT,
	CHANGE_APPEARANCE;

	private final Constructor<? extends L2Skill>	_constructor;

	private L2SkillType()
	{
		this(L2Skill.class);
	}

	private L2SkillType(Class<? extends L2Skill> clazz)
	{
		try
		{
			_constructor = clazz.getConstructor(StatsSet.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public L2Skill makeSkill(StatsSet set) throws Exception
	{
		return _constructor.newInstance(set);
	}
}

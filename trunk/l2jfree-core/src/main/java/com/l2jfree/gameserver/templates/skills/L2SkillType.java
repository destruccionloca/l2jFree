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
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
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
	MHOT,
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
	FORCE_BUFF,
	PARALYZE,
	FEAR,
	SLEEP,
	DEATH_MARK,
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
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	FEED_PET,
	HEAL_PERCENT,
	HEAL_STATIC,
	LUCK,
	MANADAM,
	MAKE_KILLABLE,
	MAKE_QUEST_DROPABLE,
	MDOT,
	MUTE,
	RECALL,
	REFLECT,
	SUMMON_FRIEND,
	SOULSHOT,
	SPIRITSHOT,
	SPOIL,
	SWEEP,
	WEAKNESS,
	DISARM,
	STEAL_BUFF,
	DEATHLINK_PET,
	MANA_BY_LEVEL,
	FAKE_DEATH,
	SIEGEFLAG,
	TAKECASTLE,
	TAKEFORT,
	UNDEAD_DEFENSE,
	BEAST_FEED,
	DRAIN_SOUL,
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	WEAPON_SA,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	CHARGESOUL,
	GET_PLAYER,
	FISHING,
	PUMPING,
	REELING,
	CANCEL_TARGET,
	AGGDEBUFF,
	COMBATPOINTPERCENTHEAL,
	SUMMONCP,
	SUMMON_TREASURE_KEY,
	SUMMON_CURSED_BONES,
	ERASE,
	MAGE_BANE,
	WARRIOR_BANE,
	STRSIEGEASSAULT,
	RAID_DESCRIPTION,
	UNSUMMON_ENEMY_PET,
	BETRAY,
	BALANCE_LIFE,
	SERVER_SIDE,
	TRANSFORMDISPEL,
	DETECT_TRAP,
	REMOVE_TRAP,
	SHIFT_TARGET,
	INSTANT_JUMP,
	BALLISTA,
	EXTRACTABLE,
	CLAN_GATE,
	
	AGATHION(L2SkillAgathion.class),
	MOUNT(L2SkillMount.class),
	CHANGEWEAPON(L2SkillChangeWeapon.class),
	CHARGEDAM(L2SkillChargeDmg.class),
	CHARGE_NEGATE(L2SkillChargeNegate.class),
	CREATE_ITEM(L2SkillCreateItem.class),
	DECOY(L2SkillDecoy.class),
	SUMMON_HORSE,
	DRAIN(L2SkillDrain.class),
	LUCKNOBLESSE(L2SkillCreateItem.class),
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	SUMMON(L2SkillSummon.class),
	SUMMON_TRAP(L2SkillTrap.class),
	
	// Skill that has no effect.
	DUMMY,
	// Skill is done within the core.
	COREDONE,
	// Unimplemented
	NOTDONE;
	
	private final Constructor<? extends L2Skill> _constructor;
	
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

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
package com.l2jfree.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.instancemanager.CoupleManager;
import com.l2jfree.gameserver.instancemanager.FourSepulchersManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfree.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.entity.Couple;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.entity.events.CTF;
import com.l2jfree.gameserver.model.entity.events.DM;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.model.entity.events.VIP;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jfree.gameserver.skills.ChanceCondition;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.IChanceSkillTrigger;
import com.l2jfree.gameserver.skills.TriggeredSkill;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.gameserver.taskmanager.DecayTaskManager;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.lang.L2Integer;
import com.l2jfree.lang.L2System;
import com.l2jfree.util.LinkedBunch;

public class L2Skill implements FuncOwner, IChanceSkillTrigger
{
	public static final L2Skill[]	EMPTY_ARRAY					= new L2Skill[0];

	protected static Log			_log						= LogFactory.getLog(L2Skill.class.getName());

	public static final int			SKILL_CUBIC_MASTERY			= 143;
	public static final int			SKILL_LUCKY					= 194;
	public static final int			SKILL_CREATE_COMMON			= 1320;
	public static final int			SKILL_CREATE_DWARVEN		= 172;
	public static final int			SKILL_EXPERTISE				= 239;
	public static final int			SKILL_CRYSTALLIZE			= 248;
	public static final int			SKILL_DIVINE_INSPIRATION	= 1405;
	public static final int			SKILL_SOUL_MASTERY			= 467;
	public static final int			SKILL_CLAN_LUCK				= 390;

	public static enum SkillOpType
	{
		OP_PASSIVE, OP_ACTIVE, OP_TOGGLE
	}

	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PET,
		TARGET_SUMMON,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_AREA,
		TARGET_FRONT_AREA,
		TARGET_BEHIND_AREA,
		TARGET_AURA,
		TARGET_FRONT_AURA,
		TARGET_BEHIND_AURA,
		TARGET_CORPSE,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_AREA_CORPSES,
		TARGET_MULTIFACE,
		TARGET_AREA_UNDEAD,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_FLAGPOLE,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_ENEMY_ALLY,
		TARGET_ENEMY_PET,
		TARGET_GATE,
		TARGET_COUPLE,
		TARGET_MOB,
		TARGET_AREA_MOB,
		TARGET_KNOWNLIST,
		TARGET_GROUND,
		TARGET_INITIATOR
		// TARGET_BOSS
	}

	private static enum OffensiveState
	{
		OFFENSIVE, NEUTRAL, POSITIVE;
	}

	// elements
	public final static int			ELEMENT_FIRE	= 1;
	public final static int			ELEMENT_WATER	= 2;
	public final static int			ELEMENT_WIND	= 3;
	public final static int			ELEMENT_EARTH	= 4;
	public final static int			ELEMENT_HOLY	= 5;
	public final static int			ELEMENT_DARK	= 6;
	public final static int			ELEMENT_UNHOLY	= 5;
	public final static int			ELEMENT_SACRED	= 6;

	// conditional values
	public final static int			COND_BEHIND		= 0x0008;
	public final static int			COND_CRIT		= 0x0010;

	// these two build the primary key
	private final Integer			_id;
	private final int				_level;

	/** Identifier for a skill that client can't display */
	private final int				_displayId;

	// not needed, just for easier debug
	private final String			_name;

	// Reference ID for extractable items
	private final int				_refId;

	private final SkillOpType		_operateType;
	private final boolean			_magic;
	private final boolean			_itemSkill;
	private final boolean			_physic;
	private final boolean			_staticReuse;
	private final boolean			_staticHitTime;
	private final int				_mpConsume;
	private final int				_mpInitialConsume;
	private final int				_hpConsume;
	private final int				_cpConsume;
	private final int[]				_teleportCoords;
	private final String			_recallType;
	private final int				_hairColorId;
	private final int				_faceId;
	private final int				_hairStyleId;

	private final int				_itemConsume;
	private final int				_itemConsumeId;
	// item consume count over time
	private final int				_itemConsumeOT;
	// item consume id over time
	private final int				_itemConsumeIdOT;
	// how many times to consume an item
	private final int				_itemConsumeSteps;

	private final int				_targetConsume;
	private final int				_targetConsumeId;

	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int				_summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	private final int				_summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	private final int				_summonTimeLostActive;
	// item consume time in milliseconds
	private final int				_itemConsumeTime;
	private final boolean			_isCubic;

	private final int				_feed;

	private final int				_castRange;
	private final int				_effectRange;

	// Abnormal levels for skills and their canceling, e.g. poison vs negate
	private final int				_abnormalLvl;				// e.g. poison or bleed lvl 2
	// Note: see also _effectAbnormalLvl
	private final int				_negateLvl;				// abnormalLvl is negated with negateLvl
	private final int[]				_negateId;					// cancels the effect of skill ID
	private final String[]			_negateStats;				// lists the effect types that are canceled
	private final int				_maxNegatedEffects;		// maximum number of effects to negate

	// all times in milliseconds
	private final int				_hitTime;
	private final int				_skillInterruptTime;
	private final int				_coolTime;
	private final int				_reuseDelay;
	// for item skills delay on equip
	private final int				_equipDelay;

	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType	_targetType;
	// base success chance
	private final double			_power;
	private final int				_levelDepend;
	private final boolean			_ignoreResists;

	// Kill by damage over time
	private final boolean			_killByDOT;

	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int				_skillRadius;

	private final L2SkillType		_skillType;
	private final L2SkillType		_effectType;				// additional effect has a type
	private final int				_effectAbnormalLvl;		// abnormal level for the additional effect type, e.g. poison lvl 1
	private final int				_effectPower;
	private final int				_effectId;
	private final float				_effectLvl;				// normal effect level
	private final int				_skill_landing_percent;

	private final boolean			_ispotion;
	private final int				_element;
	private final int				_elementPower;
	private final int				_activateRate;
	private final int				_magicLevel;

	private final int				_condition;
	private final boolean			_overhit;
	private final boolean			_ignoreShld;
	private final int				_weaponsAllowed;
	private final int				_armorsAllowed;

	private final OffensiveState	_offensiveState;

	private final int				_needCharges;
	private final int				_giveCharges;
	private final int				_maxCharges;

	private final ChanceCondition	_chanceCondition;
	private final TriggeredSkill	_triggeredSkill;

	private final int				_soulConsume;
	private final int				_soulMaxConsume;
	private final int				_numSouls;
	private final int				_expNeeded;
	private final int				_critChance;

	//Stats for transformation skills
	private final int				_transformId;

	private int						_duration;

	private final int				_baseCritRate;				// percent of success for skill critical hit (especially for PDAM & BLOW -
	// they're not affected by rCrit values or buffs). Default loads -1 for all
	// other skills but 0 to PDAM & BLOW
	private final int				_lethalEffect1;			// percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only
	// for PDAM skills)
	private final int				_lethalEffect2;			// percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only
	// for PDAM skills)
	private final boolean			_directHpDmg;				// If true then dmg is being make directly
	private final boolean			_isDance;					// If true then casting more dances will cost more MP
	private final boolean			_isSong;					// If true then casting more songs will cost more MP
	private final int				_nextDanceCost;
	private final float				_sSBoost;					// If true skill will have SoulShot boost (power*2)

	private final int				_timeMulti;

	private final boolean			_isAdvanced;				// Used by siege flag summon skills
	private final String			_attribute;

	private final int				_minPledgeClass;

	private final int				_aggroPoints;

	protected Condition				_preCondition;
	protected FuncTemplate[]		_funcTemplates;
	protected EffectTemplate[]		_effectTemplates;
	protected EffectTemplate[]		_effectTemplatesSelf;

	//Attached skills for Special Abilities
	protected L2Skill[]				_skillsOnCast;
	protected int[]					_skillsOnCastId, _skillsOnCastLvl;

	// Flying support
	private final FlyType			_flyType;
	private final int				_flyRadius;
	private final float				_flyCourse;

	private final boolean			_isDebuff;

	private final int				_afroId;
	private final boolean			_isHerbEffect;

	private final int				_learnSkillId;
	private final int				_learnSkillLvl;

	private final boolean			_useShield;
	private final boolean			_ignoreShield;
	private final boolean			_isSuicideAttack;
	private final boolean			_canBeReflected;
	private final int				_afterEffectId;
	private final int				_afterEffectLvl;
	
	private final boolean 			_stayAfterDeath; // skill should stay after death

	public L2Skill(StatsSet set)
	{
		_id = L2Integer.valueOf(set.getInteger("skill_id"));
		_level = set.getInteger("level");
		_refId = set.getInteger("referenceId", set.getInteger("itemConsumeId", 0));
		_afroId = set.getInteger("afroId", 0);
		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name").intern();
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_magic = set.getBool("isMagic", isSkillTypeMagic());
		_itemSkill = set.getBool("isItem", 3080 <= getId() && getId() <= 3259);
		_physic = set.getBool("isPhysic", false);
		_ispotion = set.getBool("isPotion", false);
		_staticReuse = set.getBool("staticReuse", false);
		_staticHitTime = set.getBool("staticHitTime", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_cpConsume = set.getInteger("cpConsume", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		_isCubic = set.getBool("isCubic", false);
		_afterEffectId = set.getInteger("afterEffectId", 0);
		_afterEffectLvl = set.getInteger("afterEffectLvl", 1);
		_hairColorId = set.getInteger("hairColorId", -1);
		_faceId = set.getInteger("faceId", -1);
		_hairStyleId = set.getInteger("hairStyleId", -1);
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_teleportCoords = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
				_teleportCoords[i] = Integer.valueOf(valuesSplit[i]);
		}
		else
			_teleportCoords = null;

		_isHerbEffect = _name.contains("Herb");

		_castRange = set.getInteger("castRange", 0);
		_effectRange = set.getInteger("effectRange", -1);

		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		_effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		_negateLvl = set.getInteger("negateLvl", -1);
		_negateStats = StringUtils.split(set.getString("negateStats", ""), " ");
		for (int i = 0; i < _negateStats.length; i++)
			_negateStats[i] = _negateStats[i].toLowerCase().intern();

		String negateId = set.getString("negateId", null);
        if (negateId != null)
        {
            String[] valuesSplit = negateId.split(",");
            _negateId = new int[valuesSplit.length];
    		for (int i = 0; i < valuesSplit.length;i++)
    		{
    			_negateId[i] = Integer.valueOf(valuesSplit[i]);
    		}
        }
        else
        	_negateId = new int[0];
        
		_maxNegatedEffects = set.getInteger("maxNegated", 0);
		_stayAfterDeath = set.getBool("stayAfterDeath", false);
		_killByDOT = set.getBool("killByDOT", false);

		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = isMagic() ? getHitTime() / 2 : 0;
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_equipDelay = set.getInteger("equipDelay", 0);

		_isDance = set.getBool("isDance", false);
		_isSong = set.getBool("isSong", false);
		if (_isDance || _isSong)
			_timeMulti = Config.ALT_DANCE_TIME;
		else if (_skillType == L2SkillType.BUFF) //This should correct the time effect that was caused on debuffs on AltBuffTime config
			_timeMulti = Config.ALT_BUFF_TIME;
		else
			_timeMulti = 1; //If the skills is not a DANCE type skill or BUFF type, the effect time is the normal, without any multiplier

		_skillRadius = set.getInteger("skillRadius", 80);

		_power = set.getFloat("power", 0.f);

		_levelDepend = set.getInteger("lvlDepend", 0);
		_ignoreResists = set.getBool("ignoreResists", false);

		_isAdvanced = set.getBool("isAdvanced", false); // Used by siege flag summon skills
		_isDebuff = set.getBool("isDebuff", false);
		_feed = set.getInteger("feed", 0); // Used for pet food

		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		_effectPower = set.getInteger("effectPower", 0);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getFloat("effectLevel", 0.f);
		_skill_landing_percent = set.getInteger("skill_landing_percent", 0);
		_element = set.getInteger("element", 0);
		_activateRate = set.getInteger("activateRate", -1);
		_magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));

		_ignoreShld = set.getBool("ignoreShld", false);
		_elementPower = set.getInteger("elementPower", 0);
		_condition = set.getInteger("condition", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);

		_needCharges = set.getInteger("needCharges", 0);
		_giveCharges = set.getInteger("giveCharges", 0);
		_maxCharges = set.getInteger("maxCharges", 0);

		_minPledgeClass = set.getInteger("minPledgeClass", 0);

		_chanceCondition = ChanceCondition.parse(set);
		_triggeredSkill = TriggeredSkill.parse(set);

		_offensiveState = getOffensiveState(set);

		_numSouls = set.getInteger("num_souls", 0);
		_soulConsume = set.getInteger("soulConsumeCount", 0);
		_soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		_expNeeded = set.getInteger("expNeeded", 0);
		_critChance = set.getInteger("critChance", 0);

		// Stats for transformation Skill
		_transformId = set.getInteger("transformId", 0);

		_duration = set.getInteger("duration", 0);

		_baseCritRate = set.getInteger("baseCritRate", (_skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW) ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1", 0);
		_lethalEffect2 = set.getInteger("lethal2", 0);
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_sSBoost = set.getFloat("SSBoost", 1.f);

		_aggroPoints = set.getInteger("aggroPoints", 0);

		_flyType = set.getEnum("flyType", FlyType.class, null);
		_flyRadius = set.getInteger("flyRadius", 200);
		_flyCourse = set.getFloat("flyCourse", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		_attribute = set.getString("attribute", "");
		_useShield = set.getBool("useShield", false);
		_ignoreShield = set.getBool("ignoreShld", false);

		_learnSkillId = set.getInteger("learnSkillId", 0);
		_learnSkillLvl = set.getInteger("learnSkillLvl", 1);
		_recallType = set.getString("recallType", "");
	}

	private boolean isPurePassiveSkill()
	{
		return isPassive() && !isChance();
	}
	
	private boolean isPureChanceSkill()
	{
		return isChance() && _triggeredSkill != null;
	}

	public final void validate() throws Exception
	{
		validateOffensiveAndDebuffState();
		validateTriggeredSkill();
	}

	private void validateOffensiveAndDebuffState() throws Exception
	{
		if (!isOffensive() && isDebuff() && !isPureChanceSkill())
			throw new IllegalStateException(toString());
	}

	private void validateTriggeredSkill() throws Exception
	{
		final L2Skill triggeredSkill = getTriggeredSkill();

		// nonsense...
		if (triggeredSkill == this)
			throw new IllegalStateException(toString());

		// must have triggered skill, and can't have effects
		if (isActive() && getSkillType() == L2SkillType.FUSION)
		{
			if (triggeredSkill == null)
				throw new IllegalStateException(toString());

			if (_effectTemplates != null)
				throw new IllegalStateException(toString());
		}
		// can have triggered skill
		else if (isChance())
		{
		}
		// can't have triggered skill
		else
		{
			if (triggeredSkill != null)
				throw new IllegalStateException(toString());
		}
	}

	@SuppressWarnings("fallthrough")
	private OffensiveState getOffensiveState(StatsSet set)
	{
		final OffensiveState defaultState = getDefaultOffensiveState();

		final Boolean isOffensive = set.contains("offensive") ? set.getBool("offensive") : null;
		final Boolean isNeutral = set.contains("neutral") ? set.getBool("neutral") : null;

		if (isOffensive == null && isNeutral == null)
			return defaultState;

		if (isPassive() || isToggle())
			throw new IllegalStateException(this + " shouldn't have 'offensive'/'neutral' property specified!");

		final List<OffensiveState> denied = new ArrayList<OffensiveState>(2);
		final List<OffensiveState> requested = new ArrayList<OffensiveState>(2);

		if (isOffensive != null)
		{
			if (isOffensive.booleanValue())
				requested.add(OffensiveState.OFFENSIVE);
			else
				denied.add(OffensiveState.OFFENSIVE);
		}

		if (isNeutral != null)
		{
			if (isNeutral.booleanValue())
				requested.add(OffensiveState.NEUTRAL);
			else
				denied.add(OffensiveState.NEUTRAL);
		}

		switch (requested.size())
		{
			case 2:
				throw new IllegalStateException("Both 'neutral' and 'offensive' property requested for " + this);
			case 1:
				return requested.get(0);
			case 0:
				if (!denied.contains(defaultState))
					return defaultState;
			default:
				throw new IllegalStateException("Requested 'neutral'/'offensive' value rules out default for " + this);
		}
	}

	public void useSkill(L2Character caster, L2Character... targets)
	{
		caster.sendPacket(ActionFailed.STATIC_PACKET);

		if (caster instanceof L2PcInstance)
			caster.sendMessage("Skill not implemented. Skill ID: " + getId() + " " + getSkillType());
	}

	public final boolean isPotion()
	{
		return _ispotion;
	}

	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}

	public final L2SkillType getSkillType()
	{
		return _skillType;
	}

	public final boolean hasEffectWhileCasting()
	{
		return getSkillType() == L2SkillType.FUSION || getSkillType() == L2SkillType.SIGNET_CASTTIME;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public final int getElement()
	{
		return _element;
	}

	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR>
	 * <BR>
	 */
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final int getCondition()
	{
		return _condition;
	}

	public final boolean ignoreShld()
	{
		return _ignoreShld;
	}

	public final boolean isOverhit()
	{
		return _overhit;
	}

	public final boolean killByDOT()
	{
		return _killByDOT;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	/**
	 * Return the power of the skill.<BR>
	 * <BR>
	 */
	public final double getPower(L2Character activeChar)
	{
		if (activeChar == null)
			return _power;

		switch (_skillType)
		{
			case DEATHLINK:
			{
				return _power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
			}
			case FATAL:
			{
				return _power * 3.5 * (1 - activeChar.getCurrentHp() / activeChar.getMaxHp());
			}
			default:
				return _power;
		}
	}

	public final double getPower()
	{
		return _power;
	}

	public final String[] getNegateStats()
	{
		return _negateStats;
	}

	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public final int getNegateLvl()
	{
		return _negateLvl;
	}

    public final int[] getNegateId()
    {
        return _negateId;
    }

	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}

	public final int getMagicLvl()
	{
		return _magicLevel;
	}

	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}

	public final L2Skill getTriggeredSkill()
	{
		return _triggeredSkill != null ? _triggeredSkill.getTriggeredSkill() : null;
	}

	public final int getLevelDepend()
	{
		return _levelDepend;
	}

	/**
	 * Return the skill landing percent probability.<BR>
	 * <BR>
	 */
	public final int getLandingPercent()
	{
		return _skill_landing_percent;
	}

	/**
	 * Return the additional effect power or base probability.<BR>
	 * <BR>
	 */
	public final int getEffectPower()
	{
		return _effectPower;
	}

	/**
	 * Return true if skill should ignore all resistances
	 */
	public final boolean ignoreResists()
	{
		return _ignoreResists;
	}

	/**
	 * Return the additional effect Id.<BR>
	 * <BR>
	 */
	public final int getEffectId()
	{
		return _effectId;
	}

	/**
	 * Return the additional effect level.<BR>
	 * <BR>
	 */
	public final float getEffectLvl()
	{
		return _effectLvl;
	}

	/**
	 * Return the additional effect skill type (ex : STUN, PARALYZE,...).<BR>
	 * <BR>
	 */
	public final L2SkillType getEffectType()
	{
		return _effectType;
	}

	/**
	 * @return Returns the timeMulti.
	 */
	public final int getTimeMulti()
	{
		return _timeMulti;
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}

	/**
	 * @return Returns the hitTime.
	 */
	public final int getHitTime()
	{
		return _hitTime;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the cpConsume.
	 */
	public final int getCpConsume()
	{
		return _cpConsume;
	}

	public final boolean allowOnTransform()
	{
		return isPassive() || isChance();
	}

	/**
	 * @return Returns the id.
	 */
	public final Integer getId()
	{
		return _id;
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	/**
	 * @return Returns the _targetConsumeId.
	 */
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}

	/**
	 * @return Returns the targetConsume.
	 */
	public final int getTargetConsume()
	{
		return _targetConsume;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}

	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}

	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}

	public final boolean isCubic()
	{
		return _isCubic;
	}

	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}

	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}

	public final boolean isItemSkill()
	{
		return _itemSkill;
	}

	/**
	 * @return Returns if the skill is Physical.
	 */
	public final boolean isPhysical()
	{
		return _physic;
	}

	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse || isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED;
	}

	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return _staticHitTime || isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED;
	}

	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final int getEquipDelay()
	{
		return _equipDelay;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public final boolean isChance()
	{
		return _chanceCondition != null && isPassive();
	}

	public final boolean isDance()
	{
		return _isDance;
	}

	public final boolean isSong()
	{
		return _isSong;
	}

	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}

	public final boolean isAdvanced()
	{
		return _isAdvanced;
	}

	/**
	 *@return Returns the boolean _isDebuff.
	 */
	public final boolean isDebuff()
	{
		return _isDebuff;
	}

	public final float getSSBoost()
	{
		return _sSBoost;
	}

	public final int getAggroPoints()
	{
		return _aggroPoints;
	}

	public final boolean useSoulShot()
	{
		switch (getSkillType())
		{
			case PDAM:
			case STUN:
			case CHARGEDAM:
			case BLOW:
				return true;
			default:
				return false;
		}
	}

	public final boolean useSpiritShot()
	{
		return isMagic();
	}

	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING));
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case BETRAY:
			case DISARM:
			case STEAL_BUFF:
			case AGGDAMAGE:
			case DELUXE_KEY_UNLOCK:
			case FATALCOUNTER:
			case MAKE_KILLABLE:
			case MAKE_QUEST_DROPABLE:
				return true;
			default:
				return false;
		}
	}

	public final boolean isOffensive()
	{
		return _offensiveState == OffensiveState.OFFENSIVE;
	}

	public final boolean isNeutral()
	{
		return _offensiveState == OffensiveState.NEUTRAL;
	}

	public final boolean isPositive()
	{
		return _offensiveState == OffensiveState.POSITIVE;
	}

	public final int getNeededCharges()
	{
		return _needCharges;
	}

	public final int getGiveCharges()
	{
		return _giveCharges;
	}

	public final int getMaxCharges()
	{
		return _maxCharges;
	}

	public final int getNumSouls()
	{
		return _numSouls;
	}

	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}

	public final int getSoulConsumeCount()
	{
		return _soulConsume;
	}

	public final int getExpNeeded()
	{
		return _expNeeded;
	}

	public final int getCritChance()
	{
		return _critChance;
	}

	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}

	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}

	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}

	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}

	/**
	 * @return pet food
	 */
	public final int getFeed()
	{
		return _feed;
	}

	public final FlyType getFlyType()
	{
		return _flyType;
	}

	public final int getFlyRadius()
	{
		return _flyRadius;
	}

	public final float getFlyCourse()
	{
		return _flyCourse;
	}

	public final int getTransformId()
	{
		return _transformId;
	}

	public final int getDuration()
	{
		return _duration;
	}

	public final void setDuration(int dur)
	{
		_duration = dur;
	}

	public final static boolean skillLevelExists(int skillId, int level)
	{
		return SkillTable.getInstance().getInfo(skillId, level) != null;
	}

	public final boolean isSkillTypeMagic()
	{
		switch (getSkillType())
		{
			// TODO: other skillTypes
			case MDAM:
			case HEAL:
			case SUMMON_FRIEND:
			case BALANCE_LIFE:
				return true;
			default:
				return false;
		}
	}

	private OffensiveState getDefaultOffensiveState()
	{
		if (isPurePassiveSkill() || isPureChanceSkill() || isToggle())
			return OffensiveState.POSITIVE;

		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case STRSIEGEASSAULT:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case FATALCOUNTER:
			case DETECT_WEAKNESS:
			case MDOT:
			case MANADAM:
			case MUTE:
			case SPOIL:
			case WEAKNESS:
			case SWEEP:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREDUCE_CHAR:
			case UNSUMMON_ENEMY_PET:
			case BETRAY:
			case GET_PLAYER:
			case DISARM:
			case STEAL_BUFF:
			case INSTANT_JUMP:
			case SIGNET_CASTTIME:
			case BALLISTA:
			case FATAL:
				return OffensiveState.OFFENSIVE;
			case BUFF:
			case CONT:
			case HEAL:
			case HEAL_STATIC:
			case HEAL_PERCENT:
			case BALANCE_LIFE:
			case HOT:
			case MPHOT:
			case CPHOT:
			case MANAHEAL:
			case MANAHEAL_PERCENT:
			case MANARECHARGE:
			case COMBATPOINTHEAL:
			case CPHEAL_PERCENT:
			case REFLECT:
			case LUCK:
			case PASSIVE:
			case WEAPON_SA:
			case RESURRECT:
			case CANCEL_DEBUFF:
			case FUSION:
			case CHARGE_NEGATE:
			case CHARGESOUL:
				return OffensiveState.POSITIVE;
			case DRAIN_SOUL:
			case HEAL_MOB:
			case AGGREDUCE:
			case AGGREMOVE:
			case SHIFT_TARGET:
			case SOULSHOT:
			case SPIRITSHOT:
			case ENCHANT_ARMOR:
			case ENCHANT_WEAPON:
			case MOUNT:
			case DECOY:
			case SUMMON:
			case AGATHION:
			case SUMMON_TRAP:
			case SUMMON_TREASURE_KEY:
			case CREATE_ITEM:
			case EXTRACTABLE:
			case UNLOCK:
			case DELUXE_KEY_UNLOCK:
			case DETECT_TRAP:
			case REMOVE_TRAP:
			case COMMON_CRAFT:
			case DWARVEN_CRAFT:
			case SIEGEFLAG:
			case TAKECASTLE:
			case TAKEFORT:
			case RECALL:
			case SUMMON_FRIEND:
			case CLAN_GATE:
			case GIVE_SP:
			case FEED_PET:
			case BEAST_FEED:
			case NEGATE: // should be divided, since can be positive, and negative skill too
			case MAKE_KILLABLE:
			case MAKE_QUEST_DROPABLE:
			case FAKE_DEATH:
			case SOW:
			case HARVEST:
			case FISHING:
			case PUMPING:
			case REELING:
			case TRANSFORMDISPEL:
			case CHANGEWEAPON:
			case SIGNET:
			case DUMMY:
			case COREDONE:
			case NOTDONE:
			default:
				return OffensiveState.NEUTRAL;
		}
	}

	public final boolean isNeedWeapon()
	{
		return (_skillType == L2SkillType.MDAM);
	}

	public final boolean isStayAfterDeath()
	{
		switch (getId())
		{
			case 5660:
			case 840:
			case 841:
			case 842:
				return true;
			default:
				return _stayAfterDeath;
		}
	}

	private String	_weaponDependancyMessage;

	public final boolean getWeaponDependancy(L2Character activeChar, boolean message)
	{
		int weaponsAllowed = getWeaponsAllowed();
		if (weaponsAllowed == 0)
			return true;

		L2Weapon weapon = activeChar.getActiveWeaponItem();
		if (weapon != null && (weapon.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		L2Weapon weapon2 = activeChar.getSecondaryWeaponItem();
		if (weapon2 != null && (weapon2.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		if (message)
		{
			if (_weaponDependancyMessage == null)
			{
				StringBuilder sb = new StringBuilder();
				for (L2WeaponType wt : L2WeaponType.VALUES)
				{
					if ((wt.mask() & weaponsAllowed) != 0)
					{
						if (sb.length() != 0)
							sb.append('/');

						sb.append(wt);
					}
				}
				sb.append(".");

				_weaponDependancyMessage = getName() + " can only be used with weapons of type " + sb.toString();
				_weaponDependancyMessage = _weaponDependancyMessage.intern();
			}

			activeChar.sendMessage(_weaponDependancyMessage);
		}

		return false;
	}

	public final boolean ownedFuncShouldBeDisabled(L2Character activeChar)
	{
		if (isOffensive())
			return false;

		if (!(isDance() || isSong()) && !getWeaponDependancy(activeChar, false))
			return true;

		return false;
	}

	public boolean checkCondition(L2Character activeChar, L2Object target)
	{
		Condition preCondition = _preCondition;

		if (preCondition == null)
			return true;

		Env env = new Env();
		env.player = activeChar;
		if (target instanceof L2Character)
			env.target = (L2Character) target;
		env.skill = this;

		if (preCondition.test(env))
			return true;

		if (activeChar instanceof L2PcInstance)
			preCondition.sendMessage((L2PcInstance) activeChar, this);
		return false;
	}

	public final L2Character[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		// Init to null the target of the skill
		L2Character target = null;

		// Get the L2Objcet targeted by the user of the skill at this moment
		L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}

		return getTargetList(activeChar, onlyFirst, target);
	}

	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR>
	 * <BR>
	 * <B><U> Values of skill type</U> :</B><BR>
	 * <BR>
	 * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on
	 * the caster if it's a L2PcInstance and no L2PcInstance targeted</li> <li>
	 * SELF</li> <li>HOLY, UNDEAD</li> <li>PET</li> <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li> <li>MULTIFACE</li> <li>PARTY, CLAN</li> <li>CORPSE_PLAYER,
	 * CORPSE_MOB, CORPSE_CLAN</li> <li>UNLOCKABLE</li> <li>ITEM</li> <BR>
	 * <BR>
	 * 
	 * @param activeChar The L2Character who use the skill
	 */
	public final L2Character[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		LinkedBunch<L2Character> targetList = new LinkedBunch<L2Character>();
		try
		{
			// Get the target type of the skill
			// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
			SkillTargetType targetType = getTargetType();

			// Get the type of the skill
			// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
			L2SkillType skillType = getSkillType();

			switch (targetType)
			{
				// The skill can only be used on the L2Character targeted, or on the caster itself
				case TARGET_ONE:
				{
					// automatically selects caster if no target is selected (only positive skills)
					if (isPositive() && target == null)
						target = activeChar;

					boolean canTargetSelf = false;
					switch (skillType)
					{
						case BUFF:
						case HEAL:
						case HOT:
						case HEAL_PERCENT:
						case MANARECHARGE:
						case MANAHEAL:
						case NEGATE:
						case CANCEL:
						case CANCEL_DEBUFF:
						case REFLECT:
						case COMBATPOINTHEAL:
						case CPHEAL_PERCENT:
						case MAGE_BANE:
						case WARRIOR_BANE:
						case BETRAY:
						case BALANCE_LIFE:
							canTargetSelf = true;
							break;
					}

					// Check for null target or any other invalid target
					if (target == null || target.isDead() || (target == activeChar && !canTargetSelf))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					if (!GeoData.getInstance().canSeeTarget(activeChar, target))
						return null;
					return new L2Character[] { target };
				}
				case TARGET_SELF:
				case TARGET_GROUND:
				{
					return new L2Character[] { activeChar };
				}
					/*
					 * case TARGET_BOSS: { L2MinionInstance Minion = null;
					 * Minion = (L2MinionInstance)target; if (activeChar
					 * instanceof L2MinionInstance) return new
					 * L2Character[]{target}; }
					 */
				case TARGET_HOLY:
				{
					if (activeChar instanceof L2PcInstance)
					{
						if (target instanceof L2ArtefactInstance)
							return new L2Character[] { target };
					}

					return null;
				}
				case TARGET_FLAGPOLE:
				{
					return new L2Character[] { activeChar };
				}
				case TARGET_COUPLE:
				{
					if (target != null && target instanceof L2PcInstance)
					{
						int _chaid = activeChar.getObjectId();
						int targetId = target.getObjectId();
						for (Couple cl : CoupleManager.getInstance().getCouples())
						{
							if ((cl.getPlayer1Id() == _chaid && cl.getPlayer2Id() == targetId) || (cl.getPlayer2Id() == _chaid && cl.getPlayer1Id() == targetId))
								return new L2Character[] { target };
						}
					}

					return null;
				}
				case TARGET_PET:
				{
					target = activeChar.getPet();
					if (target != null && !target.isDead())
						return new L2Character[] { target };

					return null;
				}
				case TARGET_SUMMON:
				{
					target = activeChar.getPet();
					if (target != null && !target.isDead() && target instanceof L2SummonInstance)
						return new L2Character[] { target };

					return null;
				}
				case TARGET_OWNER_PET:
				{
					if (activeChar instanceof L2Summon)
					{
						target = ((L2Summon) activeChar).getOwner();
						if (target != null && !target.isDead())
							return new L2Character[] { target };
					}

					return null;
				}
				case TARGET_ENEMY_PET:
				{
					if (target != null && target instanceof L2Summon)
					{
						L2Summon targetPet = null;
						targetPet = (L2Summon) target;
						if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetPet && !targetPet.isDead() && targetPet.getOwner().getPvpFlag() != 0)
						{
							return new L2Character[] { target };
						}
					}
					return null;
				}
				case TARGET_CORPSE_PET:
				{
					if (activeChar instanceof L2PcInstance)
					{
						target = activeChar.getPet();
						if (target != null && target.isDead())
						{
							return new L2Character[] { target };
						}
					}

					return null;
				}
				case TARGET_AURA:
				{
					int radius = getSkillRadius();
					boolean srcInPvP = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					L2PcInstance src = activeChar.getActingPlayer();

					// Go through the L2Character _knownList
					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj instanceof L2Attackable || obj instanceof L2Playable)
						{
							L2Character cha = (L2Character) obj;
							boolean targetInPvP = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (obj == activeChar || obj == src || ((L2Character) obj).isDead())
								continue;
							if (src != null)
							{
								if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
									continue;
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (obj instanceof L2PcInstance)
								{
									if (!src.checkPvpSkill(obj, this))
										continue;
									if ((src.getParty() != null && ((L2PcInstance) obj).getParty() != null) && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
										continue;
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == ((L2PcInstance) obj).getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
											continue;
									}
								}
								else if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, this))
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else
							// Skill user is not L2Playable
							{
								if (!(obj instanceof L2Playable) // Target is not L2Playable
										&& !activeChar.isConfused()) // and caster not confused (?)
									continue;
							}
							if (!Util.checkIfInRange(radius, activeChar, obj, true))
								continue;

							if (!onlyFirst)
								targetList.add((L2Character) obj);
							else
								return new L2Character[] { (L2Character) obj };
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
					// [L2J_JP ADD SANDMAN]
					// case TARGET_AURA:
					/*
					 * case TARGET_AREA: { return getAreaTargetList(activeChar);
					 * }
					 */
				case TARGET_MULTIFACE:
				{
					return getMultiFaceTargetList(activeChar);
				}
				case TARGET_FRONT_AURA:
				{
					int radius = getSkillRadius();
					boolean srcInArena = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					L2PcInstance src = activeChar.getActingPlayer();

					// Go through the L2Character _knownList
					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj instanceof L2Attackable || obj instanceof L2Playable)
						{
							L2Character cha = (L2Character) obj;

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (obj == activeChar || obj == src || ((L2Character) obj).isDead())
								continue;
							if (src != null)
							{
								if (!cha.isInFrontOf(activeChar))
									continue;

								if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
									continue;

								boolean objInPvpZone = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (obj instanceof L2PcInstance)
								{
									if (!src.checkPvpSkill(obj, this))
										continue;
									if ((src.getParty() != null && ((L2PcInstance) obj).getParty() != null) && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == ((L2PcInstance) obj).getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
											continue;
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, this))
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else
							// Skill user is not L2Playable
							{
								if (!(obj instanceof L2Playable) // Target is not L2Playable
										&& !activeChar.isConfused()) // and caster not confused (?)
									continue;
							}
							if (!Util.checkIfInRange(radius, activeChar, obj, true))
								continue;

							if (!onlyFirst)
								targetList.add((L2Character) obj);
							else
								return new L2Character[] { (L2Character) obj };
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_BEHIND_AURA:
				{
					int radius = getSkillRadius();
					boolean srcInArena = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					L2PcInstance src = activeChar.getActingPlayer();

					// Go through the L2Character _knownList
					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj instanceof L2Attackable || obj instanceof L2Playable)
						{
							L2Character cha = (L2Character) obj;

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (obj == activeChar || obj == src || ((L2Character) obj).isDead())
								continue;
							if (src != null)
							{
								if (!cha.isBehind(activeChar))
									continue;

								if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
									continue;

								boolean objInPvpZone = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (obj instanceof L2PcInstance)
								{
									if (!src.checkPvpSkill(obj, this))
										continue;
									if ((src.getParty() != null && ((L2PcInstance) obj).getParty() != null) && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == ((L2PcInstance) obj).getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
											continue;
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, this))
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else
							// Skill user is not L2Playable
							{
								if (!(obj instanceof L2Playable) // Target is not L2Playable
										&& !activeChar.isConfused()) // and caster not confused (?)
									continue;
							}
							if (!Util.checkIfInRange(radius, activeChar, obj, true))
								continue;

							if (!onlyFirst)
								targetList.add((L2Character) obj);
							else
								return new L2Character[] { (L2Character) obj };
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA:
				{
					if ((!(target instanceof L2Attackable || target instanceof L2Playable)) || // Target is not L2Attackable or L2Playable
							(getCastRange() >= 0 && (target == activeChar || target.isAlikeDead()))) // target is null or self or dead/faking
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					L2Character cha;

					if (getCastRange() >= 0)
					{
						cha = target;

						if (!onlyFirst)
							targetList.add(cha); // Add target to target list
						else
							return new L2Character[] { cha };
					}
					else
						cha = activeChar;

					boolean effectOriginIsL2Playable = (cha instanceof L2Playable);
					boolean srcIsSummon = (activeChar instanceof L2Summon);

					L2PcInstance src = activeChar.getActingPlayer();

					int radius = getSkillRadius();

					boolean srcInPvP = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
							continue;
						if (obj == cha)
							continue;
						target = (L2Character) obj;
						boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);

						if (!GeoData.getInstance().canSeeTarget(activeChar, target))
							continue;

						if (!target.isDead() && (target != activeChar))
						{
							if (!Util.checkIfInRange(radius, obj, cha, true))
								continue;
							if (src != null) // caster is L2Playable and exists
							{
								if (obj instanceof L2PcInstance)
								{
									L2PcInstance trg = (L2PcInstance) obj;
									if (trg == src)
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;

									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(obj, this, srcIsSummon))
											continue;
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;

									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(trg, this, srcIsSummon))
											continue;
									}

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;
								}
							}
							else
							// Skill user is not L2Playable
							{
								if (effectOriginIsL2Playable && // If effect starts at L2Playable and
										!(obj instanceof L2Playable)) // Object is not L2Playable
									continue;
							}

							targetList.add((L2Character) obj);
						}
					}

					if (targetList.size() == 0)
						return null;

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_FRONT_AREA:
				{
					if ((!(target instanceof L2Attackable || target instanceof L2Playable)) || //   Target is not L2Attackable or L2Playable
							(getCastRange() >= 0 && (target == activeChar || target.isAlikeDead()))) //target is null or self or dead/faking
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					L2Character cha;

					if (getCastRange() >= 0)
					{
						cha = target;

						if (!onlyFirst)
							targetList.add(cha); // Add target to target list
						else
							return new L2Character[] { cha };
					}
					else
						cha = activeChar;

					boolean effectOriginIsL2Playable = (cha instanceof L2Playable);

					L2PcInstance src = activeChar.getActingPlayer();

					int radius = getSkillRadius();

					boolean srcInArena = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj == cha)
							continue;

						if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
							continue;

						target = (L2Character) obj;

						if (!target.isDead() && (target != activeChar))
						{
							if (!Util.checkIfInRange(radius, target, activeChar, true))
								continue;

							if (!target.isInFrontOf(activeChar))
								continue;

							if (!GeoData.getInstance().canSeeTarget(activeChar, target))
								continue;

							if (src != null) // caster is L2Playable and exists
							{
								boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);
								if (obj instanceof L2PcInstance)
								{
									L2PcInstance trg = (L2PcInstance) obj;
									if (trg == src)
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;

									if (!srcInArena && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(obj, this))
											continue;
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;

									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (!srcInArena && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(trg, this))
											continue;
									}

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;
								}
							}
							else
							// Skill user is not L2Playable
							{
								if (effectOriginIsL2Playable && // If effect starts at L2Playable and
										!(obj instanceof L2Playable)) // Object is not L2Playable
									continue;
							}

							targetList.add((L2Character) obj);
						}
					}

					if (targetList.size() == 0)
						return null;

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_BEHIND_AREA:
				{
					if ((!(target instanceof L2Attackable || target instanceof L2Playable)) || //   Target is not L2Attackable or L2Playable
							(getCastRange() >= 0 && (target == activeChar || target.isAlikeDead()))) //target is null or self or dead/faking
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					L2Character cha;

					if (getCastRange() >= 0)
					{
						cha = target;

						if (!onlyFirst)
							targetList.add(cha); // Add target to target list
						else
							return new L2Character[] { cha };
					}
					else
						cha = activeChar;

					boolean effectOriginIsL2Playable = (cha instanceof L2Playable);

					L2PcInstance src = activeChar.getActingPlayer();

					int radius = getSkillRadius();

					boolean srcInArena = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj == cha)
							continue;
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
							continue;
						target = (L2Character) obj;

						if (!target.isDead() && (target != activeChar))
						{
							if (!Util.checkIfInRange(radius, obj, activeChar, true))
								continue;

							if (!target.isBehind(activeChar))
								continue;

							if (!GeoData.getInstance().canSeeTarget(activeChar, target))
								continue;

							if (src != null) // caster is L2Playable and exists
							{
								boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);
								if (obj instanceof L2PcInstance)
								{
									L2PcInstance trg = (L2PcInstance) obj;
									if (trg == src)
										continue;
									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;

									if (!srcInArena && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(obj, this))
											continue;
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
										continue;

									if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;

									if (!srcInArena && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;

										if (trg.isInsideZone(L2Zone.FLAG_PEACE))
											continue;

										if (src.getClan() != null && trg.getClan() != null)
										{
											if (src.getClan().getClanId() == trg.getClan().getClanId())
												continue;
										}

										if (!src.checkPvpSkill(trg, this))
											continue;

									}
								}
							}
							else
							// Skill user is not L2Playable
							{
								// If effect starts at L2Playable and object is not L2Playable
								if (effectOriginIsL2Playable && !(obj instanceof L2Playable))
									continue;
							}

							targetList.add(target);
						}
					}

					if (targetList.size() == 0)
						return null;

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA_UNDEAD:
				{
					L2Character cha;
					int radius = getSkillRadius();
					if (getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance) && target.isUndead() && !target.isAlikeDead())
					{
						cha = target;

						if (!onlyFirst)
							targetList.add(cha); // Add target to target list
						else
							return new L2Character[] { cha };
					}
					else
						cha = activeChar;

					for (L2Object obj : cha.getKnownList().getKnownObjects().values())
					{
						if (obj instanceof L2Npc)
							target = (L2Npc) obj;
						else if (obj instanceof L2SummonInstance)
							target = (L2SummonInstance) obj;
						else
							continue;

						if (!GeoData.getInstance().canSeeTarget(activeChar, target))
							continue;

						if (!target.isAlikeDead()) // If target is not dead/fake death and not self
						{
							if (!target.isUndead())
								continue;
							if (!Util.checkIfInRange(radius, cha, obj, true)) // Go to next obj if obj isn't in range
								continue;

							if (!onlyFirst)
								targetList.add((L2Character) obj); // Add obj to target lists
							else
								return new L2Character[] { (L2Character) obj };
						}
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_PARTY:
				{
					if (onlyFirst)
						return new L2Character[] { activeChar };

					targetList.add(activeChar);

					L2PcInstance player = null;

					if (activeChar instanceof L2Summon)
					{
						player = ((L2Summon) activeChar).getOwner();
						targetList.add(player);
					}
					else if (activeChar instanceof L2PcInstance)
					{
						player = (L2PcInstance) activeChar;
						if (activeChar.getPet() != null)
							targetList.add(activeChar.getPet());
					}

					if (activeChar.getParty() != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get a list of Party Members
						List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();

						for (L2PcInstance partyMember : partyList)
						{
							if (player == null || partyMember == null || partyMember == player)
								continue;

							if (player.isInDuel() && player.getDuelId() != partyMember.getDuelId())
								continue;

							//check if allow interference is allowed if player is not on event but target is on event
							if (((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE) || (VIP._started && !Config.VIP_ALLOW_INTERFERENCE))
									&& !player.isGM())
							{
								if ((partyMember._inEventTvT && !player._inEventTvT) || (!partyMember._inEventTvT && player._inEventTvT))
									continue;
								if ((partyMember._inEventCTF && !player._inEventCTF) || (!partyMember._inEventCTF && player._inEventCTF))
									continue;
								if ((partyMember._inEventDM && !player._inEventDM) || (!partyMember._inEventDM && player._inEventDM))
									continue;
								else if ((partyMember._inEventVIP && !player._inEventVIP) || (!partyMember._inEventVIP && player._inEventVIP))
									continue;
							}

							if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
							{
								targetList.add(partyMember);

								if (partyMember.getPet() != null && !partyMember.getPet().isDead())
									targetList.add(partyMember.getPet());
							}
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_PARTY_MEMBER:
				{
					if ((target != null && target == activeChar) || (target != null && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
							|| (target != null && activeChar instanceof L2PcInstance && target instanceof L2Summon && activeChar.getPet() == target)
							|| (target != null && activeChar instanceof L2Summon && target instanceof L2PcInstance && activeChar == target.getPet()))
					{
						if (!target.isDead())
						{
							// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
							return new L2Character[] { target };
						}

						return null;
					}

					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_PARTY_OTHER:
				{
					if (target != null && target != activeChar && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
					{
						if (!target.isDead())
						{
							if (target instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) target;
								switch (getId())
								{
									// FORCE BUFFS may cancel here but there should be a proper condition
									case 426:
										if (!player.isMageClass())
											return new L2Character[] { target };

										return null;
									case 427:
										if (player.isMageClass())
											return new L2Character[] { target };

										return null;
								}
							}
							return new L2Character[] { target };
						}

						return null;
					}

					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_CORPSE_ALLY:
				case TARGET_ALLY:
				{
					if (activeChar instanceof L2Playable)
					{
						int radius = getSkillRadius();
						L2PcInstance player = activeChar.getActingPlayer();
						if (player == null)
							return null;

						L2Clan clan = player.getClan();

						if (player.isInOlympiadMode())
						{
							if (player.getPet() == null)
								return new L2Character[] { player };

							return new L2Character[] { player, player.getPet() };
						}

						if (targetType != SkillTargetType.TARGET_CORPSE_ALLY)
						{
							if (!onlyFirst)
								targetList.add(player);
							else
								return new L2Character[] { player };
						}

						if (activeChar.getPet() != null)
						{
							if ((targetType != SkillTargetType.TARGET_CORPSE_ALLY) && !(activeChar.getPet().isDead()))
								targetList.add(activeChar.getPet());
						}

						if (clan != null)
						{
							// Get all visible objects in a spheric area near the L2Character
							// Get Clan Members
							for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
							{
								if (obj == player || !(obj instanceof L2Playable) || obj.getActingPlayer() == null)
									continue;

								L2PcInstance newTarget = obj.getActingPlayer();

								if ((newTarget.getAllyId() == 0 || newTarget.getAllyId() != player.getAllyId()) && (newTarget.getClan() == null || newTarget.getClanId() != player.getClanId()))
									continue;

								if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || (player.getParty() != null && player.getParty() != newTarget.getParty())))
									continue;

								//check if allow interference is allowed if player is not on event but target is on event
								if (((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE)) && !player.isGM())
								{
									if ((newTarget._inEventTvT && !player._inEventTvT) || (!newTarget._inEventTvT && player._inEventTvT))
										continue;
									if ((newTarget._inEventCTF && !player._inEventCTF) || (!newTarget._inEventCTF && player._inEventCTF))
										continue;
									else if ((newTarget._inEventDM && !player._inEventDM) || (!newTarget._inEventDM && player._inEventDM))
										continue;
								}

								L2Summon pet = newTarget.getPet();
								if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst
										&& ((targetType == SkillTargetType.TARGET_CORPSE_ALLY && pet.isDead()) || (targetType == SkillTargetType.TARGET_ALLY && !pet.isDead())) && player.checkPvpSkill(newTarget, this))
									targetList.add(pet);

								if (targetType == SkillTargetType.TARGET_CORPSE_ALLY)
								{
									if (!newTarget.isDead())
										continue;
									// Siege battlefield resurrect has been made possible for participants
									if (getSkillType() == L2SkillType.RESURRECT)
									{
										if (newTarget.isInsideZone(L2Zone.FLAG_SIEGE) && newTarget.getSiegeState() == 0)
											continue;
									}
								}

								if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
									continue;

								// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
								if (!player.checkPvpSkill(newTarget, this))
									continue;

								if (!onlyFirst)
									targetList.add(newTarget);

								return new L2Character[] { newTarget };

							}
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_ENEMY_ALLY:
				{
					// int charX, charY, charZ, targetX, targetY, targetZ, dx, dy, dz;
					int radius = getSkillRadius();
					L2Character newTarget;

					if (getCastRange() > -1 && target != null)
					{
						newTarget = target;
					}
					else
						newTarget = activeChar;

					if (newTarget != activeChar || isOffensive())
						targetList.add(newTarget);

					for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					{
						if (obj == newTarget || obj == activeChar)
							continue;

						if (obj instanceof L2Attackable)
						{
							if (!obj.isAlikeDead())
							{
								// Don't add this target if this is a PC->PC pvp casting and pvp condition not met
								if (activeChar instanceof L2PcInstance && !((L2PcInstance) activeChar).checkPvpSkill(obj, this))
									continue;

								// check if both attacker and target are L2PcInstances and if they are in same party or clan
								if ((activeChar instanceof L2PcInstance && obj instanceof L2PcInstance)
										&& (((L2PcInstance) activeChar).getClanId() != ((L2PcInstance) obj).getClanId() || (((L2PcInstance) activeChar).getAllyId() != ((L2PcInstance) obj).getAllyId() && ((activeChar.getParty() != null && obj
												.getParty() != null) && activeChar.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID()))))
									continue;

								targetList.add(obj);
							}
						}
					}
					//FIXME: (Noctarius) Added return here to deny fallthrough - is it wished to add more targets?
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_CORPSE_CLAN:
				case TARGET_CLAN:
				{
					if (activeChar instanceof L2Playable)
					{
						int radius = getSkillRadius();
						L2PcInstance player = activeChar.getActingPlayer();
						if (player == null)
							return null;

						L2Clan clan = player.getClan();

						if (player.isInOlympiadMode())
						{
							if (player.getPet() == null)
								return new L2Character[] { player };

							return new L2Character[] { player, player.getPet() };
						}

						if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
						{
							if (!onlyFirst)
								targetList.add(player);
							else
								return new L2Character[] { player };
						}

						if (activeChar.getPet() != null)
						{
							if ((targetType != SkillTargetType.TARGET_CORPSE_CLAN) && !(activeChar.getPet().isDead()))
								targetList.add(activeChar.getPet());
						}

						if (clan != null)
						{
							// Get all visible objects in a spheric area near the L2Character
							// Get Clan Members
							for (L2ClanMember member : clan.getMembers())
							{
								L2PcInstance newTarget = member.getPlayerInstance();

								if (newTarget == null || newTarget == player)
									continue;

								if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || (player.getParty() == null && player.getParty() != newTarget.getParty())))
									continue;

								//check if allow interference is allowed if player is not on event but target is on event
								if (((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE)) && !player.isGM())
								{
									if ((newTarget._inEventTvT && !player._inEventTvT) || (!newTarget._inEventTvT && player._inEventTvT))
										continue;
									if ((newTarget._inEventCTF && !player._inEventCTF) || (!newTarget._inEventCTF && player._inEventCTF))
										continue;
									else if ((newTarget._inEventDM && !player._inEventDM) || (!newTarget._inEventDM && player._inEventDM))
										continue;
								}

								L2Summon pet = newTarget.getPet();
								if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst
										&& ((targetType == SkillTargetType.TARGET_CORPSE_CLAN && pet.isDead()) || (targetType == SkillTargetType.TARGET_CLAN && !pet.isDead())) && player.checkPvpSkill(newTarget, this))
									targetList.add(pet);

								if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
								{
									if (!newTarget.isDead())
										continue;
									if (getSkillType() == L2SkillType.RESURRECT)
									{
										// check for charm of courage and caster being a siege participant, otherwise do not allow resurrection
										// on siege battlefield
										Siege siege = SiegeManager.getInstance().getSiege(newTarget);
										if (siege != null && siege.getIsInProgress())
										{
											// could/should be a more accurate check for siege clans
											if (!newTarget.getCharmOfCourage() || player.getSiegeState() == 0)
												continue;
										}
									}
								}

								if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
									continue;

								// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
								if (!player.checkPvpSkill(newTarget, this))
									continue;

								if (!onlyFirst)
									targetList.add(newTarget);
								else
									return new L2Character[] { newTarget };
							}
						}
					}
					else if (activeChar instanceof L2Npc)
					{
						// for buff purposes, returns one unbuffed friendly mob nearby or mob itself?
						L2Npc npc = (L2Npc) activeChar;
						for (L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
						{
							if (newTarget instanceof L2Npc && ((L2Npc) newTarget).getFactionId() == npc.getFactionId())
							{
								if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
									continue;
								if (((L2Npc) newTarget).getFirstEffect(this) != null)
								{
									targetList.add((L2Npc) newTarget);
									break;
								}
							}
						}
						if (targetList.isEmpty())
						{
							targetList.add(activeChar);
						}
					}

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_CORPSE_PLAYER:
				{
					if (target != null && target.isDead())
					{
						L2PcInstance player = null;

						if (activeChar instanceof L2PcInstance)
							player = (L2PcInstance) activeChar;
						L2PcInstance targetPlayer = null;

						if (target instanceof L2PcInstance)
							targetPlayer = (L2PcInstance) target;
						L2PetInstance targetPet = null;

						if (target instanceof L2PetInstance)
							targetPet = (L2PetInstance) target;

						if (player != null && (targetPlayer != null || targetPet != null))
						{
							boolean condGood = true;

							if (getSkillType() == L2SkillType.RESURRECT)
							{
								// check target is not in a active siege zone
								Siege siege = null;

								if (targetPlayer != null)
									siege = SiegeManager.getInstance().getSiege(targetPlayer);
								else if (targetPet != null)
									siege = SiegeManager.getInstance().getSiege(targetPet);

								if (siege != null && siege.getIsInProgress() && targetPlayer != null && (!targetPlayer.getCharmOfCourage() || player.getSiegeState() == 0))
								{
									condGood = false;
									player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
								}

								if (targetPlayer != null)
								{
									if (targetPlayer.isReviveRequested())
									{
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been
										// proposed.
										condGood = false;
									}
								}
								else if (targetPet != null)
								{
									if (targetPet.getOwner() != player)
									{
										condGood = false;
										player.sendMessage("You are not the owner of this pet");
									}
								}
							}

							if (condGood)
							{
								if (!onlyFirst)
								{
									targetList.add(target);
									return targetList.moveToArray(new L2Character[targetList.size()]);
								}

								return new L2Character[] { target };

							}
						}
					}
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_CORPSE_MOB:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					// Corpse mob only available for half time
					switch (getSkillType())
					{
						case DRAIN:
						case SUMMON:
						{
							if (DecayTaskManager.getInstance().hasDecayTask(target))
							{
								if (DecayTaskManager.getInstance().getRemainingDecayTime(target) < 0.5)
								{
									activeChar.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
									return null;
								}
							}
						}
					}

					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.moveToArray(new L2Character[targetList.size()]);
					}

					return new L2Character[] { target };

				}
				case TARGET_AREA_CORPSE_MOB:
				{
					if ((!(target instanceof L2Attackable)) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
						targetList.add(target);
					else
						return new L2Character[] { target };

					boolean srcInArena = (activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE));
					L2PcInstance src = null;
					if (activeChar instanceof L2PcInstance)
						src = (L2PcInstance) activeChar;
					L2PcInstance trg = null;

					int radius = getSkillRadius();
					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable) || ((L2Character) obj).isDead() || obj == activeChar)
							continue;

						boolean targetInPvP = ((L2Character) obj).isInsideZone(L2Zone.FLAG_PVP) && !((L2Character) obj).isInsideZone(L2Zone.FLAG_SIEGE);

						if (!Util.checkIfInRange(radius, target, obj, true))
							continue;

						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
							continue;

						if (obj instanceof L2PcInstance && src != null)
						{
							trg = (L2PcInstance) obj;

							if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								continue;

							if (trg.isInsideZone(L2Zone.FLAG_PEACE))
								continue;

							if (!srcInArena && !targetInPvP)
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									continue;

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
										continue;
								}

								if (!src.checkPvpSkill(obj, this))
									continue;
							}
						}
						if (obj instanceof L2Summon && src != null)
						{
							trg = ((L2Summon) obj).getOwner();

							if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								continue;

							if (!srcInArena && !targetInPvP)
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									continue;

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
										continue;
								}

								if (!src.checkPvpSkill(trg, this))
									continue;
							}

							if (((L2Summon) obj).isInsideZone(L2Zone.FLAG_PEACE))
								continue;
						}

						targetList.add((L2Character) obj);
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA_CORPSES:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
						targetList.add(target);
					else
						return new L2Character[] { target };

					int radius = getSkillRadius();
					if (activeChar.getKnownList() != null)
					{
						for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
						{
							if (obj == null || !(obj instanceof L2Attackable))
								continue;
							L2Character cha = (L2Character) obj;

							if (!cha.isDead() || !Util.checkIfInRange(radius, target, cha, true))
								continue;

							if (!GeoData.getInstance().canSeeTarget(activeChar, cha))
								continue;

							targetList.add(cha);
						}
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_UNLOCKABLE:
				{
					if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
					{
						// activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.moveToArray(new L2Character[targetList.size()]);
					}

					return new L2Character[] { target };

				}
				case TARGET_ENEMY_SUMMON:
				{
					if (target instanceof L2Summon)
					{
						L2Summon targetSummon = (L2Summon) target;
						if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetSummon && !targetSummon.isDead() && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0)
								|| (targetSummon.getOwner().isInsideZone(L2Zone.FLAG_PVP) && activeChar.isInsideZone(L2Zone.FLAG_PVP)))
							return new L2Character[] { targetSummon };
					}
					return null;
				}
				case TARGET_GATE:
				{
					// Check for null target or any other invalid target
					if (target == null || target.isDead() || !(target instanceof L2DoorInstance))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new L2Character[] { target };
				}
				case TARGET_MOB:
				{
					// Check for null target or any other invalid target
					if (target == null || target.isDead() || !(target instanceof L2Attackable))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new L2Character[] { target };
				}
				case TARGET_KNOWNLIST:
				{
					if (target != null && target.getKnownList() != null)
						for (L2Object obj : target.getKnownList().getKnownObjects().values())
						{
							if (obj instanceof L2Attackable || obj instanceof L2Playable)
								return new L2Character[] { (L2Character) obj };
						}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_INITIATOR:
					if (target != null)
						return new L2Character[] { target };
					else
						return null;
				default:
				{
					if (activeChar instanceof L2PcInstance || _log.isDebugEnabled()) // normally log only player skills errors
						_log.error("Target type of skill Id " + _id + " is not implemented.");
					return null;
				}
			}// end switch
		}
		finally
		{
			targetList.clear();
		}
	}

	// [L2J_JP ADD SANDMAN START]
	public final L2Character[] getAreaTargetList(L2Character activeChar)
	{
		LinkedBunch<L2Character> targetList = new LinkedBunch<L2Character>();
		L2Object target;
		L2PcInstance tgOwner;
		L2Clan acClan;
		L2Clan tgClan;
		L2Party acPt = activeChar.getParty();
		int radius = getSkillRadius();

		if (getCastRange() <= 0 || (getTargetType() == SkillTargetType.TARGET_AURA))
			target = activeChar;
		else
			target = activeChar.getTarget();

		if (target == null || !(target instanceof L2Character))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		if ((getTargetType() == SkillTargetType.TARGET_AREA) && (target.getObjectId() != activeChar.getObjectId()))
		{
			if (!((L2Character) target).isAlikeDead())
				targetList.add((L2Character) target);
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}
		}

		if (!(activeChar instanceof L2Playable))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2Playable)
				{
					if (!(Util.checkIfInRange(radius, target, obj, true)))
						continue;
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
			}
			if (targetList.size() == 0)
				return null;
			return targetList.moveToArray(new L2Character[targetList.size()]);
		}

		if (activeChar instanceof L2PcInstance)
			acClan = ((L2PcInstance) activeChar).getClan();
		else if (activeChar instanceof L2Summon)
			acClan = ((L2Summon) activeChar).getOwner().getClan();
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		if (activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;

				if (obj instanceof L2PcInstance)
				{
					tgClan = ((L2PcInstance) obj).getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();
					tgClan = tgOwner.getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else if (activeChar.isInsideZone(L2Zone.FLAG_STADIUM) || activeChar.isInsideZone(L2Zone.FLAG_PVP) || FourSepulchersManager.getInstance().checkIfInZone(activeChar))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;

				if (obj instanceof L2PcInstance)
				{
					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;

				if (obj instanceof L2MonsterInstance)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
			}
		}

		if (targetList.size() == 0)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		return targetList.moveToArray(new L2Character[targetList.size()]);
	}

	public final L2Character[] getMultiFaceTargetList(L2Character activeChar)
	{
		LinkedBunch<L2Character> targetList = new LinkedBunch<L2Character>();
		L2Object target;
		L2Object FirstTarget;
		L2PcInstance tgOwner;
		L2Clan acClan;
		L2Clan tgClan;
		L2Party acPt = activeChar.getParty();
		int radius = getSkillRadius();

		if (getCastRange() <= 0)
			target = activeChar;
		else
			target = activeChar.getTarget();
		FirstTarget = target;

		if (target == null || !(target instanceof L2Character))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		int newHeading = getNewHeadingToTarget(activeChar, (L2Character) target);

		if (target.getObjectId() != activeChar.getObjectId())
		{
			if (!((L2Character) target).isAlikeDead())
				targetList.add((L2Character) target);
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}
		}

		if (!(activeChar instanceof L2Playable))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2Playable)
				{
					if (!(Util.checkIfInRange(radius, target, obj, true)))
						continue;
					else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) target))
						continue;
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);

				}
			}
			if (targetList.size() == 0)
				return null;
			return targetList.moveToArray(new L2Character[targetList.size()]);
		}

		if (activeChar.getActingPlayer() != null)
			acClan = activeChar.getActingPlayer().getClan();
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		if (activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2PcInstance)
				{
					tgClan = ((L2PcInstance) obj).getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();
					tgClan = tgOwner.getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else if (activeChar.isInsideZone(L2Zone.FLAG_STADIUM) || activeChar.isInsideZone(L2Zone.FLAG_PVP) || FourSepulchersManager.getInstance().checkIfInZone(activeChar))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2PcInstance)
				{
					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2MonsterInstance)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
			}
		}

		if (targetList.size() == 0)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		return targetList.moveToArray(new L2Character[targetList.size()]);
	}

	protected int getNewHeadingToTarget(L2Character caster, L2Character target)
	{
		if (caster == null || target == null)
			return 0;

		double befHeading = Util.convertHeadingToDegree(caster.getHeading());
		if (befHeading > 360)
			befHeading -= 360;

		int dx = caster.getX() - target.getX();
		int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
			dist = 0.01;

		double sin = dy / dist;
		double cos = dx / dist;
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);

		return heading;

	}

	public boolean isBehindFromCaster(int heading, L2Character caster, L2Character target)
	{
		if (caster == null || target == null)
			return true;

		double befHeading = Util.convertHeadingToDegree(heading);
		if (befHeading > 360)
			befHeading -= 360;
		else if (befHeading < 0)
			befHeading += 360;

		int dx = caster.getX() - target.getX();
		int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
			dist = 0.01;

		double sin = dy / dist;
		double cos = dx / dist;
		int newheading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);

		double aftHeading = Util.convertHeadingToDegree(newheading);
		if (aftHeading > 360)
			aftHeading -= 360;
		else if (aftHeading < 0)
			aftHeading += 360;

		double diffHeading = Math.abs(aftHeading - befHeading);
		if (diffHeading > 360)
			diffHeading -= 360;
		else if (diffHeading < 0)
			diffHeading += 360;

		return (diffHeading > 90) && (diffHeading < 270);
	}

	// [L2J_JP ADD SANDMAN END]
	public final L2Character[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}

	public final L2Character getFirstOfTargetList(L2Character activeChar)
	{
		L2Character[] targets;

		targets = getTargetList(activeChar, true);

		if (targets == null || targets.length == 0)
			return null;

		return targets[0];
	}

	public final Func[] getStatFuncs(L2Character player)
	{
		if (!(player instanceof L2Playable) && !(player instanceof L2Attackable))
			return Func.EMPTY_ARRAY;
		if (_funcTemplates == null)
			return Func.EMPTY_ARRAY;
		LinkedBunch<Func> funcs = new LinkedBunch<Func>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.skill = this;
			Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
				funcs.add(f);
		}
		if (funcs.size() == 0)
			return Func.EMPTY_ARRAY;
		return funcs.moveToArray(new Func[funcs.size()]);
	}

	public boolean hasEffects()
	{
		return (_effectTemplates != null && _effectTemplates.length > 0);
	}

	public final void getEffects(L2Character effector, L2Character effected)
	{
		if (_effectTemplates == null)
			return;

		if (!GlobalRestrictions.canCreateEffect(effector, effected, this))
			return;

		boolean skillMastery = Formulas.calcSkillMastery(effector, this);

		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effected;
			env.skill = this;
			env.skillMastery = skillMastery;
			et.getEffect(env);
		}
	}

	public final void getEffects(L2CubicInstance effector, L2Character effected)
	{
		if (_effectTemplates == null)
			return;

		if (!GlobalRestrictions.canCreateEffect(effector.getOwner(), effected, this))
			return;

		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = effector.getOwner();
			//			Disabled until it's really used...
			//			env.cubic = effectorCubic;
			env.target = effected;
			env.skill = this;
			et.getEffect(env);
		}
	}

	public final void getEffectsSelf(L2Character effector)
	{
		if (_effectTemplatesSelf == null)
			return;

		if (!GlobalRestrictions.canCreateEffect(effector, effector, this))
			return;

		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effector;
			env.skill = this;
			et.getEffect(env);
		}
	}

	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[] { f };
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[] { effect };
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[] { effect };
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}

	public final void attach(Condition c)
	{
		Condition old = _preCondition;

		if (old != null)
			_log.fatal("Replaced " + old + " condition with " + c + " condition at skill: " + this);

		_preCondition = c;
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	public String generateUniqueStackType()
	{
		int count = _effectTemplates == null ? 0 : _effectTemplates.length;
		count += _effectTemplatesSelf == null ? 0 : _effectTemplatesSelf.length;

		return _id + "-" + count;
	}

	public float generateStackOrder()
	{
		return getLevel();
	}

	@Override
	public String getFuncOwnerName()
	{
		return getName();
	}

	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return this;
	}

	/**
	 * used for tracking item id in case that item consume cannot be used
	 * 
	 * @return reference item id
	 */
	public final int getReferenceItemId()
	{
		return _refId;
	}

	/**
	 * @return
	 */
	public final int getAfroColor()
	{
		return _afroId;
	}

	public final boolean isBuff()
	{
		if (4360 < getId() && getId() < 4367) // 7s buffs
			return false;

		// TODO: this is a so ugly hax
		switch (getSkillType())
		{
			case BUFF:
			case REFLECT:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case COMBATPOINTHEAL:
				return true;
			default:
				return false;
		}
	}

	public final boolean isHerbEffect()
	{
		return _isHerbEffect;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof L2Skill))
			return false;

		L2Skill skill = (L2Skill) obj;

		return getId() == skill.getId() && getLevel() == skill.getLevel();
	}

	@Override
	public int hashCode()
	{
		return L2System.hash(SkillTable.getSkillUID(this));
	}

	public final int getElementPower()
	{
		return _elementPower;
	}

	public String getAttributeName()
	{
		return _attribute;
	}

	public boolean useShield()
	{
		return _useShield;
	}

	public boolean ignoreShield()
	{
		return _ignoreShield;
	}

	public int[] getTeleportCoords()
	{
		return _teleportCoords;
	}

	public String getRecallType()
	{
		return _recallType;
	}

	public boolean canBeReflected()
	{
		return _canBeReflected;
	}

	public int getHairColorId()
	{
		return _hairColorId;
	}

	public int getHairStyleId()
	{
		return _hairStyleId;
	}

	public int getFaceId()
	{
		return _faceId;
	}

	/**
	 * used for learning skills through skills
	 * 
	 * @return new skill id to learn (if not defined, default 0)
	 */
	public int getNewSkillId()
	{
		return _learnSkillId;
	}

	/**
	 * used for learning skills through skills
	 * 
	 * @return skill lvl to learn (if not defined, default 1)
	 */
	public int getNewSkillLvl()
	{
		return _learnSkillLvl;
	}

	public int getAfterEffectId()
	{
		return _afterEffectId;
	}

	public int getAfterEffectLvl()
	{
		return _afterEffectLvl;
	}
	
	@Override
	public L2Skill getChanceTriggeredSkill(L2Character activeChar)
	{
		if (!getWeaponDependancy(activeChar, false))
			return null;
		
		if (_triggeredSkill == null)
			return this;
		
		return _triggeredSkill.getTriggeredSkill();
	}
	
	@Override
	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}
}

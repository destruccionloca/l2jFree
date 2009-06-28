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
package com.l2jfree.gameserver.model.actor.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.PetDataTable;
import com.l2jfree.gameserver.model.Elementals;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2PetData;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2AirShipInstance;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Calculator;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.templates.item.L2WeaponType;

public class CharStat
{
	protected final static Log	_log	= LogFactory.getLog(CharStat.class.getName());

	// =========================================================
	// Data Field
	protected final L2Character	_activeChar;
	private long				_exp	= 0;
	private int					_sp		= 0;
	private byte				_level	= 1;

	// =========================================================
	// Constructor
	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	// =========================================================
	// Method - Public
	/**
	 * Calculate the new value of the state with modifiers that will be applied
	 * on the targeted L2Character.<BR>
	 * <BR>
	 * 
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object. A
	 * Func object is a mathematic function that permit to calculate the
	 * modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * <BR>
	 * 
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * 
	 * When the calc method of a calculator is launched, each mathematic
	 * function is called according to its priority <B>_order</B>. Indeed, Func
	 * with lowest priority order is executed firsta and Funcs with the same
	 * order are executed in unspecified order. The result of the calculation is
	 * stored in the value property of an Env class instance.<BR>
	 * <BR>
	 * 
	 * @param stat The stat to calculate the new value with modifiers
	 * @param init The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the
	 *            calculation (ex : CON, INT...)
	 * @param skill The L2Skill whose properties will be used in the calculation
	 *            (ex : Level...)
	 * 
	 */
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return init;

		int id = stat.ordinal();

		Calculator c = _activeChar.getCalculators()[id];

		// If no Func object found, no modifier is applied
		if (c == null || c.size() == 0)
			return init;

		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env.player = _activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;

		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be negative)
		if (env.value <= 0)
		{
			switch (stat)
			{
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENCE:
				case POWER_DEFENCE:
				case POWER_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
					env.value = 1;
			}
		}

		return env.value;
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	/**
	 * Return the Accuracy (base+modifier) of the L2Character in function of the
	 * Weapon Expertise Penalty.
	 */
	public int getAccuracy()
	{
		if (_activeChar == null)
			return 0;

		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / _activeChar.getWeaponExpertisePenalty());
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to
	 * get proper animations.
	 */
	public final float getAttackSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;

		return (float) ((1.1) * getPAtkSpd() / _activeChar.getTemplate().getBasePAtkSpd());
	}

	/** Return the CON of the L2Character (base+modifier). */
	public final int getCON()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_CON, _activeChar.getTemplate().getBaseCON(), null, null);
	}

	/** Return the Critical Hit rate (base+modifier) of the L2Character. */
	public int getCriticalHit(L2Character target)
	{
		int criticalHit = (int) calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().getBaseCritRate(), target, null);

		// Set a cap of Critical Hit at ALT_PCRITICAL_CAP
		if (criticalHit > Config.ALT_PCRITICAL_CAP)
			criticalHit = Config.ALT_PCRITICAL_CAP;

		return criticalHit;
	}

	/** Return the DEX of the L2Character (base+modifier). */
	public final int getDEX()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_DEX, _activeChar.getTemplate().getBaseDEX(), null, null);
	}

	/** Return the Attack Evasion rate (base+modifier) of the L2Character. */
	public int getEvasionRate(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		int val = (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / _activeChar.getArmourExpertisePenalty());

		return val;
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExp(long value)
	{
		_exp = value;
	}

	/** Return the INT of the L2Character (base+modifier). */
	public int getINT()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_INT, _activeChar.getTemplate().getBaseINT(), null, null);
	}

	public byte getLevel()
	{
		return _level;
	}

	public void setLevel(byte value)
	{
		_level = value;
	}

	/** Return the Magical Attack range (base+modifier) of the L2Character. */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);

		if (_activeChar == null)
			return 1;

		return _activeChar.getTemplate().getBaseAtkRange();
	}

	public int getMaxCp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().getBaseCpMax(), null, null);
	}

	public int getMaxHp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().getBaseHpMax(), null, null);
	}

	public int getMaxMp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().getBaseMpMax(), null, null);
	}

	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in
	 * function of abnormal effects in progress.<BR>
	 * <BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * <BR>
	 * 
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 */
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		float bonusAtk = 1;
		if (_activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;

		// Get the base MAtk of the L2Character
		double attack = _activeChar.getTemplate().getBaseMAtk() * bonusAtk;

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		/*
		 * Stats stat = skill == null ? null : skill.getStat();
		 * 
		 * if (stat != null) { switch (stat) { case AGGRESSION: attack +=
		 * _activeChar.getTemplate().getBaseAggression(); break; case BLEED:
		 * attack += _activeChar.getTemplate().getBaseBleed(); break; case
		 * POISON: attack += _activeChar.getTemplate().getBasePoison(); break;
		 * case STUN: attack += _activeChar.getTemplate().getBaseStun(); break;
		 * case ROOT: attack += _activeChar.getTemplate().getBaseRoot(); break;
		 * case MOVEMENT: attack += _activeChar.getTemplate().getBaseMovement();
		 * break; case CONFUSION: attack +=
		 * _activeChar.getTemplate().getBaseConfusion(); break; case SLEEP:
		 * attack += _activeChar.getTemplate().getBaseSleep(); break; case FIRE:
		 * attack += _activeChar.getTemplate().getBaseFire(); break; case WIND:
		 * attack += _activeChar.getTemplate().getBaseWind(); break; case WATER:
		 * attack += _activeChar.getTemplate().getBaseWater(); break; case
		 * EARTH: attack += _activeChar.getTemplate().getBaseEarth(); break;
		 * case HOLY: attack += _activeChar.getTemplate().getBaseHoly(); break;
		 * case DARK: attack += _activeChar.getTemplate().getBaseDark(); break;
		 * } }
		 */

		// Add the power of the skill to the attack effect
		if (skill != null)
			attack += skill.getPower();

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getMAtkSpd()
	{
		if (_activeChar == null)
			return 1;

		float bonusSpdAtk = 1;
		if (_activeChar.isChampion())
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;

		int val = (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().getBaseMAtkSpd() * bonusSpdAtk, null, null);

		return val;
	}

	/** Return the Magic Critical Hit rate (base+modifier) of the L2Character. */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		double mrate = calcStat(Stats.MCRITICAL_RATE, _activeChar.getTemplate().getBaseMCritRate(), target, skill);

		// Set a cap of Critical Hit at ALT_MCRITICAL_CAP
		if (mrate > Config.ALT_MCRITICAL_CAP)
			mrate = Config.ALT_MCRITICAL_CAP;

		return (int) mrate;
	}

	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in
	 * function of abnormal effects in progress.<BR>
	 * <BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * <BR>
	 * 
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 */
	public int getMDef(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		// Get the base MAtk of the L2Character
		double defence = _activeChar.getTemplate().getBaseMDef();

		// Calculate modifier for Raid Bosses
		if (_activeChar.isRaid())
			defence *= Config.RAID_MDEFENCE_MULTIPLIER;

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}

	/** Return the MEN of the L2Character (base+modifier). */
	public final int getMEN()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_MEN, _activeChar.getTemplate().getBaseMEN(), null, null);
	}

	public float getMovementSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;

		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).isMounted())
		{
			L2PetData stats = PetDataTable.getInstance().getPetData(((L2PcInstance) _activeChar).getMountNpcId(), ((L2PcInstance) _activeChar).getMountLevel());
			if (stats != null)
				return getRunSpeed() * 1f / stats.getPetSpeed();
		}

		return getRunSpeed() * 1f / _activeChar.getTemplate().getBaseRunSpd();
	}

	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the
	 * L2Character in function of the movement type.
	 */
	public final float getMoveSpeed()
	{
		if (_activeChar == null)
			return 1;

		// TODO: Merge this
		if (_activeChar instanceof L2BoatInstance)
			return ((L2BoatInstance) _activeChar).boatSpeed;
		if (_activeChar instanceof L2AirShipInstance)
			return ((L2AirShipInstance) _activeChar).boatSpeed;

		if (_activeChar.isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	/** Return the MReuse rate (base+modifier) of the L2Character. */
	public final double getMReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().getBaseMReuseRate(), null, skill);
	}

	/** Return the PReuse rate (base+modifier) of the L2Character. */
	public final double getPReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		return calcStat(Stats.PHYS_REUSE_RATE, _activeChar.getTemplate().getBaseMReuseRate(), null, skill);
	}

	/** Return the PAtk (base+modifier) of the L2Character. */
	public int getPAtk(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		float bonusAtk = 1;
		if (_activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;

		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().getBasePAtk() * bonusAtk, target, null);
	}

	/** Return the PAtk Modifier against animals. */
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}

	/** Return the PAtk Modifier against dragons. */
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}

	/** Return the PAtk Modifier against insects. */
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}

	/** Return the PAtk Modifier against monsters. */
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}

	/** Return the PAtk Modifier against plants. */
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}

	/** Return the PAtk Modifier against giants. */
	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}

	/** Return the PAtk Modifier against magic creatures. */
	public final double getPAtkMagic(L2Character target)
	{
		return calcStat(Stats.PATK_MAGIC, 1, target, null);
	}

	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getPAtkSpd()
	{
		if (_activeChar == null)
			return 1;

		float bonusSpdAtk = 1;
		if (_activeChar.isChampion())
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;

		int val = (int) (calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().getBasePAtkSpd() * bonusSpdAtk, null, null));

		return val;
	}

	/** Return the PDef Modifier against animals. */
	public final double getPDefAnimals(L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}

	/** Return the PDef Modifier against dragons. */
	public final double getPDefDragons(L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}

	/** Return the PDef Modifier against insects. */
	public final double getPDefInsects(L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}

	/** Return the PDef Modifier against monsters. */
	public final double getPDefMonsters(L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}

	/** Return the PDef Modifier against plants. */
	public final double getPDefPlants(L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}

	/** Return the PDef Modifier against giants. */
	public final double getPDefGiants(L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}

	/** Return the PDef Modifier against magic creatures. */
	public final double getPDefMagic(L2Character target)
	{
		return calcStat(Stats.PDEF_MAGIC, 1, target, null);
	}

	/** Return the PDef (base+modifier) of the L2Character. */
	public int getPDef(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.POWER_DEFENCE, (_activeChar.isRaid()) ? _activeChar.getTemplate().getBasePDef() * Config.RAID_PDEFENCE_MULTIPLIER : _activeChar.getTemplate().getBasePDef(), target, null);
	}

	/** Return the Physical Attack range (base+modifier) of the L2Character. */
	public final int getPhysicalAttackRange()
	{
		if (_activeChar == null)
			return 1;

		// Polearm handled here for now. Basically L2PcInstance could have a function
		// similar to FuncBowAtkRange and NPC are defined in DP.
		L2Weapon weaponItem = _activeChar.getActiveWeaponItem();
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.POLE)
			return (int) calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().getBaseAtkRange(), null, null);
	}

	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the
	 * Armour Expertise Penalty.
	 */
	public int getRunSpeed()
	{
		if (_activeChar == null)
			return 1;

		// err we should be adding TO the persons run speed
		// not making it a constant
		double baseRunSpd = _activeChar.getTemplate().getBaseRunSpd();
		if (_activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) _activeChar;
			if (player.isMounted())
			{
				L2PetData stats = PetDataTable.getInstance().getPetData(player.getMountNpcId(), player.getMountLevel());
				if (stats != null)
					baseRunSpd = stats.getPetSpeed();
			}
		}
		int val = (int) (calcStat(Stats.RUN_SPEED, baseRunSpd, null, null) * Config.RATE_RUN_SPEED);

		return val;
	}

	/** Return the ShieldDef rate (base+modifier) of the L2Character. */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}

	public int getSp()
	{
		return _sp;
	}

	public void setSp(int value)
	{
		_sp = value;
	}

	/** Return the STR of the L2Character (base+modifier). */
	public final int getSTR()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_STR, _activeChar.getTemplate().getBaseSTR(), null, null);
	}

	/** Return the WalkSpeed (base+modifier) of the L2Character. */
	public int getWalkSpeed()
	{
		if (_activeChar == null)
			return 1;

		if (_activeChar instanceof L2PcInstance)
		{
			return (getRunSpeed() * 70) / 100;
		}

		return (int) calcStat(Stats.WALK_SPEED, _activeChar.getTemplate().getBaseWalkSpd(), null, null);
	}

	/** Return the WIT of the L2Character (base+modifier). */
	public final int getWIT()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_WIT, _activeChar.getTemplate().getBaseWIT(), null, null);
	}

	/** Return the mpConsume. */
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;

		int mpconsume = skill.getMpConsume();

		if (!Config.ALT_DANCE_MP_CONSUME && (skill.isDance() || skill.isSong()) && _activeChar != null)
		{
			int count = _activeChar.getDanceCount(skill.isDance(), skill.isSong());
			if (count > 0)
				mpconsume += (count * skill.getNextDanceMpCost());
		}

		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}

	/** Return the mpInitialConsume. */
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;

		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}

	public byte getAttackElement()
	{
		L2ItemInstance weaponInstance = _activeChar.getActiveWeaponInstance();
		// 1st order - weapon element
		if (weaponInstance != null && weaponInstance.getAttackElementType() >= 0)
			return weaponInstance.getAttackElementType();
		
		// temp fix starts
		int maxValue = 0;
		byte returnAttribute = -2;
		
		for (byte attribute = 0; attribute < 6; attribute++)
		{
			final int attackElementValue = getAttackElementValue(attribute);
			
			if (attackElementValue > maxValue)
			{
				maxValue = attackElementValue;
				returnAttribute = attribute;
			}
		}
		
		return returnAttribute;
		// temp fix ends
		
		// uncomment me once deadlocks in getAllEffects() fixed
		// return _activeChar.getElementIdFromEffects();
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		switch (attackAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_POWER, _activeChar.getTemplate().getBaseFire(), null, null);
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_POWER, _activeChar.getTemplate().getBaseWater(), null, null);
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_POWER, _activeChar.getTemplate().getBaseWind(), null, null);
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_POWER, _activeChar.getTemplate().getBaseEarth(), null, null);
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_POWER, _activeChar.getTemplate().getBaseHoly(), null, null);
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_POWER, _activeChar.getTemplate().getBaseDark(), null, null);
			default:
				return 0;
		}
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		switch (defenseAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_RES, _activeChar.getTemplate().baseFireRes, null, null);
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_RES, _activeChar.getTemplate().baseWaterRes, null, null);
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_RES, _activeChar.getTemplate().baseWindRes, null, null);
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_RES, _activeChar.getTemplate().baseEarthRes, null, null);
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_RES, _activeChar.getTemplate().baseHolyRes, null, null);
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_RES, _activeChar.getTemplate().baseDarkRes, null, null);
			default:
				return 0;
		}
	}
}

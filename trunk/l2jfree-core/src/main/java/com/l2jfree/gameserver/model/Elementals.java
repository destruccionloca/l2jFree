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

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.funcs.FuncAdd;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;

public final class Elementals
{
	private final FuncOwner		FUNC_OWNER			= new FuncOwner() {
														@Override
														public String getFuncOwnerName()
														{
															return null;
														}

														@Override
														public L2Skill getFuncOwnerSkill()
														{
															return null;
														}
													};

	private ElementalStatBoni	_boni				= null;

	public final static byte	NONE				= -1;
	public final static byte	FIRE				= 0;
	public final static byte	WATER				= 1;
	public final static byte	WIND				= 2;
	public final static byte	EARTH				= 3;
	public final static byte	HOLY				= 4;
	public final static byte	DARK				= 5;

	public final static int		ENCHANT_CHANCE		= 50;

	public final static int		FIRST_WEAPON_BONUS	= 20;
	public final static int		NEXT_WEAPON_BONUS	= 5;
	public final static int		ARMOR_BONUS			= 6;

	public final static int[]	WEAPON_VALUES		= { 0, // Level 1
			25, // Level 2
			75, // Level 3
			150, // Level 4
			175, // Level 5
			225, // Level 6
			300, // Level 7
			325, // Level 8
			375, // Level 9
			450, // Level 10
			Integer.MAX_VALUE						// TODO: Higher stones
													};

	public final static int[]	ARMOR_VALUES		= { 0, // Level 1
			6, // Level 2
			15, // Level 3
			30, // Level 4
			36, // Level 5
			45, // Level 6
			60, // Level 7
			66, // Level 8
			75, // Level 9
			90, // Level 10
			Integer.MAX_VALUE						// TODO: Higher stones
													};

	public final static int[]	STONES				= { 9546, 9547, 9549, 9548, 9551, 9550 };

	public final static int[]	CRYSTALS			= { 9552, 9553, 9555, 9554, 9557, 9556 };

	public final static int[]	JEWELS				= { 9558, 9559, 9561, 9560, 9563, 9562 };

	public final static int[]	ENERGIES			= { 9564, 9565, 9567, 9566, 9569, 9568 };

	private byte				_element			= NONE;
	private int					_value				= 0;

	public byte getElement()
	{
		return _element;
	}

	public void setElement(byte type)
	{
		_element = type;
		_boni.setElement(type);
	}

	public int getValue()
	{
		return _value;
	}

	public void setValue(int val)
	{
		_value = val;
		_boni.setValue(val);
	}

	public static String getElementName(byte element)
	{
		switch (element)
		{
			case FIRE:
				return "Fire";
			case WATER:
				return "Water";
			case WIND:
				return "Wind";
			case EARTH:
				return "Earth";
			case DARK:
				return "Dark";
			case HOLY:
				return "Holy";
		}
		return "None";
	}

	public static byte getElementId(String name)
	{
		String tmp = name.toLowerCase();
		if (tmp.equals("fire"))
			return FIRE;
		if (tmp.equals("water"))
			return WATER;
		if (tmp.equals("wind"))
			return WIND;
		if (tmp.equals("earth"))
			return EARTH;
		if (tmp.equals("dark"))
			return DARK;
		if (tmp.equals("holy"))
			return HOLY;
		return NONE;
	}

	public static byte getOppositeElement(byte element)
	{
		return (byte) ((element % 2 == 0) ? (element + 1) : (element - 1));
	}

	@Override
	public String toString()
	{
		return getElementName(_element) + " +" + _value;
	}

	public Elementals(byte type, int value)
	{
		_element = type;
		_value = value;
		_boni = new ElementalStatBoni(_element, _value);
	}

	public class ElementalStatBoni
	{
		private byte				_elementalType;
		private int					_elementalValue;
		private boolean				_active;
		private boolean				_toPet	= false;
		private L2SummonInstance	_pet	= null;

		public ElementalStatBoni(byte type, int value)
		{
			_elementalType = type;
			_elementalValue = value;
			_active = false;
		}

		public void applyBonus(L2PcInstance player, boolean isArmor)
		{
			// make sure the bonuses are not applied twice..
			if (_active)
				return;

			double petPower = 0;
			double tempPower = _elementalValue;
			if (!isArmor && player.getPet() != null && player.getPet() instanceof L2SummonInstance && !player.getPet().isDead() && player.getExpertisePenalty() == 0) // grade
			// penalty
			{
				_pet = (L2SummonInstance) player.getPet();
				_toPet = true;
				petPower = (int) (_elementalValue * 0.8);
				tempPower *= 0.2;
			}

			switch (_elementalType)
			{
				case FIRE:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.FIRE_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.FIRE_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.FIRE_POWER, 0x40, FUNC_OWNER, petPower, null));
					}
					break;
				case WATER:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.WATER_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.WATER_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.WATER_POWER, 0x40, FUNC_OWNER, tempPower, null));
					}
					break;
				case WIND:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.WIND_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.WIND_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.WIND_POWER, 0x40, FUNC_OWNER, tempPower, null));
					}
					break;
				case EARTH:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.EARTH_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.EARTH_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.EARTH_POWER, 0x40, FUNC_OWNER, tempPower, null));
					}
					break;
				case DARK:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.DARK_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.DARK_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.DARK_POWER, 0x40, FUNC_OWNER, tempPower, null));
					}
					break;
				case HOLY:
					if (isArmor)
						((L2Character) player).addStatFunc(new FuncAdd(Stats.HOLY_VULN, 0x40, FUNC_OWNER, tempPower, null));
					else
					{
						((L2Character) player).addStatFunc(new FuncAdd(Stats.HOLY_POWER, 0x40, FUNC_OWNER, tempPower, null));
						if (_toPet)
							_pet.addStatFunc(new FuncAdd(Stats.HOLY_POWER, 0x40, FUNC_OWNER, tempPower, null));
					}
					break;
			}

			_active = true;
		}

		public void removeBonus(L2PcInstance player)
		{
			// make sure the bonuses are not removed twice
			if (!_active)
				return;

			((L2Character) player).removeStatsOwner(FUNC_OWNER);
			if (_pet != null)
			{
				_pet.removeStatsOwner(FUNC_OWNER);
				_pet = null;
				_toPet = false;
			}
			_active = false;
		}

		public void setValue(int val)
		{
			_elementalValue = val;
		}

		public void setElement(byte type)
		{
			_elementalType = type;
		}
	}

	/**
	 * Applies the bonuses to the player.
	 * 
	 * @param player
	 */
	public void applyBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.applyBonus(player, isArmor);
	}

	/**
	 * Removes the elemetal bonuses from the player.
	 * 
	 * @param player
	 */
	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);
	}

	/**
	 * Update the elemetal bonuses from the player.
	 * 
	 * @param player
	 */
	public void updateBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.removeBonus(player);
		_boni.applyBonus(player, isArmor);

	}
}

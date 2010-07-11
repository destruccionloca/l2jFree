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

import java.util.List;

import javolution.util.FastTable;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public final class L2EnchantSkillLearn
{
	private final int					_id;
	private final int					_baseLvl;
	
	@SuppressWarnings("unchecked")
	private List<EnchantSkillDetail>[]	_enchantDetails	= new FastTable[0];
	
	public L2EnchantSkillLearn(int id, int baseLvl)
	{
		_id = id;
		_baseLvl = baseLvl;
	}
	
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return _baseLvl;
	}
	
	@SuppressWarnings("unchecked")
	public void addEnchantDetail(EnchantSkillDetail esd)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantType(esd.getLevel());
		
		if (enchantType < 0)
		{
			throw new IllegalArgumentException("Skill enchantments should have level higher then 100");
		}
		
		if (enchantType >= _enchantDetails.length)
		{
			List<EnchantSkillDetail>[] newArray = new FastTable[enchantType + 1];
			System.arraycopy(_enchantDetails, 0, newArray, 0, _enchantDetails.length);
			_enchantDetails = newArray;
			_enchantDetails[enchantType] = new FastTable<EnchantSkillDetail>();
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(esd.getLevel());
		_enchantDetails[enchantType].add(index, esd);
	}
	
	public List<EnchantSkillDetail>[] getEnchantRoutes()
	{
		return _enchantDetails;
	}
	
	public EnchantSkillDetail getEnchantSkillDetail(int level)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantType(level);
		if (enchantType < 0 || enchantType >= _enchantDetails.length)
		{
			return null;
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(level);
		if (index < 0 || index >= _enchantDetails[enchantType].size())
		{
			return null;
		}
		return _enchantDetails[enchantType].get(index);
	}
	
	public static int getEnchantIndex(int level)
	{
		return (level % 100) - 1;
	}
	
	public static int getEnchantType(int level)
	{
		return ((level - 1) / 100) - 1;
	}
	
	public static class EnchantSkillDetail
	{
		private final int		_level;
		private final int		_adenaCost;
		private final int		_expCost;
		private final int		_spCost;
		private final int		_minSkillLevel;
		private final byte[]	_rates	= new byte[10];
		
		public EnchantSkillDetail(int lvl, int minSkillLvl, int adenaCost, int expCost, int spCost, byte rate76, byte rate77, byte rate78, byte rate79,
				byte rate80, byte rate81, byte rate82, byte rate83, byte rate84, byte rate85)
		{
			_level = lvl;
			_minSkillLevel = minSkillLvl;
			_adenaCost = adenaCost;
			_expCost = expCost;
			_spCost = spCost;
			_rates[0] = rate76;
			_rates[1] = rate77;
			_rates[2] = rate78;
			_rates[3] = rate79;
			_rates[4] = rate80;
			_rates[5] = rate81;
			_rates[6] = rate82;
			_rates[7] = rate83;
			_rates[8] = rate84;
			_rates[9] = rate85;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public int getMinSkillLevel()
		{
			return _minSkillLevel;
		}
		
		public int getExpCost()
		{
			return _expCost;
		}
		
		public int getAdenaCost()
		{
			return _adenaCost;
			// FIXME 1.4.0
			//return _expCost / SkillTreeTable.ADENA_XP_DIV;
		}
		
		public int getSpCost()
		{
			return _spCost;
		}
		
		public byte getRate(L2PcInstance ply)
		{
			if (ply.getLevel() < 76)
				return 0;
			
			return _rates[ply.getLevel() - 76];
		}
	}
}
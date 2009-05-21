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
package net.sf.l2j.gameserver.templates;

import net.sf.l2j.gameserver.model.L2ItemInstance;

/**
 * This class contains L2ItemInstance<BR>
 * Use to sort L2ItemInstance of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI> 
 * @version $Revision: 1.7.2.2.2.5 $ $Date: 2005/04/06 18:25:18 $
 */
public class L2WarehouseItem
{
	
	private L2Item _item;
	private int _object;
	private int _count;
	private int _owner;
	private int _enchant;
	private int _grade;
	private boolean _isAugmented;
	private int _augmentationId;
	private int _manaLeft;

	private int ae_enchantLvl;
	private int ae_enchantElement;
	private int ae_enchantVal;
	private int ad_fire;
	private int ad_water;
	private int ad_earth;
	private int ad_wind;
	private int ad_holy;
	private int ad_unholy;

	private int _type1;
	private int _type2;

	public L2WarehouseItem(L2ItemInstance item)
	{
		_item = item.getItem();
		_object = item.getObjectId();
		_count = item.getCount();
		_owner = item.getOwnerId();
		_enchant = item.getEnchantLevel();
		_grade = item.getItem().getItemGrade();
		if (item.isAugmented())
		{
			_isAugmented = true;
			_augmentationId = item.getAugmentation().getAugmentationId();
		}
		else
			_isAugmented = false;
		_manaLeft = item.getMana();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();

		ae_enchantElement = item.getAttackAttrElement();
		ae_enchantVal = item.getAttackAttrElementVal();
		ad_fire = item.getDefAttrFire();
		ad_water = item.getDefAttrWater();
		ad_earth = item.getDefAttrEarth();
		ad_wind = item.getDefAttrWind();
		ad_holy = item.getDefAttrHoly();
		ad_unholy = item.getDefAttrUnholy();
	}
	
	/**
	 * Returns the item.
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _item;
	}
	
	/**
	 * Returns the unique objectId
	 * @return int
	 */
	public final int getObjectId()
	{
		return _object;
	}
	
	/**
	 * Returns the owner
	 * @return int
	 */
	public final int getOwnerId()
	{
		return _owner;
	}
	
	/**
	 * Returns the count
	 * @return int
	 */
	public final int getCount()
	{
		return _count;
	}
	
	/**
	 * Returns the first type
	 * @return int
	 */
	public final int getType1()
	{
		return _item.getType1();
	}
	
	/**
	 * Returns the second type
	 * @return int
	 */
	public final int getType2()
	{
		return _item.getType2();
	}
	
	/**
	 * Returns the second type
	 * @return int
	 */
	public final Enum getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Returns the ItemId
	 * @return int
	 */
	public final int getItemId()
	{
		return _item.getItemId();
	}
	
	/**
	 * Returns the part of body used with this item
	 * @return int
	 */
	public final int getBodyPart()
	{
		return _item.getBodyPart();
	}
	
	/**
	 * Returns the enchant level
	 * @return int
	 */
	public final int getEnchantLevel()
	{
		return _enchant;
	}
	
	/**
	 * Returns the item grade
	 * @return int
	 */
	public final int getItemGrade()
	{
		return _grade;
	}
	
	/**
	 * Returns true if it is a weapon
	 * @return boolean
	 */
	public final boolean isWeapon()
	{
		return (_item instanceof L2Weapon);
	}
	
	/**
	 * Returns true if it is an armor
	 * @return boolean
	 */
	public final boolean isArmor()
	{
		return (_item instanceof L2Armor);
	}
	
	/**
	 * Returns true if it is an EtcItem
	 * @return boolean
	 */
	public final boolean isEtcItem()
	{
		return (_item instanceof L2EtcItem);
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	public boolean isAugmented()
	{
		return _isAugmented;
	}
	
	public int getAugmentationId()
	{
		return _augmentationId;
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 * @deprecated beware to use getItemName() instead because getName() is final in L2Object and could not be overriden! Allover L2Object.getName() may return null!
	 */
	public String getName()
	{
		return _item.getName();
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	public String toString()
	{
		return _item.toString();
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public int getManaLeft()
	{
		return _manaLeft;
	}

	public int getAttackAttrElement()
	{
		return ae_enchantElement;
	}

	public int getAttackAttrElementVal()
	{
		return ae_enchantVal;
	}

	public int getDefAttrFire()
	{
		return ad_fire;
	}

	public int getDefAttrWater()
	{
		return ad_water;
	}

	public int getDefAttrEarth()
	{
		return ad_earth;
	}

	public int getDefAttrWind()
	{
		return ad_wind;
	}

	public int getDefAttrHoly()
	{
		return ad_holy;
	}

	public int getDefAttrUnholy()
	{
		return ad_unholy;
	}
}

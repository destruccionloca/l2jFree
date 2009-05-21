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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;

/**
 * 
 *
 * @author Luno & Psychokiller1888
 */
public final class L2ArmorSet
{
	private final int _chest;
	private final int _legs;
	private final int _head;
	private final int _gloves;
	private final int _feet;
	private final int _skillId;
	private final int _skillLvl;

	private final int _shield;
	private final int _shieldSkillId;

	private final int _enchant6Skill;

	private final int _mwork_chest;
	private final int _mwork_legs;
	private final int _mwork_head;
	private final int _mwork_gloves;
	private final int _mwork_feet;
	private final int _mwork_shield;

	public L2ArmorSet(int chest, int legs, int head, int gloves, int feet, int skillId, int skill_lvl, int shield, int shieldSkillId, int enchant6Skill, int mwork_chest, int mwork_legs, int mwork_head, int mwork_gloves, int mwork_feet, int mwork_shield)
	{
		_chest = chest;
		_legs  = legs;
		_head  = head;
		_gloves = gloves;
		_feet  = feet;
		_skillId = skillId;
		_skillLvl = skill_lvl;

		_shield = shield;
		_shieldSkillId = shieldSkillId;
		
		_enchant6Skill = enchant6Skill;
		
		_mwork_chest = mwork_chest;
		_mwork_legs  = mwork_legs;
		_mwork_head  = mwork_head;
		_mwork_gloves = mwork_gloves;
		_mwork_feet  = mwork_feet;
		_mwork_shield = mwork_shield;
	}
	/**
	 * Checks if player have equiped all items from set (not checking shield)
	 * @param player whose inventory is being checked
	 * @return True if player equips whole set
	 */
	public boolean containAll(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;

		if (legsItem != null)
			legs = legsItem.getItemId();
		if (headItem != null)
			head = headItem.getItemId();
		if (glovesItem != null)
			gloves = glovesItem.getItemId();
		if (feetItem != null)
			feet = feetItem.getItemId();
		
		return containAll(_chest,legs,head,gloves,feet);
	}

	public boolean containAll(int chest, int legs, int head, int gloves, int feet)
	{
		if ((_chest != 0 && _chest != chest) && (_mwork_chest == 0 || chest != _mwork_chest))
			return false;

		if ((_legs != 0 && _legs != legs) && (_mwork_legs == 0 || legs != _mwork_legs))
			return false;

		if ((_head != 0 && _head != head) && (_mwork_head == 0 || head != _mwork_head))
			return false;

		if ((_gloves != 0 && _gloves != gloves) && (_mwork_gloves ==0 || gloves != _mwork_gloves))
			return false;

		return !((_feet != 0 && _feet != feet) && (_mwork_feet == 0 || feet != _mwork_feet));
	}

	public boolean containItem(int slot, int itemId)
	{
		switch (slot)
		{
		case Inventory.PAPERDOLL_CHEST:
			return (_chest == itemId || _mwork_chest == itemId);

		case Inventory.PAPERDOLL_LEGS:
			return (_legs == itemId || _mwork_legs == itemId);

		case Inventory.PAPERDOLL_HEAD:
			return (_head == itemId || _mwork_head == itemId);

		case Inventory.PAPERDOLL_GLOVES:
			return (_gloves == itemId || _mwork_gloves == itemId);

		case Inventory.PAPERDOLL_FEET:
			return (_feet == itemId || _mwork_feet == itemId);
		
		default:
			return false;
		}
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public int getSkillLvl()
	{
		return _skillLvl;
	}

	public boolean containShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance shieldItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return shieldItem != null && shieldItem.getItemId() == _shield;
	}

	public boolean containShield(int shield_id)
	{
		return _shield != 0 && ((_shield == shield_id || _mwork_shield == shield_id));
	}

	public int getShieldSkillId()
	{
		return _shieldSkillId;
	}

	public int getEnchant6skillId()
	{
		return _enchant6Skill;
	}

	/**
	 * Checks if all parts of set are enchanted to +6 or more
	 * @param player
	 * @return 
	 */
	public boolean isEnchanted6(L2PcInstance player)
	{
		 // Player don't have full set
		if (!containAll(player))
			return false;
		
		Inventory inv = player.getInventory();
		
		L2ItemInstance chestItem  = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);

		if (chestItem != null && chestItem.getEnchantLevel() < 6)
			return false;

		if (_legs != 0 && legsItem != null && legsItem.getEnchantLevel() < 6)
			return false;

		if (_gloves != 0 && glovesItem != null && glovesItem.getEnchantLevel() < 6)
			return false;

		if (_head != 0 && headItem != null && headItem.getEnchantLevel() < 6)
			return false;

		return !(_feet != 0 && feetItem != null && feetItem.getEnchantLevel() < 6);
	}
}

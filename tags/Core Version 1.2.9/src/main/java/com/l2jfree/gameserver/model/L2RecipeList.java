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

import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * This class describes a Recipe used by Dwarf to craft Item. All L2RecipeList
 * are made of L2RecipeInstance (1 line of the recipe : Item-Quantity needed).
 */
public class L2RecipeList
{
	/**
	 * The table containing all L2RecipeInstance (1 line of the recipe :
	 * Item-Quantity needed) of the L2RecipeList
	 */
	private L2RecipeInstance[]	_recipes;

	/** The Identifier of the Instance */
	private int					_id;

	/** The crafting level needed to use this L2RecipeList */
	private int					_level;

	/** The Identifier of the L2RecipeList */
	private int					_recipeId;

	/** The name of the L2RecipeList */
	private String				_recipeName;

	/** The crafting succes rate when using the L2RecipeList */
	private int					_successRate;

	/** The ID of the MasterWork Item (-1 if none) */
	private int					_masterwork;

	/** The crafting MP cost of this L2RecipeList */
	private int					_mpCost;

	/** The Identifier of the Item crafted with this L2RecipeList */
	private int					_itemId;

	/** The quantity of Item crafted when using this L2RecipeList */
	private int					_count;

	/** If this a common or a dwarven recipe */
	private boolean				_isDwarvenRecipe;

	/**
	 * Constructor of L2RecipeList (create a new Recipe).
	 */
	public L2RecipeList(int id, int level, int recipeId, String recipeName, int successRate, int masterwork, int mpCost, int itemId, int count,
			boolean isDwarvenRecipe)
	{
		_id = id;
		_recipes = new L2RecipeInstance[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_masterwork = masterwork;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_isDwarvenRecipe = isDwarvenRecipe;
	}

	/**
	 * Add a L2RecipeInstance to the L2RecipeList (add a line Item-Quantity
	 * needed to the Recipe).
	 */
	public void addRecipe(L2RecipeInstance recipe)
	{
		int len = _recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}

	/**
	 * Return the Identifier of the Instance.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Return the crafting level needed to use this L2RecipeList.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * Return the Identifier of the L2RecipeList.
	 */
	public int getRecipeId()
	{
		return _recipeId;
	}

	/**
	 * Return the name of the L2RecipeList.
	 */
	public String getRecipeName()
	{
		return _recipeName;
	}

	/**
	 * Return the crafting succes rate when using the L2RecipeList.
	 */
	public int getSuccessRate()
	{
		return _successRate;
	}

	/**
	 * Return the Masterwork item ID.
	 */
	public int getMasterwork()
	{
		return _masterwork;
	}

	/**
	 * Return the crafting MP cost of this L2RecipeList.
	 */
	public int getMpCost()
	{
		return _mpCost;
	}

	/**
	 * Return true if the Item crafted with this L2RecipeList is consumable
	 * (shot, arrow,...).
	 */
	public boolean isConsumable()
	{
		return ((_itemId >= 1463 && _itemId <= 1467) // Soulshots
				|| (_itemId >= 2509 && _itemId <= 2514) // Spiritshots
				|| (_itemId >= 3947 && _itemId <= 3952) // Blessed Spiritshots
				|| (_itemId >= 1341 && _itemId <= 1345) // Arrows
		);
	}
	
	/**
	 * Return true if the Item crafted with this L2RecipeList is affected by critical craft.<BR><BR>
	 */
	public boolean isCriticalAffected()
	{
		L2Item item = ItemTable.getInstance().getTemplate(_itemId);
		
		if (item == null)
			return false;
		
		return (item.getCrystalType() != L2Item.CRYSTAL_B
				&& item.getCrystalType() != L2Item.CRYSTAL_A
				&& item.getCrystalType() != L2Item.CRYSTAL_S
				&& item.getCrystalType() != L2Item.CRYSTAL_S80
		);
	}

	/**
	 * Return the Identifier of the Item crafted with this L2RecipeList.
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Return the quantity of Item crafted when using this L2RecipeList.
	 */
	public int getCount()
	{
		return _count;
	}

	/**
	 * Return <B>true</B> if this a Dwarven recipe or <B>false</B> if its a
	 * Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}

	/**
	 * Return the table containing all L2RecipeInstance (1 line of the recipe :
	 * Item-Quantity needed) of the L2RecipeList.
	 */
	public L2RecipeInstance[] getRecipes()
	{
		return _recipes;
	}
}

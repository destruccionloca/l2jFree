/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.recipes.model;

/**
 * This class describes a Recipe used by Dwarf to craft Item.
 * All L2Recipe are made of L2RecipeComponent (1 line of the recipe : Item-Quantity needed).<BR><BR>
 * 
 */
public class L2Recipe
{   
    // TODO move all those constant declaration in a specific constant file 
	private static final int ARROWS_MAX = 1345;

    private static final int ARROWS_INF = 1341;

    private static final int BLESSED_SPIRITSHOTS_MAX = 3952;

    private static final int BLESSED_SPIRITSHOTS_INF = 3947;

    private static final int SPIRITSHOTS_MAX = 2514;

    private static final int SPIRITSHOTS_INF = 2509;

    private static final int SOULSHOTS_MAX = 1467;

    private static final int SOULSHOTS_INF = 1463;

    /** The table containing all L2RecipeComponent (1 line of the recipe : Item-Quantity needed) of the L2RecipeList */
	private L2RecipeComponent[] _recipesComponent;

	/** The Identifier of the Instance */
	private int _id;

	/** The crafting level needed to use this L2Recipe */
	private int _level;

	/** The Identifier of the L2Recipe */
	private int _recipeId;

	/** The name of the L2Recipe */
	private String _recipeName;

	/** The crafting succes rate when using the L2Recipe */
	private int _successRate;

	/** The crafting MP cost of this L2Recipe */
	private int _mpCost;

	/** The Identifier of the Item crafted with this L2Recipe */
	private int _itemId;

	/** The quantity of Item crafted when using this L2Recipe */
	private int _count;

	/** If this a common or a dwarven recipe */ 
	private boolean _IsDwarvenRecipe; 

	/**
	 * Constructor of L2Recipe (create a new Recipe).<BR><BR>
	 */
	public L2Recipe(int id, int level, int recipeId, String recipeName, int successRate, int mpCost, int itemId, int count, boolean isDwarvenRecipe)
	{
		_id = id;
		_recipesComponent = new L2RecipeComponent[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_IsDwarvenRecipe = isDwarvenRecipe;
	}

	/**
	 * Add a L2RecipeInstance to the L2Recipe (add a line Item-Quantity needed to the Recipe).<BR><BR>
	 */
	public void addRecipe(L2RecipeComponent recipe)
	{
		int len = _recipesComponent.length;
		L2RecipeComponent[] tmp = new L2RecipeComponent[len+1];
		System.arraycopy(_recipesComponent, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipesComponent = tmp;
	}


	/**
	 * Return the Identifier of the Instance.<BR><BR>
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Return the crafting level needed to use this L2Recipe.<BR><BR>
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * Return the Identifier of the L2Recipe.<BR><BR>
	 */
	public int getRecipeId()
	{
		return _recipeId;
	}

	/**
	 * Return the name of the L2Recipe.<BR><BR>
	 */
	public String getRecipeName()
	{
		return _recipeName;
	}

	/**
	 * Return the crafting succes rate when using the L2Recipe.<BR><BR>
	 */
	public int getSuccessRate()
	{
		return _successRate;
	}

	/**
	 * Return the crafting MP cost of this L2Recipe.<BR><BR>
	 */
	public int getMpCost()
	{
		return _mpCost;
	}

	/**
	 * Return rue if the Item crafted with this L2Recipe is consumable (shot, arrow,...).<BR><BR>
	 */
	public boolean isConsumable()
	{
		return ((_itemId >= SOULSHOTS_INF && _itemId <= SOULSHOTS_MAX) // Soulshots
				|| (_itemId >= SPIRITSHOTS_INF && _itemId <= SPIRITSHOTS_MAX) // Spiritshots
				|| (_itemId >= BLESSED_SPIRITSHOTS_INF && _itemId <= BLESSED_SPIRITSHOTS_MAX) // Blessed Spiritshots
				|| (_itemId >= ARROWS_INF && _itemId <= ARROWS_MAX) // Arrows
		);
	}

	/**
	 * Return the Identifier of the Item crafted with this L2Recipe.<BR><BR>
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Return the quantity of Item crafted when using this L2Recipe.<BR><BR>
	 */
	public int getCount()
	{
		return _count;
	}

	/** 
	 * Return <B>true</B> if this a Dwarven recipe or <B>false</B> if its a Common recipe 
	 */ 
	public boolean isDwarvenRecipe() 
	{ 
		return _IsDwarvenRecipe; 
	} 

	/**
	 * Return the table containing all L2RecipeComponent (1 line of the recipe : Item-Quantity needed) of the L2Recipe.<BR><BR>
	 */
	public L2RecipeComponent[] getRecipeComponents()
	{
		return _recipesComponent;
	}
}


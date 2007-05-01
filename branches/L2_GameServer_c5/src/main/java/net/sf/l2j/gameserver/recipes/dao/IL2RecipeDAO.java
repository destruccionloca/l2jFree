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
package net.sf.l2j.gameserver.recipes.dao;

import net.sf.l2j.gameserver.recipes.model.L2Recipe;

/**
 * Interface for a data acces to recipes
 */
public interface IL2RecipeDAO
{
    /**
     * Return the recipe list size
     * @return recipe list size
     */
    public abstract int getRecipesCount();

    /**
     * Return a L2Recipe by its place in the list.
     * 
     * @return a L2Recipe
     * @param listId or null if it doesn't exist
     */
    public abstract L2Recipe getRecipe(int listId);

    /** 
     * Retrieve the recipe for the given item id
     * @param itemId
     * @return L2Recipe for this itemId or null if not found
     */
    public abstract L2Recipe getRecipeByItemId(int itemId);

    /**
     * 
     * @param recId
     * @return L2Recipe for the the given recipe id or null if not found
     */
    public abstract L2Recipe getRecipeById(int recId);    
    
    /**
     * 
     * @return an array of all recipe ids
     */
    public abstract int[] getRecipeIds();        
    
}

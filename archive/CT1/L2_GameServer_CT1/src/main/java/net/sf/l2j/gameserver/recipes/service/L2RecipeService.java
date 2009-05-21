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
package net.sf.l2j.gameserver.recipes.service;

import net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;

/**
 * Service use to question RecipeDAO
 * recipes are {@link L2Recipe} object composed by {@link L2RecipeComponent} 
 * 
 */
public class L2RecipeService
{
    private IL2RecipeDAO l2RecipeDAO = null;
    
    public  L2RecipeService ()
    {
    }
    
    /**
     * Return the recipe in position listId in the recipe list
     * @param listId
     * @return a L2Recipe
     */
    public L2Recipe getRecipeList(int listId)
    {
        return l2RecipeDAO.getRecipe(listId);
    }

    /**
     * @param recipeDAO the l2RecipeDAO to set
     */
    public void setL2RecipeDAO(IL2RecipeDAO recipeDAO)
    {
        l2RecipeDAO = recipeDAO;
    }

    /**
     * @param listId
     * @return
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipe(int)
     */
    public L2Recipe getRecipe(int listId)
    {
        return l2RecipeDAO.getRecipe(listId);
    }

    /**
     * @param recId
     * @return
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeById(int)
     */
    public L2Recipe getRecipeById(int recId)
    {
        return l2RecipeDAO.getRecipeById(recId);
    }

    /**
     * @param itemId
     * @return
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeByItemId(int)
     */
    public L2Recipe getRecipeByItemId(int itemId)
    {
        return l2RecipeDAO.getRecipeByItemId(itemId);
    }

    /**
     * @return
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipesCount()
     */
    public int getRecipesCount()
    {
        return l2RecipeDAO.getRecipesCount();
    }

    /**
     * @return
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeIds()
     */
    public int[] getRecipeIds()
    {
        return l2RecipeDAO.getRecipeIds();
    }    

}

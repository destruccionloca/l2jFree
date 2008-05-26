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
package net.sf.l2j.gameserver.recipes.manager;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.service.L2RecipeService;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.tools.L2Registry;

public class CraftManager 
{
    /**
     * Service for recipe list
     */    
    private static L2RecipeService l2RecipeService= (L2RecipeService) L2Registry.getBean(IServiceRegistry.RECIPE);

    /**
     * map with all recipe maker
     * This map is synchronized to prevent incoherence in craft actions.
     * This is a weakHashMap to avoid keeping reference of L2PcInstance if they are not used anymore
     */    
    private static Map<L2PcInstance, RecipeItemMaker> activeMakers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RecipeItemMaker>());

    /**
     * Check if the player is in craft mode
     * It is only possible if ALT_GAME_CREATION is activated (craft take time)
     * 
     * @param player
     * @return true or false
     */
    public static boolean isPlayerCrafting (L2PcInstance player)
    {
        if (Config.ALT_GAME_CREATION)
        {
            synchronized (activeMakers)
            {
                return activeMakers.containsKey(player);
            }
        }
        return false;
    }
    
    /**
     * Request to open book
     * If the player is not in craft mode (or if we activate ALT_GAME_CREATION)
     * => return the list of recipe in the recipe book
     * Else
     * => send a system message : CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING
     * 
     * @param player
     * @param isDwarvenCraft
     */
    public static void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
    {
        RecipeItemMaker maker = null;
        if (Config.ALT_GAME_CREATION)
        {
            synchronized (activeMakers)
            {
                maker = activeMakers.get(player);
            }
        }
        
        if (maker == null)
        {
            RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
            response.addRecipes(isDwarvenCraft  ? player.getDwarvenRecipeBook()
                                                : player.getCommonRecipeBook());
            player.sendPacket(response);
            return;
        }
        
        SystemMessage sm = new SystemMessage(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
        player.sendPacket(sm);
        return;
    }
    
    /**
     * Ask abort of a craft
     * 
     * @param player
     */
    public static void requestMakeItemAbort(L2PcInstance player)
    {
        if ( Config.ALT_GAME_CREATION )
        {
            // don't need to synchronize the method, just need to synchronize access to activeMakers
            synchronized (activeMakers)
            {
                activeMakers.remove(player); 
            }
        }
    }
    
    /**
     * Ask the manufacture of an item by another player
     * If manufacturer is already in craft mode and  ALT_GAME_CREATION = false
     * => send message to say that manufacturer is busy
     * Else
     * => put the action in ThreadPoolManager
     * @param manufacturer
     * @param recipeListId
     * @param player
     */
    public static void requestManufactureItem(L2PcInstance manufacturer, int recipeListId,
                                                    L2PcInstance player)
    {
        L2Recipe pRecipe = getValidRecipeList(player, recipeListId);
        
        if (pRecipe == null) return;
        
        RecipeItemMaker maker;
        
        if (CraftManager.isPlayerCrafting(manufacturer)) // check if busy
        {
            player.sendMessage("Manufacturer is busy, please try again later.");
            return;
        }
        
        maker = new RecipeItemMaker(manufacturer, pRecipe, player);
        if (maker.isValid())
        {
            if (Config.ALT_GAME_CREATION)
            {
                synchronized (activeMakers)
                {
                    activeMakers.put(manufacturer, maker);
                    ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
                }
            }
            else maker.run();
        }
    }
    
    /**
     * Ask the craft of an item 
     * If player is already in craft mode and  ALT_GAME_CREATION = false
     * => send message to say that player is busy
     * Else
     * => put the action in ThreadPoolManager
     * @param player
     * @param recipeListId
     */
    public static void requestMakeItem(L2PcInstance player, int recipeListId)
    {
		if (player.isInDuel())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_CRAFT_DURING_COMBAT));
			return;
		}

        L2Recipe recipe = getValidRecipeList(player, recipeListId);
        
        if (recipe == null) return;
        
        RecipeItemMaker maker;
        
        // check if already busy (possible in alt mode only)
        if (CraftManager.isPlayerCrafting(player)) 
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("You are busy creating ");
            sm.addItemNameById(recipe.getItemId());
            player.sendPacket(sm);
            return;
        }
        
        maker = new RecipeItemMaker(player, recipe, player);
        if (maker.isValid())
        {
            if (Config.ALT_GAME_CREATION)
            {
                synchronized (activeMakers)
                {
                    activeMakers.put(player, maker);
                    ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
                }
            }
            else maker.run();
        }
    }
    

    /**
     * 
     * @param player
     * @param id
     * @return
     */
    private static L2Recipe getValidRecipeList(L2PcInstance player, int id)
    {
        L2Recipe recipe =  l2RecipeService.getRecipeList(id - 1);
        
        if ((recipe == null) || (recipe.getRecipeComponents().length == 0))
        {
            player.sendMessage("No recipe for: " + id);
            player.isInCraftMode(false);
            return null;
        }
        return recipe;
    }
}

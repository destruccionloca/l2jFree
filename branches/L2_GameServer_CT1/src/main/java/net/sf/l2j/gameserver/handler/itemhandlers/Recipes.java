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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.service.L2RecipeService;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.tools.L2Registry;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.5.2.5 $ $Date: 2005/04/06 16:13:51 $
 */

public class Recipes implements IItemHandler
{
    private static int[] ITEM_IDS = null;
    private L2RecipeService __l2RecipeService ;
    
    public Recipes()
    {
        __l2RecipeService = (L2RecipeService) L2Registry.getBean(IServiceRegistry.RECIPE);
        ITEM_IDS = __l2RecipeService.getRecipeIds();
    }

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if (!(playable instanceof L2PcInstance))
            return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        L2Recipe rp = __l2RecipeService.getRecipeByItemId(item.getItemId()); 
        if (activeChar.hasRecipeList(rp.getId())) 
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED); 
            activeChar.sendPacket(sm); 
        }
        else
        {
            if (rp.isDwarvenRecipe()) 
            {
                if (activeChar.hasDwarvenCraft()) 
                { 
                    if (rp.getLevel()>activeChar.getDwarvenCraft())
                    {
                        //can't add recipe, becouse create item level too low
                        SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER); 
                        activeChar.sendPacket(sm); 
                    }
                    else if (activeChar.getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit())
                    {
                        //Up to $s1 recipes can be registered.
                        SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
                        sm.addNumber(activeChar.getDwarfRecipeLimit());
                        activeChar.sendPacket(sm);
                    }
                    else
                    {
                        activeChar.registerDwarvenRecipeList(rp); 
                        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false); 
                        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); 
                        sm.addString("Added recipe \"" + rp.getRecipeName() + "\" to Dwarven RecipeBook"); 
                        activeChar.sendPacket(sm); 
                    }
                } 
                else 
                { 
                    SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT); 
                    activeChar.sendPacket(sm); 
                } 
            } 
            else 
            { 
                if (activeChar.hasCommonCraft()) 
                { 
                    if (rp.getLevel()>activeChar.getCommonCraft())
                    {
                        //can't add recipe, becouse create item level too low
                        SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER); 
                        activeChar.sendPacket(sm); 
                    }
                    else if (activeChar.getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit())
                    {
                        //Up to $s1 recipes can be registered.
                        SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
                        sm.addNumber(activeChar.getCommonRecipeLimit());
                        activeChar.sendPacket(sm);
                    }
                    else
                    {
                        activeChar.registerCommonRecipeList(rp); 
                        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false); 
                        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); 
                        sm.addString("Added recipe \"" + rp.getRecipeName() + "\" to Common RecipeBook"); 
                        activeChar.sendPacket(sm); 
                    }
                } 
                else 
                { 
                    SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT); 
                    activeChar.sendPacket(sm); 
                } 
            }
        }
    }

    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
}

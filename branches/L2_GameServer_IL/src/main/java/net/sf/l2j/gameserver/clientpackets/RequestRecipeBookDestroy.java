/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.service.L2RecipeService;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;
import net.sf.l2j.tools.L2Registry;

public class RequestRecipeBookDestroy extends L2GameClientPacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";

    private int _recipeId;

    /**
    * Unknown Packet:ad
    * 0000: ad 02 00 00 00
    */
    @Override
    protected void readImpl()
    {
        _recipeId = readD();
    }
            
    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar != null)
        {
            L2RecipeService l2RecipeService = (L2RecipeService) L2Registry.getBean(IServiceRegistry.RECIPE);
        	L2Recipe rp =l2RecipeService.getRecipeList(_recipeId-1) ;
         	if (rp == null) 
         		return;
            activeChar.unregisterRecipeList(_recipeId);
            
            RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(),activeChar.getMaxMp()); 
         	if (rp.isDwarvenRecipe()) 
         		response.addRecipes(activeChar.getDwarvenRecipeBook()); 
         	else 
         		response.addRecipes(activeChar.getCommonRecipeBook()); 
            
            activeChar.sendPacket(response);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKDESTROY;
    }
}

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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.service.L2RecipeService;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.L2Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * 
 * format   dddd
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeItemMakeInfo extends ServerBasePacket
{
    private static final String _S__D7_RECIPEITEMMAKEINFO = "[S] D7 RecipeItemMakeInfo";
    private final static Log _log = LogFactory.getLog(RecipeItemMakeInfo.class.getName());

    private int _id;
    private L2PcInstance _player;
    private boolean _success;
    private L2Recipe[] _recipes;
    private L2RecipeService __l2RecipeService = (L2RecipeService) L2Registry.getBean("L2RecipeService");
    
    public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
    {
        _id = id;
        _player = player;
        _success = success;
    }

    public RecipeItemMakeInfo(int id, L2PcInstance player)
    {
        _id = id;
        _player = player;
        _success = true;
    }

    final void runImpl()
    {
        // no long-running tasks
    }

    final void writeImpl()
    {
        L2Recipe recipe = __l2RecipeService.getRecipeById(_id);
        
        boolean hasRecipe = false;

        _recipes = _player.getDwarvenRecipeBook();
        for (L2Recipe rl: _recipes)
        {
            if (rl.getId() == _id)
                hasRecipe = true;
        }
        _recipes = _player.getCommonRecipeBook();
        for (L2Recipe rl: _recipes)
        {
            if (rl.getId() == _id)
                hasRecipe = true;
        }
        if (!hasRecipe)
        {
            Util.handleIllegalPlayerAction(_player,"Player "+_player.getName()+" tried to open itemmakeinfo without having recipe registered - possible packet exploit", IllegalPlayerAction.PUNISH_KICK);
            return;
        }

        if (recipe != null)
        {
            writeC(0xD7);

            writeD(_id);
            writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common 
            writeD((int) _player.getCurrentMp());
            writeD(_player.getMaxMp());
            writeD(_success ? 1 : 0); // item creation success/failed  
        }
        else if (_log.isDebugEnabled()) _log.info("No recipe found with ID = " + _id);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__D7_RECIPEITEMMAKEINFO;
    }
}

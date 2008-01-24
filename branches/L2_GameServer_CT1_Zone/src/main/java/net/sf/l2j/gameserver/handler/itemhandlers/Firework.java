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
package net.sf.l2j.gameserver.handler.itemhandlers; 

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;

/** 
 * This class ... 
 * 
 * @version $Revision: 1.0.0.0.0.0 $ $Date: 2005/09/02 19:41:13 $ 
 */ 

public class Firework implements IItemHandler 
{
    //Modified by Baghak (Prograsso): Added Firework support
    private static final int[] ITEM_IDS = { 6403, 6406, 6407 };
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        L2PcInstance activeChar;
        activeChar = (L2PcInstance)playable;
        int itemId = item.getItemId();

        if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_FIREWORK))
        {
        	SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
        	sm.addItemName(item);
        	activeChar.sendPacket(sm);
        	return;
        }

        /*
         * Elven Firecracker
         */
        if (itemId == 6403) // elven_firecracker, xml: 2023
        {
            MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, 2023, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2023, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
          /*
         * Firework
         */
        else if (itemId == 6406) // firework, xml: 2024
        {
            MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, 2024, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2024, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
        /*
         * Lage Firework
         */
        else if (itemId == 6407) // large_firework, xml: 2025
        {
            MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2025, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
    }
    
    public void useFw(L2PcInstance activeChar, int magicId,int level) 
    {
        L2Skill skill = SkillTable.getInstance().getInfo(magicId,level);
        if (skill != null) {
            activeChar.useMagic(skill, false, false);
        }
    }
    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
}
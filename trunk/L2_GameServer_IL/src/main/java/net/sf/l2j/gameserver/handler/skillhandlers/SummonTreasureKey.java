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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class SummonTreasureKey implements ISkillHandler
{
    static Log _log = LogFactory.getLog(ChargeSelf.class.getName());
    protected SkillType[] _skillIds = {SkillType.SUMMON_TREASURE_KEY};

    public void useSkill(L2Character activeChar, @SuppressWarnings("unused")
    L2Skill skill, @SuppressWarnings("unused")
    L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance) activeChar;

        try
        {
            L2ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
            if (itemToTake.getCount() - skill.getItemConsume() <= 0)
                itemToTake.decayMe();
            else
                itemToTake.setCount(itemToTake.getCount() - skill.getItemConsume());
         
            int item_id = 0;

            switch (skill.getLevel())
            {
                case 1:
                {
                  item_id = Rnd.get(6667, 6669); 
                  break;
                }
                case 2:
                {
                  item_id = Rnd.get(6668, 6670); 
                  break;
                }
                case 3:
                {
                  item_id = Rnd.get(6669, 6671); 
                  break;
                }
                case 4:
                {
                  item_id = Rnd.get(6670, 6672); 
                  break;
                }
            }
            player.addItem("Skill", item_id, Rnd.get(2,3), player, false);  
        }
        catch (Exception e)
        {
            _log.fatal("Error using skill summon Treasure Key:" + e); 
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }

}

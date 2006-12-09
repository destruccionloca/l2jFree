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
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/** 
 * @author _drunk_ 
 * 
 * TODO To change the template for this generated type comment go to 
 * Window - Preferences - Java - Code Style - Code Templates 
 */ 
public class SummonCp implements ISkillHandler 
{ 
    //private final static Log _log = LogFactory.getLog(SiegeFlag.class.getName()); 
    protected SkillType[] _skillIds = {SkillType.SUMMONCP}; 
    
    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;
      
        try
        {
            L2ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
            if (itemToTake.getCount() - skill.getItemConsume() <= 0)
                itemToTake.decayMe();
            else
                itemToTake.setCount(itemToTake.getCount() - skill.getItemConsume());

            // Give items
            player.addItem("Skill",5591,20,player,false);
        }
        catch (Exception e)
        {
            player.sendMessage("Error using skill summon CP:" + e);
        }
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    }

}

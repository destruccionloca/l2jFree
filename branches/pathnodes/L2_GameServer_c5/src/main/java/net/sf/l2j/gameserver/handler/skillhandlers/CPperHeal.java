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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
/**
 * @author -Nemesiss-
 *
 */

public class CPperHeal implements ISkillHandler
{
    //private final static Log _log = LogFactory.getLog(CombatPointHeal.class.getName());
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = {SkillType.COMBATPOINTPERHEAL};
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useSkill(@SuppressWarnings("unused") L2Character actChar, L2Skill skill, L2Object[] targets)
    {
        L2Character target;
        
        for(int index = 0;index < targets.length;index++)
        {
            target = (L2Character)targets[index];
            
            double percp = target.getMaxCp()*skill.getPower();
            target.setCurrentCp(target.getCurrentCp()-percp);             
            StatusUpdate sucp = new StatusUpdate(target.getObjectId()); 
            sucp.addAttribute(StatusUpdate.CUR_CP, (int)target.getCurrentCp()); 
            target.sendPacket(sucp); 
            //Missing system message?? 
        }
    }
    
    
    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
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

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.entity.Siege;

/** 
 * @author _drunk_ 
 * 
 */ 
public class SiegeFlag implements ISkillHandler 
{ 
    private static final SkillType[] SKILL_IDS = {SkillType.SIEGEFLAG}; 
    
    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;

        if (SiegeManager.checkIfOkToPlaceFlag(activeChar, false))
        {
            Siege siege = SiegeManager.getInstance().getSiege(player);

            try
            {
                // spawn a new flag
                L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062), skill.isAdvanced());
                flag.setTitle(player.getClan().getName());
                flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
                flag.setHeading(player.getHeading());
                flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
                siege.getFlag(player.getClan()).add(flag);
            }
            catch (Exception e)
            {
            }
        }
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return SKILL_IDS; 
    }


}

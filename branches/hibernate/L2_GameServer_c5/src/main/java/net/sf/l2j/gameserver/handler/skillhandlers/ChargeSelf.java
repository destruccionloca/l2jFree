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
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/04 19:08:01 $
 */

public class ChargeSelf implements ISkillHandler
{
    static Log _log = LogFactory.getLog(ChargeSelf.class.getName());
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = {
           SkillType.CHARGE_SELF,
           };
    
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        int damage =0;
        for(int index = 0;index < targets.length;index++)
        {   L2Character target = (L2Character)targets[index];
            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
            if (!(targets[index] instanceof L2PcInstance))
                continue;
            
            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance &&
            target.isAlikeDead() && target.isFakeDeath())
            {
            target.stopFakeDeath(null);
            }else if (target.isAlikeDead())
                continue;
            boolean dual = activeChar.isUsingDualWeapon();
            boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
            boolean crit = Formulas.getInstance().calcCrit(activeChar, target, activeChar.getCriticalHit(target, skill));
            boolean soul = (weapon!= null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER );
           
            skill.getEffects(activeChar, activeChar);
            damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
            if (target.isPetrified())
            {damage = 0;}
            target.reduceCurrentHp(damage, activeChar);
            if (activeChar instanceof L2PcInstance)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
                sm.addNumber(damage);
                activeChar.sendPacket(sm);
            }
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}

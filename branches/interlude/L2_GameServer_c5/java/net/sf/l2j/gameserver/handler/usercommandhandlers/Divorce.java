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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Divorce implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 901 }; 
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;
        
        if(activeChar.getPartnerId()==0)
            return false;

        int _partnerId = activeChar.getPartnerId();
        
        if(activeChar.isMaried())
            activeChar.sendMessage("You are divorced now.");
        else
            activeChar.sendMessage("You are disengaged now.");
        
        activeChar.setMaried(false);
        activeChar.setPartnerId(0);
        Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
        couple.divorce();
        couple = null;
        
        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(_partnerId);
        
        if (partner != null)
        {
            partner.setPartnerId(0);
            if(partner.isMaried())
                partner.sendMessage("Your Partner has decided to divorce from you.");
            else
                partner.sendMessage("Your Partner has decided to disengage.");
            partner.setMaried(false);            
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminSendHome implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS = {"admin_sendhome"};
    private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
        	return false;
        
        if (command.startsWith("admin_sendhome"))
        {
            if(command.split(" ").length > 1)
                handleSendhome(activeChar, command.split(" ")[1]);
            else
                handleSendhome(activeChar);
        }
        
        return true;
    }
    
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
    
    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
    
    private void handleSendhome(L2PcInstance activeChar)
    {
        handleSendhome(activeChar, null);
    }
    
    private void handleSendhome(L2PcInstance activeChar, String player)
    {
        L2Object obj = activeChar.getTarget();
        
        if (player != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(player);
            
            if (plyr != null)
            {
                obj = plyr;
            }
        }
        
        if (obj == null)
            obj = activeChar;
        
        if (obj instanceof L2Character) 
            doSendhome((L2Character)obj);
        else 
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
    }
    
    private void doSendhome(L2Character targetChar)
    {
    	targetChar.teleToLocation(TeleportWhereType.Town);
    }
}

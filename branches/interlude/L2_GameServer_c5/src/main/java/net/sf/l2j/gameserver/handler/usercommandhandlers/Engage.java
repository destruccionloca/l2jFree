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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** 
 * @author evill33t
 * 
 */
public class Engage implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 81 }; 
    private final static Log _log = LogFactory.getLog(Engage.class.getName());
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;
        
        if (activeChar.getTarget()==null)
        {
            activeChar.sendMessage("You have noone targeted.");
            return false;
        }
        if (!(activeChar.getTarget() instanceof L2PcInstance))
        {
            activeChar.sendMessage("You can only ask another Player for partnership");
            return false;
        }
        if (activeChar.getPartnerId()!=0)
        {
            activeChar.sendMessage("You are already engaged.");
            // Punish Code here
            return false;
        }

        L2PcInstance ptarget = (L2PcInstance)activeChar.getTarget();
        
        if (ptarget.getSex()==activeChar.getSex() && !Config.WEDDING_SAMESEX)
        {
            activeChar.sendMessage("You cant ask partners of same sex.");
            return false;
        }
        
        boolean FoundOnFriendList = false;
        int objectId;
        java.sql.Connection con = null;
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
            statement.setInt(1, ptarget.getObjectId());
            ResultSet rset = statement.executeQuery();
        
            while (rset.next())
            {
                objectId = rset.getInt("friend_id");
                if(objectId == activeChar.getObjectId())
                    FoundOnFriendList = true;
            }
        } 
        catch (Exception e) 
        {
            _log.warn("could not read friend data:"+e);
        } 
        finally 
        {
            try {con.close();} catch (Exception e){}
        }
        
        if(!FoundOnFriendList)
        {
            activeChar.sendMessage("The Person you wanna ask hasnt added you on the friendlist.");
            return false;
        }
        
        //TODO: code for popup box here
        return false; //not finished
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
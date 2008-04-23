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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles following admin commands: - delete = deletes target
 * 
 * @version $Revision: 1.1.2.6.2.3 $ $Date: 2005/04/11 10:05:59 $
 */
public class AdminRepairChar implements IAdminCommandHandler
{
    private final static Log _log = LogFactory.getLog(AdminRepairChar.class.getName());

    private static final String[] ADMIN_COMMANDS = { "admin_restore", "admin_repair" };

    private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
                return false;
        }
        
        handleRepair(command);
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

    private void handleRepair(String command)
    {
        String[] parts = command.split(" ");
        if (parts.length != 2)
        {
            return;
        }
        
        Connection connection = null;
        try
        {
            connection = L2DatabaseFactory.getInstance().getConnection(connection);
            
            PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
            statement.setString(1,parts[1]);
            ResultSet rset = statement.executeQuery();
            int objId = 0;
            if (rset.next())
            {
                objId = rset.getInt(1);
            }
            rset.close();
            statement.close();
            
            if (objId == 0) {connection.close(); return;}
            
            statement = connection.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3503 WHERE obj_id=?");
            statement.setInt(1, objId);
            statement.execute();
            statement.close();
            
            statement = connection.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
            statement.setInt(1, objId);
            statement.execute();
            statement.close();
            
            statement = connection.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
            statement.setInt(1, objId);
            statement.execute();
            statement.close();
            
            connection.close();
        }
        catch (Exception e)
        {
			_log.warn( "could not repair char:", e);
        } 
        finally 
        {
            try { connection.close(); } catch (Exception e) {}
        }
    }
}

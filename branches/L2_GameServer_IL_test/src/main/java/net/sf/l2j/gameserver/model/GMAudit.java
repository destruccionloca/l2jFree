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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GMAudit {
    private static final Log _log = LogFactory.getLog(GMAudit.class.getName());

    public static void auditGMAction(L2PcInstance gm, String type, String action, String param){
        if (Config.GM_AUDIT && Config.ALT_PRIVILEGES_ADMIN)
        {
            String gm_name = gm.getAccountName() + " - " + gm.getName();
            String target;
            
            if (gm.getTarget() != null)
                target = String.valueOf(gm.getTargetId()) + " - " + gm.getTarget().getName();
            else
                target = "null";
            
            java.sql.Connection con = null;
            PreparedStatement statement = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection(con);
                statement = con.prepareStatement(
                "INSERT INTO gm_audit(gm_name, target, type, action, param, date) VALUES(?,?,?,?,?,now())");

                statement.setString(1, gm_name);
                statement.setString(2, target );
                statement.setString(3, type   );
                statement.setString(4, action );
                statement.setString(5, param  );

                statement.executeUpdate();

            } catch (Exception e) {
                _log.fatal( "could not audit GM action:", e);
            } finally {
                try {
                    statement.close(); 
                    con.close(); 
                } catch (Exception e) {}
            }
        }
    }
}

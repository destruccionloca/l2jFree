package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GMAudit {
private static final Log _log = LogFactory.getLog(GMAudit.class.getName());

public static void auditGMAction(String gmName, String action, String target, String params){
	if (Config.GMAUDIT)
	{
        java.sql.Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(
            	"INSERT INTO GM_AUDIT(GM_NAME, ACTION, TARGET, PARAM, DATE) VALUES(?,?,?,?,now())");
            
            statement.setString(1, gmName);
            statement.setString(2, action);
            statement.setString(3, target);
            statement.setString(4, params);
            
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
	
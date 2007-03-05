package net.sf.l2j.gameserver.script.stat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/** 
 *  Leaderboard interface for all Leaderboards.
 *
 * @author  Imper1um
 */

public interface Leaderboard {
   final static Log _log = LogFactory.getLog(Leaderboard.class.getName());
   
   public void update(Connection c, ResultSet rs) throws SQLException;
   public String getHTML();
   public int getID();
   public void assignID(int id);
   public String getDescription();
   public FastList<LeaderboardEntry> getAllEntries();
}
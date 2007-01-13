package net.sf.l2j.gameserver.script.stat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javolution.util.FastList;

/** 
 *  Leaderboard interface for all Leaderboards.
 *
 * @author  Imper1um
 */

public interface Leaderboard {
   static final Logger _log = Logger.getLogger(Leaderboard.class.getName());
   
   public void update(Connection c, ResultSet rs) throws SQLException;
   public String getHTML();
   public int getID();
   public void assignID(int id);
   public String getDescription();
   public FastList<LeaderboardEntry> getAllEntries();
}
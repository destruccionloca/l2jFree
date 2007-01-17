package net.sf.l2j.gameserver.script.stat.leaderboards;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.script.stat.Leaderboard;
import net.sf.l2j.gameserver.script.stat.LeaderboardEngine;
import net.sf.l2j.gameserver.script.stat.LeaderboardEntry;
import net.sf.l2j.gameserver.script.stat.LeaderboardHTMLFormat;

/** 
 * 
 *
 * @author  Imper1um
 */
public class LeaderboardTotalKills implements Leaderboard {
   private static String leaderboardDescription = "Total Kills";
   private int id = -1;
   private FastList<LeaderboardEntry> _leaderboard;
   
   public LeaderboardTotalKills() {
       _leaderboard = new FastList<LeaderboardEntry>();
   }
   
   public void update(Connection c, ResultSet rs) throws SQLException {
       _leaderboard.clear();
       rs.beforeFirst();
       String s = null;
       while (rs.next()) {
           if (_leaderboard.getLast() == null) { //No Entries, yet.
               s = LeaderboardEngine.getInstance().lookup(c, rs.getInt("id"));
               _leaderboard.add(new LeaderboardEntry(s, rs.getDouble("totalKills")));
           } else {
               int i = -1;
               double sc = rs.getDouble("totalKills");
               boolean pass = true;
               for (LeaderboardEntry le: _leaderboard) {
                   i++;
                   if (le.d <= sc) { //If this is the highest score.
                       s = LeaderboardEngine.getInstance().lookup(c, rs.getInt("id"));
                       _leaderboard.add(i, new LeaderboardEntry(s, sc));
                       pass = false;
                   }
               }
               if (pass && _leaderboard.size() < Config.MAX_LEADERBOARD) {
                   s = LeaderboardEngine.getInstance().lookup(c, rs.getInt("id"));
                   _leaderboard.add(i, new LeaderboardEntry(s, sc));
               }
           }
       }
       while (_leaderboard.size() > Config.MAX_LEADERBOARD)
           _leaderboard.removeLast();
   }
   
   public String getHTML() {
       return LeaderboardHTMLFormat.getHTML(false, this);
   }
   
   public FastList<LeaderboardEntry> getAllEntries() { return _leaderboard; }
   public int getID() { return id; }
   public void assignID(int _id) { id = _id; }
   public String getDescription() { return leaderboardDescription; }
}
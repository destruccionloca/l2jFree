package net.sf.l2j.gameserver.script.stat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.script.stat.leaderboards.*;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

import javolution.util.FastList;
import javolution.util.FastMap;

/** Leaderboard Engine
+ * 
 * This ties in with the Stats Engine. This will call the stats engine
 * every x hours, where x = Config.LEADERBOARD_REFRESH
 *
 * @author  Imper1um
 */

public class LeaderboardEngine implements Runnable {
   private static final Logger _log = Logger.getLogger(LeaderboardEngine.class.getName());
   private static LeaderboardEngine _instance;
   
   private FastMap<Integer, Leaderboard> _leaderboards;
   private boolean init = false;
   private int lastLeaderboardId = -1;
   
   public static LeaderboardEngine getInstance() {
       if (_instance == null) _instance = new LeaderboardEngine();
       return _instance;
   }
   
   private LeaderboardEngine() {
       _leaderboards = new FastMap<Integer, Leaderboard>();
       init();
   }
   
   private void init() {
       if (init) return;
       init = true;
// Registered Commands Go below this line
       registerLeaderboard(new LeaderboardTotalDeaths());
       registerLeaderboard(new LeaderboardTotalTimePlayed());
       registerLeaderboard(new LeaderboardTotalPlayerKills());
       registerLeaderboard(new LeaderboardTotalKarma());
// Registered Commands Go above this line
       _log.info("LeaderboardEngine: Registered " + _leaderboards.size() + " Leaderboards.");
       run();
       ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, Config.LEADERBOARD_REFRESH * 60000 * 60, Config.LEADERBOARD_REFRESH * 60000 * 60);
   }
   
   private void registerLeaderboard(Leaderboard l) {
       lastLeaderboardId++;
       l.assignID(lastLeaderboardId);
       _leaderboards.put(lastLeaderboardId, l);
       _log.info("LeaderboardEngine: Registered " + l.getDescription());
   }
   
   public void run() {
       Connection con = null;
       try {
           con = L2DatabaseFactory.getInstance().getConnection();
           PreparedStatement ps = con.prepareStatement("SELECT * FROM character_stats");
           ResultSet rs = ps.executeQuery();
           for (Integer l: _leaderboards.keySet())
               _leaderboards.get(l).update(con, rs);
           Date d = new Date(System.currentTimeMillis());
           SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, h:mm a z");
           if (Config.LEADERBOARD_ANNOUNCE)
               Announcements.getInstance().announceToAll("Leaderboards: Updated " + sdf.format(d));
           if (Config.LEADERBOARD_ADD_ANNOUNCE)
               Announcements.getInstance().setLeaderboardAnnouncement("Leaderboards: Updated " + sdf.format(d));
       } catch (SQLException e) {
           _log.warning("LeaderboardEngine: Unable to refresh leaderboard! EC:" + e.getErrorCode());
           if (Config.DEVELOPER) e.printStackTrace();
       } finally {
           try { con.close(); } catch (Exception e) {}
       }
   }

   /**
    * @param int1
    * @return
    */
   public String lookup(Connection c, int i) throws SQLException {
       PreparedStatement ps = c.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
       ps.setInt(1,i);
       ResultSet rs = ps.executeQuery();
       String s = null;
       if (rs.next())
           s = rs.getString("char_name");
       return s;
   }
   
   public String getHTML() {
       StringBuffer html = new StringBuffer("<HTML><body>");
       html.append("<center>Leaderboards<br>");
       html.append("------------------------------");
       for (Integer i: _leaderboards.keySet()) {
           html.append("<a action=\"bypass -h leaderboard_view " + i + "\">");
           html.append(_leaderboards.get(i).getDescription());
           html.append("</a><br>");
       }
       html.append("</center></body></HTML>");
       return html.toString();
   }

   /** Processes Bypass from user
    */
   public void processBypass(L2PcInstance activeChar, String bypassInput) {
       if (bypassInput.substring(0,4).equalsIgnoreCase("view")) {
           int i = -1;
           try { i = Integer.valueOf(bypassInput.substring(5)); }
           catch (Exception e) { _log.warning("LeaderboardEngine: Invalid View Command! Bypass: " + bypassInput); if (Config.DEVELOPER) e.printStackTrace(); return; }
           if (_leaderboards.get(i) == null) {
               _log.warning("LeaderboardEngine: No such LeaderboardId (" + i + ")!");
               activeChar.sendMessage("Unable to Find Leaderboard!");
               return;
           }
           NpcHtmlMessage html = new NpcHtmlMessage(3);
           html.setHtml(_leaderboards.get(i).getHTML());
           activeChar.sendPacket(html);
       } else if (bypassInput.equalsIgnoreCase("menu")) {
           NpcHtmlMessage html = new NpcHtmlMessage(3);
           html.setHtml(getHTML());
           activeChar.sendPacket(html);
       }
   }
}
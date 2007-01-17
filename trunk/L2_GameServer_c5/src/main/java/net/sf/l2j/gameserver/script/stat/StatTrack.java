package net.sf.l2j.gameserver.script.stat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Imper1um
 */
public class StatTrack {
   private static final Logger _log = Logger.getLogger(StatTrack.class.getName());
   private static FastMap<Integer, StatTrack> _allStats = new FastMap<Integer, StatTrack>();
   
   L2PcInstance owner;
   private double totalKarma;
   private int totalPlayerKills;   //All Player Kills, PK or PvP
   private double totalKills;          //ALL Kills (Monster or otherwise)
   private double totalMonKills;       //Monster Kills ONLY
   private double totalDamageDealt;    //Total Damage Dealt
   private double totalDamageTaken;    //Total Damage Taken
   private int totalDied;          //Total Times Died
   private int totalPlayerDeaths;  //Total Times Died to a player.
   private int totalPKDeaths;      //Total Times PKed.
   private int totalPvPDeaths;     //Total Times killed in PvP.
   private int totalMonsterDeaths; //Total Times Killed by a Monster
   private double totalHealthGained;   //Total Health Gained
   private double totalSpellsCasted;   //Includes Skills.
   private double totalTimesAttacked;  //Total Attack Times (per time)
   private double totalPlayTime;       //in Milliseconds
   private long startPlayTime;     //Start Play Time.
   private double totalXPGained;       //Total XP Gained
   private double totalXPLost;         //Total XP Lost.
   private double totalDistanceTravelled;  //In units. We'll have to figure this one out.
   private boolean active;
   StatLoc lastLoc;
   private LocUpdater loc;
   private ScheduledFuture sf;
   private boolean isTeleporting = false;
   
   private class StatLoc {
       public int x;
       public int y;
       public int z;
		private L2Object owner1;
       
       public StatLoc(L2Object o) {
			owner1 = o;
           updateLoc();
       }
		
		public L2Object getOwner() { return owner1; }
       
       public double getDistance(int _x, int _y, int _z) {
           //Distance is fun. Here's how you do it:
           //The distance between two points on a line is the absolute value
           // of the beginning coordinate minus the ending coordinate. So, 
           // the distance between points 2 and 4 is 4-2, or 2-(4).
           //The equation to find the distance between two points on a single 
           // plane is:
           // Math.sqrt((Math.abs(a1-a2))^2 + (Math.abs(b1-b2))^2)
           //That will get you the distance between the two
           //x and y coordinates.
           double d = Math.sqrt((Math.abs(x-_x))^2 + (Math.abs(y-_y))^2);
           //Then, all you have to do is
           //use that final number in the same equation to get
           //the 3d distance between two points.
           d = Math.sqrt(((int) d)^2 + (Math.abs(z-_z))^2);
           return d;
       }
		public void updateLoc() { x = owner1.getX(); y = owner1.getY(); z = owner1.getZ(); }
		public void destroy() { owner1 = null; }
   }
   
   public class LocUpdater implements Runnable {
		private StatTrack owner1;
		public LocUpdater(StatTrack o) { owner1 = o; }
		public void run() { owner1.increaseDistance(); }
		public StatTrack getOwner() { return owner1; }
		public void destroy() { owner1 = null; }
   }
   
   public class AutoUpdater implements Runnable {
		private StatTrack owner1;
		public AutoUpdater(StatTrack o) { owner1 = o; }
		public void run() { owner1.save(); }
		public StatTrack getOwner() { return owner1; }
		public void destroy() { owner1 = null; }
   }
   
   public void increaseDistance() {
       if (!active || isTeleporting) return; //DO NOT RUN IF TELEPORTING OR ACTIVE!
       totalDistanceTravelled += lastLoc.getDistance(owner.getX(), owner.getY(), owner.getZ());
       lastLoc.updateLoc();
   }
   
   public void increaseTime() {
       if (!active) return;
       totalPlayTime += System.currentTimeMillis() + startPlayTime;
       startPlayTime = System.currentTimeMillis();
   }
   
   public static StatTrack findStat(L2PcInstance owner) {
       if (!Config.ENABLE_STATS)
           return null; //If were disabled, don't do nething.
       //If it's already pre-loaded.
		if (_allStats.get(owner.getObjectId()) != null)
			return _allStats.get(owner.getObjectId());
       StatTrack st = new StatTrack(owner);
       return st;
   }
   
   private StatTrack(L2PcInstance pc) {
       owner = pc;
       load();
       _allStats.put(pc.getObjectId(), this);
   }
   
   private void load() { //Load into memory the stat tracking sheet.
       java.sql.Connection con = null;
       try {
           con = L2DatabaseFactory.getInstance().getConnection();
           PreparedStatement ps = con.prepareStatement("SELECT * FROM character_stats WHERE id=?");
           ps.setInt(1, owner.getObjectId());
           ResultSet rs = ps.executeQuery();
           if (rs.next()) { //If there are previous statistics.
               this.totalDamageDealt = rs.getDouble("totalDamageDealt");
               this.totalDamageTaken = rs.getDouble("totalDamageTaken");
               this.totalDied = rs.getInt("totalDied");
               this.totalDistanceTravelled = rs.getDouble("totalDistanceTravelled");
               this.totalHealthGained = rs.getDouble("totalHealthGained");
               this.totalKarma = rs.getDouble("totalKarma");
               this.totalKills = rs.getDouble("totalKills");
               this.totalMonKills = rs.getDouble("totalMonKills");
               this.totalPKDeaths = rs.getInt("totalPKDeaths");
               this.totalPlayerDeaths = rs.getInt("totalPlayerDeaths");
               this.totalPlayerKills = rs.getInt("totalPlayerKills");
               this.totalPlayTime = rs.getDouble("totalPlayTime");
               this.totalPvPDeaths = rs.getInt("totalPvPDeaths");
               this.totalSpellsCasted = rs.getDouble("totalSpellsCasted");
               this.totalTimesAttacked = rs.getDouble("totalTimesAttacked");
               this.totalXPGained = rs.getDouble("totalXPGained");
               this.totalMonsterDeaths = rs.getInt("totalMonsterDeaths");
           } else { //If there's no statistics.
               totalDamageDealt = 0;
               totalDamageTaken = 0;
               totalDied = 0;
               totalDistanceTravelled = 0;
               totalHealthGained = 0;
               totalKarma = 0;
               totalKills = 0;
               totalMonKills = 0;
               totalPKDeaths = 0;
               totalPlayerDeaths = 0;
               totalPlayerKills = 0;
               totalPlayTime = 0;
               totalPvPDeaths = 0;
               totalSpellsCasted = 0;
               totalTimesAttacked = 0;
               totalXPGained = 0;
               totalMonsterDeaths = 0;
               ps.close();
               ps = con.prepareStatement("INSERT INTO character_stats SET id=?");
               ps.setInt(1, owner.getObjectId());
               ps.execute();
           }
           ps.close();
			wake(owner);
       } catch (Exception e) {
           _log.warning("StatTrack.load(): Unable to access statistics for user " + owner.getName());
           if (Config.DEVELOPER) e.printStackTrace();
       } finally {
           try { con.close(); } catch (Exception e) {}
       }
   }
   
   public void save() {
       java.sql.Connection con = null;
       try {
           con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_stats WHERE id=? SET " +
					"totalKarma=?, totalPlayerKills=?, totalKills=?, " +
					"totalMonKills=?, totalDamageDealt=?, totalDamageTaken=?, " +
					"totalDied=?, totalPlayerDeaths=?, totalPKDeaths=?, " +
					"totalPvPDeaths=?, totalHealthGained=?, totalSpellsCasted=?, " +
					"totalTimesAttacked=?, totalPlayTime=?, totalXPGained=?, " +
					"totalXPLost=?, totalDistanceTravelled=?, totalMonsterDeaths=?");
			ps.setInt(1, owner.getObjectId());
			ps.setDouble(2, totalKarma);
			ps.setInt(3, totalPlayerKills);
			ps.setDouble(4, totalKills);
			ps.setDouble(5, totalMonKills);
			ps.setDouble(6, totalDamageDealt);
			ps.setDouble(7, totalDamageTaken);
			ps.setInt(8, totalDied);
			ps.setInt(9, totalPlayerDeaths);
			ps.setInt(10, totalPKDeaths);
			ps.setInt(11, totalPvPDeaths);
			ps.setDouble(12, totalHealthGained);
			ps.setDouble(13, totalSpellsCasted);
			ps.setDouble(14, totalTimesAttacked);
			ps.setDouble(15, totalPlayTime);
			ps.setDouble(16, totalXPGained);
			ps.setDouble(17, totalXPLost);
			ps.setDouble(18, totalDistanceTravelled);
			ps.setDouble(19, totalMonsterDeaths);
			ps.execute();
			ps.close();
       } catch (Exception e) {
           _log.warning("StatTrack.save(): Unable to save statistics for user " + owner.getName());
			if (Config.DEVELOPER) e.printStackTrace();
		} finally {
			try { con.close(); } catch (Exception e) {}
       }
   }
   
   public void sleep() { //Put this Stat Track to Sleep (Logged off User)
		if (!active) return;
       increaseDistance();
       increaseTime();
       active = false;
       sf.cancel(false);
       save();
       sf = null;
       loc = null;
		owner = null; //So Garbage Collection can occur.
	}
	
	public void wake(L2PcInstance newPC) { //Wake up this Stat Track (returning user or new user)
		if (active) return;
       active = true;
       startPlayTime = System.currentTimeMillis();
       lastLoc = new StatLoc(owner);
       loc = new LocUpdater(this);
       sf = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(loc, Config.STATS_UPDATE_TIME*1000, Config.STATS_UPDATE_TIME*1000);
       owner = newPC;
   }
   
   public void increaseDamageDealt(int i) {
       totalDamageDealt += i;
   }
   
   public void increaseTimesAttacked() {
       totalTimesAttacked++;
   }
   
   public void increaseSpellsCasted() {
       totalSpellsCasted++;
   }
   
   public void increaseDamageTaken(int i) {
       totalDamageTaken += 1;
   }

   public void increaseHealthGain(double d) {
       totalHealthGained += d;
   }

   public void increaseKarma(int i) {
       totalKarma += i;
   }
   
   public void increasePKKills() {
       totalPlayerKills++;
       totalKills++;
   }
   
   public void increasePvPKills() {
       totalPlayerKills++;
       totalKills++;
   }
   
   public void reduceXP(long o) {
       totalXPLost += o;
   }
   
   public void increaseXP(long o) {
       totalXPGained += o;
   }
   
   public void increaseMonsterKills() {
       totalMonKills++;
       totalKills++;
   }
   
   public void increaseMonsterDeaths() {
       totalDied++;
       totalMonsterDeaths++;
   }
   
   public void increasePKDeaths() {
       totalDied++;
       totalPKDeaths++;
       totalPlayerDeaths++;
   }
   
   public void increasePvPDeaths() {
       totalDied++;
       totalPvPDeaths++;
       totalPlayerDeaths++;
   }
	
	public String getHTML() {
		StringBuffer sb = new StringBuffer("<HTML><body>");
		sb.append("<center>");
		sb.append("<font color=\"FF7700\">");
		sb.append(owner.getName()); 						//Name
		sb.append("</font>");
		sb.append(" - Level " + owner.getLevel()); 			//Level
		sb.append(" " + owner.getRace().toString());		//Race
		sb.append(" " + owner.getClassId().toString()); 	//Class
		sb.append("<br>");
		sb.append("<font color=\"FFFF00\">");
		sb.append("-------------------------------");
		sb.append("</font>");
		sb.append("<table>");
		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Kills:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append(totalKills);								//All Kills
		sb.append("</font>");
		sb.append("<br>");
		sb.append("(M" + totalMonKills);					//Monster Kills
		sb.append("/P" + totalPlayerKills);					//Player Kills
		sb.append(") (PK" + owner.getPkKills());			//PK Kills
		sb.append("/PvP" + owner.getPvpKills());			//PvP Kills
		sb.append(")");
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Deaths:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append(totalDied);								//All Deaths
		sb.append("</font>");
		sb.append("<br>");
		sb.append("(M" + totalMonsterDeaths);				//Monster Deaths
		sb.append("/P" + totalPlayerDeaths);				//Player Deaths
		sb.append(") (PK" + totalPKDeaths);					//PK Deaths
		sb.append("/PvP" + totalPvPDeaths);					//PvP Deaths
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Karma Gained:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append(totalKarma);								//Karma
		sb.append("</font>");
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("XP:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append("+" + totalXPGained);						//+ XP
		sb.append("/-" + totalXPLost);						//- XP
		sb.append("</font>");
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Damage Dealt/Taken:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append("Dealt " + totalDamageDealt);				//Damage Dealt
		sb.append("<br>");
		sb.append("Taken " + totalDamageTaken);				//Damage Taken
		sb.append("</font>");
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("XP:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append("+" + totalXPGained);						//+ XP
		sb.append(" / -" + totalXPLost);						//- XP
		sb.append("</td>");
		sb.append("</tr>");

		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Play Time:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append(getStringTime());							//Time Played
		sb.append("</td>");
		sb.append("</tr>");
		
		sb.append("<tr>");
		sb.append("<td>");
		sb.append("Total Distance Travelled:");
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<font color=\"FF7700\">");
		sb.append(totalDistanceTravelled);					//Total Distance Travelled
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</center>");        
		
		sb.append("</body>");
		sb.append("</HTML>");
		return sb.toString();
	}
	
	public String getStringTime() {
		increaseTime();
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		double timePlayed = totalPlayTime;
		timePlayed /= 1000;
		while (timePlayed >= 86400) {
			day++;
			timePlayed -= 86400;
		}
		while (timePlayed >= 3600) {
			hour++;
			timePlayed -= 3600;
		}
		while (timePlayed >= 60) {
			minute++;
			timePlayed -= 60;
		}
		second = (int) timePlayed;
		return day + "d " + hour + "h " + hour + " m" + minute + " s" + second; 
	}
   
	public FastMap<Integer, StatTrack> getAllStats() {
	    return _allStats;
	}

	/** Starts the teleport so that users cannot use Teleporters to
     * exploit the distance travelled stat. Increases distance, 
     * then sets isTeleporting = true; */
	public void startTeleport() {
	    increaseDistance();
	    isTeleporting = true;
	}
   
    /** Ends the teleport sequence so that distance travelled statistic
     * will track again. */
	public void stopTeleport() {
	    lastLoc.updateLoc();
	    isTeleporting = false;
	}    
}
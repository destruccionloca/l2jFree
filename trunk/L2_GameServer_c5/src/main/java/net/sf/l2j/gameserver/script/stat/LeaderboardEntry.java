package net.sf.l2j.gameserver.script.stat;

/**
 * 
 *
 * @author  Imper1um
 */
public class LeaderboardEntry {
   public String name;
   public int i;
   public double d;
   
   public LeaderboardEntry(String n, int _i) { name = n; i = _i; }
   public LeaderboardEntry(String n, double _d) { name = n; d = _d; }
}
package net.sf.l2j.gameserver.script.stat;


/**
 * 
 *
 * @author  Imper1um
 */
public class LeaderboardHTMLFormat {
   public static String LeaderboardTitleColor = "FF7700";
   public static String LeaderboardNameColor = "FFFF00";
   public static String LeaderboardFormat =
       "<center><font color=\"%titleColor%\">%title%</font><br>" +
       "---------------------------------<br>" +
       "<table><tr><td>Name</td><td>Score</td></tr>" +
       "%rows%" +
       "</table>" +
       "%links%" +
       "</center>";
   public static String RowFormat = "<tr><td><font color=\"%leaderboardNameColor\">%name%</font></td><td>%score%</td></tr>";

   //DO NOT MODIFY BELOW THIS LINE!!!
   public static String START = "<HTML><body>";
   public static String END = "</body></HTML>";
   public static String LinkFormat = "<a action=\"bypass -h leaderboard_menu\">Leaderboard Menu</a>";
   
   public static String getHTML(boolean isDouble, Leaderboard l) {
       String html = "";
       html += START;
       html += LeaderboardFormat;
       html += END;
       html.replaceAll("%title%", l.getDescription());
       html.replaceAll("%titleColor%", LeaderboardTitleColor);
       html.replaceAll("%links%", LinkFormat);
       StringBuffer rows = new StringBuffer("");
       if (isDouble) {
           for (LeaderboardEntry r: l.getAllEntries()) {
               String t = RowFormat;
               t.replaceAll("%leaderboardNameColor%", LeaderboardNameColor);
               t.replaceAll("%name%", r.name);
               t.replaceAll("%score%", String.valueOf(r.d));
               rows.append(t);
           }
       }
       html.replaceAll("%rows%", rows.toString());
       return html.toString();
   }
}
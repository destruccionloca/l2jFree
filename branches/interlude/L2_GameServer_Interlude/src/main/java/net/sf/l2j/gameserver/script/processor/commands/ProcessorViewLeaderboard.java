package net.sf.l2j.gameserver.script.processor.commands;

import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.script.processor.Processor;
import net.sf.l2j.gameserver.script.stat.LeaderboardEngine;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 * 
 *
 * @author  Imper1um
 */
public class ProcessorViewLeaderboard implements Processor {
   private static boolean  isCaseSensitive = false;
   private static String   SearchString = ".leaderboard";
   private static int      searchType = Processor.EXACT;
   private static String   registryID = "ViewLeaderboard";
   
   public boolean processCommand(Say2 input) {
       NpcHtmlMessage html = new NpcHtmlMessage(3);
       html.setHtml(LeaderboardEngine.getInstance().getHTML());
       input.getClient().getActiveChar().sendPacket(html);
       return true;
   }
   
   public boolean caseSensitive() { return isCaseSensitive; }
   public String getSearchString() { return SearchString; }
   public int getSearchType() { return searchType; }
   public String getRegistryID() { return registryID; }
}
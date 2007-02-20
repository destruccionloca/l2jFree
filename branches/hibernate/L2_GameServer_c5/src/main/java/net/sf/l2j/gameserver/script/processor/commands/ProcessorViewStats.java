package net.sf.l2j.gameserver.script.processor.commands;

import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.script.processor.Processor;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 * 
 *
 * @author  Imper1um
 */
public class ProcessorViewStats implements Processor {
   private static boolean  isCaseSensitive = false;
   private static String   SearchString = ".stats";
   private static int      searchType = Processor.BEGINNING;
   private static String   registryID = "ViewStats";
   
   public boolean processCommand(Say2 input) {
       NpcHtmlMessage html = new NpcHtmlMessage(3);
       String say = input.getSay();
       L2PcInstance sayer = input.getClient().getActiveChar();
       L2PcInstance target = null;
       if (say.length() > 7) { //mentioned person
           target = L2World.getInstance().getPlayer(say.substring(7));
           if (target == null) sayer.sendMessage("StatView: No such player \"" + say.substring(7) + "\". Viewing Target/Own Stats.");
       }
       if (target == null) { //No Such Name, not Logged in, or whatever.
           if (sayer.getTarget() == null) //No Target
               target = sayer;
           else if (sayer.getTarget() instanceof L2PcInstance) //PC Target
               target = (L2PcInstance)sayer.getTarget();
           else //Non-PC Target
               target = sayer;
       }
       html.setHtml(target.getStatTrack().getHTML());
       input.getClient().getActiveChar().sendPacket(html);
       return true;
   }
   
   public boolean caseSensitive() { return isCaseSensitive; }
   public String getSearchString() { return SearchString; }
   public int getSearchType() { return searchType; }
   public String getRegistryID() { return registryID; }
}
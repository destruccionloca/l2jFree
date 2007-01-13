package net.sf.l2j.gameserver.script.processor;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.script.processor.commands.*;

import javolution.util.FastList;

/** Processor Script by Imper1um 
 * This script translates any input from Say2 and makes sure another
 *  script needs to run off of it. This can lead to a more advanced
 *  filter, or simply just a better engine to translate anything.
 *  Admin scripts need to be handled. Each processor must be called
 *  on startup of the engine itself. Otherwise, Processors will not run.
 *  each processor also has a set of rules that it can adhere to, including
 *  beginning checks, middle checks, or end checks.
 *  
 *   @author Imper1um   
 */

public class ProcessorEngine {
   private static final Logger _log = Logger.getLogger(ProcessorEngine.class.getName());
   private static ProcessorEngine _instance;
   
   private FastList<Processor> _processorList;
   private boolean init = false;
   
   
   public static ProcessorEngine getInstance() {
       if (_instance == null) _instance = new ProcessorEngine();
       return _instance;
   }
   
   private ProcessorEngine() {
       _processorList = new FastList<Processor>();
       init();
   }
   
   /** NOTE: All of the registerProcessor commands should be put in
    * here. This should not be initialized twice.*/
   private void init() {
       if (init) return;
       init = true; //Don't init twice.

//      ---  NOTE: ALL PROCESSORS GO BELOW THIS LINE!!!  ---
       registerProcessor(new ProcessorViewStats());
       
       
//     ---  NOTE: ALL PROCESSORS GO ABOVE THIS LINE!!! ---
       _log.info("ProcessorEngine: Registered " + _processorList.size() + " Chat Processors.");
   }
   
   private void registerProcessor(Processor p) {
       _processorList.add(p);
       _log.info("ProcessorEngine: Registered " + p.getRegistryID());
   }
   
   /** Official Command run by Say2. Say2 sends itself to this
    * command to figure out if it should stop running or continue.
    * If checkMatch = true, Say2 will stop executing. Otherwise,
    * it will continue from the script. */
   public boolean checkMatch(Say2 check) {
       boolean ret = false;
       boolean com = false;
       for (Processor p: _processorList) {
           if (matches(check,p) && !com) {
               ret = p.processCommand(check);
               com = true; } //We need to only run once.
       }
       return ret;
   }
   
   public static boolean matches(Say2 check, Processor p) {
       String processed = "";
       int loc = -1;
       switch (p.getSearchType()) {
           case Processor.EXACT: //If Search string matches exactly
               if (check.getSay().length() != p.getSearchString().length())
                   return false; //Not exactly long enough, so it couldn't possibly match.
               processed = check.getSay();
               break;
           case Processor.EXACT_OR_BEGINNING: //If Search string matches exactly or starts out
               if (check.getSay().length() < p.getSearchString().length())
                   return false; //Not exactly long enough, so it couldn't possibly match.
               processed = getProcessedString(check.getSay(), 0, p.getSearchString().length());
               break;
           case Processor.BEGINNING: //If Search String matches the first part.
               if (check.getSay().length() + 1 < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               processed = getProcessedString(check.getSay(), 0, p.getSearchString().length());
               break;
           case Processor.MIDDLE_BEGINNING: //If Search String starts out, or is in the middle of the Say, but has at least one character after it.
               if (check.getSay().length() + 1 < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               loc = check.getSay().indexOf(p.getSearchString());
               if (loc >= 0 && loc + p.getSearchString().length() < check.getSay().length())
                   processed = getProcessedString(check.getSay(), loc, p.getSearchString().length());
               break;
           case Processor.MIDDLE_BOUND: //If Search String is in the middle of the Say, but has at least one character before and after it.
               if (check.getSay().length() + 2 < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               loc = check.getSay().indexOf(p.getSearchString());
               if (loc >= 1 && loc + p.getSearchString().length() < check.getSay().length())
                   processed = getProcessedString(check.getSay(), loc, p.getSearchString().length());
               break;
           case Processor.MIDDLE_UNBOUND: //If SearchString is anywhere in the say, period.
               if (check.getSay().length() < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               loc = check.getSay().indexOf(p.getSearchString());
               if (loc >= 0)
                   processed = getProcessedString(check.getSay(), loc, p.getSearchString().length());
               break;
           case Processor.MIDDLE_ENDING: //If SearchString is anywhere in the middle of the say, but has at least one character before it.
               if (check.getSay().length() + 1 < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               loc = check.getSay().indexOf(p.getSearchString());
               if (loc >= 1)
                   processed = getProcessedString(check.getSay(), loc, p.getSearchString().length());
               break;
           case Processor.ENDING: //If SearchString is at the end, with at least one character before it.
               if (check.getSay().length() + 1 < p.getSearchString().length())
                   return false; //Not long enough, so it couldn't possibly match.
               loc = check.getSay().indexOf(p.getSearchString());
               if (loc >= 1 && loc + p.getSearchString().length() == check.getSay().length())
                   processed = getProcessedString(check.getSay(), loc, p.getSearchString().length());
               break;
       }
       if (p.caseSensitive()) return processed.matches(p.getSearchString());
       return processed.equalsIgnoreCase(p.getSearchString());
   }
   
   /** A String is assigned as such:
    * 
    * 0123456789
    * THISSTRING
    * 
    * So when you call this script, if you want the last letter of the
    * string, you would need start = 9 [or String.length() -1], length = 1.*/
   public static String getProcessedString(String process, int start, int length) {
       String processed = "";
       if (start + length > process.length()) return "";
       for (int i = start; i < start + length; i++)
           processed += process.toCharArray()[i];
       return processed;
   }
}
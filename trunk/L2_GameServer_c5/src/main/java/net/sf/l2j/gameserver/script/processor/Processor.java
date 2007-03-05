package net.sf.l2j.gameserver.script.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.l2j.gameserver.clientpackets.Say2;

/**
 * 
 *
 * @author  Imper1um
 */
public interface Processor {
    final static Log _log = LogFactory.getLog(Processor.class.getName());
   public static final int EXACT = 0;
   public static final int EXACT_OR_BEGINNING = 1;
   public static final int BEGINNING = 2;
   public static final int MIDDLE_BEGINNING = 3;
   public static final int MIDDLE_BOUND = 4;
   public static final int MIDDLE_UNBOUND = 5;
   public static final int MIDDLE_ENDING = 6;
   public static final int ENDING = 7;
   
   /** NOTE: This should return true if Say2 should call return; */
   public boolean processCommand(Say2 input);
   
   /** Search Types:
    * [Examples: SearchString = this] 
    * 0 = Exact (this)
    * 1 = Beginning or Exact (this this*)                          [Eg: Commands]
    * 2 = Beginning (this*)                                        [Eg: Commands that require input]
    * 3 = Middle/Beginning (this* *this*) 
    * 4 = Middle Only (*this*) [Requires a char before and after]
    * 5 = Unbound Middle (this *this* *this this*)                 [Eg: Word Filters]
    * 6 = Middle/Ending (*this *this*) 
    * 7 = Ending Only (*this) */
   public int getSearchType();
   public String getSearchString();
   public boolean caseSensitive();
   public String getRegistryID();
}
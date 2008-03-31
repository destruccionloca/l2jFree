/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.script.faenor;

import java.util.Map;

import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.script.EngineInterface;
import net.sf.l2j.gameserver.script.Expression;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{
    private final static Log _log = LogFactory.getLog(FaenorInterface.class);
    private static FaenorInterface _instance;
    
    public NpcTable npcTable = NpcTable.getInstance();
    
    public static FaenorInterface getInstance()
    {
        if (_instance == null)
        {
            _instance = new FaenorInterface();
        }
        return _instance;
    }
    
    private FaenorInterface()
    {
    }

    /**
     * 
     * Adds a new Quest Drop to an NPC
     * 
     * @see net.sf.l2j.gameserver.script.EngineInterface#addQuestDrop(int)
     */
    public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
    {
        L2NpcTemplate npc = npcTable.getTemplate(npcID);
        if (npc == null)
        {
            throw new NullPointerException();
        }
        L2DropData drop = new L2DropData();
        drop.setItemId(itemID);
        drop.setMinDrop(min);
        drop.setMaxDrop(max);
        drop.setChance(chance);
        drop.setQuestID(questID);
        drop.addStates(states);
        addDrop(npc, drop, false);
    }

   /**
    * Adds a new drop to an NPC.  If the drop is sweep, it adds it to the NPC's Sweep category
    * If the drop is non-sweep, it creates a new category for this drop. 
    *  
    * @param npc
    * @param drop
    * @param sweep
    */
    public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
    {
        if(sweep)
            addDrop(npc, drop,-1);
        else
        {
            int maxCategory = -1;
            if (npc.getDropData()!=null)
            {
                for(L2DropCategory cat:npc.getDropData())
                {
                    if(maxCategory<cat.getCategoryType())
                        maxCategory = cat.getCategoryType();
                }
            }
            maxCategory++;
            npc.addDropData(drop, maxCategory);
        }
    }

   /**
    * Adds a new drop to an NPC, in the specified category.  If the category does not exist, 
    * it is created.  
    *  
    * @param npc
    * @param drop
    * @param sweep
    */
    public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
    {
       npc.addDropData(drop, category);
    }

    public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
    {
        EventDroplist.getInstance().addGlobalDrop(items, count, (int)(chance * L2DropData.MAX_CHANCE), range);
    }
    
    public void onPlayerLogin(String[] message, DateRange validDateRange)
    {
        Announcements.getInstance().addEventAnnouncement(validDateRange, message);
    }
    
    public void addPetData(int petID, int levelStart, int levelEnd, Map<String, String> stats)
    {
        try
        {
            BSFManager context = new BSFManager(); 
            
            context.eval("beanshell", "core", 0, 0, "double log1p(double d) { return Math.log1p(d); }"); 
            context.eval("beanshell", "core", 0, 0, "double pow(double d, double p) { return Math.pow(d,p); }");
            
            L2PetData[] petData = new L2PetData[levelEnd - levelStart + 1];
            int value           = 0;
            for (int level = levelStart; level <= levelEnd; level++)
            {
                petData[level - 1]  = new L2PetData();
                petData[level - 1].setPetID(petID);
                petData[level - 1].setPetLevel(level);
                
                context.declareBean("level", new Double(level), Double.TYPE); 
                for (String stat : stats.keySet())
                {
                    value = ((Number)Expression.eval(context, "beanshell", stats.get(stat))).intValue(); 
                    petData[level - 1].setStat(stat, value);
                }
                context.undeclareBean("level"); 
            }
        }
        catch (BSFException e)
        {
            _log.error(e.getMessage(),e); 
        }
    }
}

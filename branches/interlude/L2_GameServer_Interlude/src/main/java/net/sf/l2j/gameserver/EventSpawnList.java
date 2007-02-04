// Author neoDeviL

package net.sf.l2j.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Date;

import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.model.L2Spawn;

public class EventSpawnList
{

    private static Logger _log = Logger.getLogger(EventSpawnList.class.getName());

    private static EventSpawnList _instance;

    public static EventSpawnList getInstance()
    {
        if (_instance == null)
        {
            _instance = new EventSpawnList();
        }
        return _instance;
    }
    
    public static void addNewGlobalSpawn(int NpcId, int Xpos, int Ypos, int Zpos, int count, int Heading, int respavntime, DateRange DateRanges)
    {
            L2NpcTemplate template;
            Date currentDate = new Date();

        if (DateRanges.isWithinRange(currentDate))
        {
            template = NpcTable.getInstance().getTemplate(NpcId); 
                try
                {
                L2Spawn spawn = new L2Spawn(template);
                spawn.setLocx(Xpos);
                spawn.setLocy(Ypos);
                spawn.setLocz(Zpos);
                spawn.setAmount(count);
                spawn.setHeading(Heading);
                spawn.setRespawnDelay(respavntime);
                
                SpawnTable.getInstance().addNewSpawn(spawn, false);
                        spawn.init();
                    System.out.println("Global Spawn :: NPCId: "+NpcId+", Date Range From: "+DateRanges.getStartDate()+" To: "+DateRanges.getEndDate()+" Now: "+ currentDate);
                }
                catch (Exception e)
                {
                _log.log(Level.SEVERE, "error while creating npc spawn: "+ e);
                }
        }
    }
}
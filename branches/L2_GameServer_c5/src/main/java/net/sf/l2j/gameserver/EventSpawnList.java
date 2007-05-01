// Author neoDeviL

package net.sf.l2j.gameserver;

import java.util.Date;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EventSpawnList
{

    private static Log _log = LogFactory.getLog(EventSpawnList.class.getName());

    private static EventSpawnList _instance;

    public static EventSpawnList getInstance()
    {
        if (_instance == null)
        {
            _instance = new EventSpawnList();
        }
        return _instance;
    }

    public static void addNewGlobalSpawn(int NpcId, int Xpos, int Ypos, int Zpos, int count,
                                         int Heading, int respavntime, DateRange DateRanges)
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
                _log.info("Global Spawn :: NPCId: " + NpcId + ", Date Range From: "
                    + DateRanges.getStartDate() + " To: " + DateRanges.getEndDate() + " Now: "
                    + currentDate);
            }
            catch (Exception e)
            {
                _log.error("error while creating npc spawn: " + e);
            }
        }
    }
}
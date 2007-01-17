package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines,
 * when time is elapsed.
 * 
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
    private static final Log _log = LogFactory.getLog(TaskSevenSignsUpdate.class);
    
    public static final String NAME = "SevenSignsUpdate";
    
    public String getName()
    {
        return NAME;
    }

    public void onTimeElapsed(ExecutedTask task)
    {
        try {
            SevenSigns.getInstance().saveSevenSignsData(null, true);

            if (!SevenSigns.getInstance().isSealValidationPeriod())
                SevenSignsFestival.getInstance().saveFestivalData(false);
            
            _log.info("SevenSigns: Data updated successfully.");
        }
        catch (Exception e) {
            _log.error("SevenSigns: Failed to save Seven Signs configuration: " + e,e);
        }
    }
}

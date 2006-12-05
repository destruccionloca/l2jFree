/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

import org.apache.log4j.Logger;


/**
 * @author Layane
 *
 */
public abstract class Task
{
    private static Logger _log = Logger.getLogger(Task.class.getName());
    
    public void initializate()
    {
        if (_log.isDebugEnabled())
            _log.debug("Task" + getName() + " inializate");
    }
    
    public ScheduledFuture launchSpecial(ExecutedTask instance)
    {
        return null;
    }
    
    public abstract String getName();
    public abstract void onTimeElapsed(ExecutedTask task);
    
    public void onDestroy()
    {
    }
}

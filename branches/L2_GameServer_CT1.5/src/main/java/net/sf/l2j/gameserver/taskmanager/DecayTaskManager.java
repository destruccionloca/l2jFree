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
package net.sf.l2j.gameserver.taskmanager;

import java.util.NoSuchElementException;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;

/**
 * @author la2
 * Lets drink to code!
 */
public class DecayTaskManager
{
    protected FastMap<L2Character,Long> _decayTasks = new FastMap<L2Character,Long>().setShared(true);

    private static DecayTaskManager _instance;
    
    public DecayTaskManager()
    {
        ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(),10000,5000);
    }
    
    public static DecayTaskManager getInstance()
    {
        if(_instance == null)
            _instance = new DecayTaskManager();
        
        return _instance;
    }
    
    public void addDecayTask(L2Character actor)
    {
        _decayTasks.put(actor,System.currentTimeMillis());
    }

    public void addDecayTask(L2Character actor, int interval)
    {
        _decayTasks.put(actor,System.currentTimeMillis()+interval);
    }
    
    public void cancelDecayTask(L2Character actor)
    {
        try
        {
            _decayTasks.remove(actor);
        }
        catch(NoSuchElementException e){}
    }
    
    private class DecayScheduler implements Runnable
    {
        protected DecayScheduler()
        {
            // Do nothing
        }
        
        public void run()
        {
            Long current = System.currentTimeMillis();
            long forDecay = 0;
            if(_decayTasks.isEmpty()) return;
            for(L2Character actor : _decayTasks.keySet())
            {
                // [L2J_JP ADD SANDMAN]
                if(actor instanceof L2MonsterInstance)
                {
                    if(actor instanceof L2RaidBossInstance)
                        forDecay = 30000;
                    else
                    {
                        L2MonsterInstance monster = (L2MonsterInstance) actor;
                        switch(monster.getNpcId())
                        {
                            case 29028:     // Valakas
                                forDecay = 18000;
                                break;
                            case 29019:     // Antharas
                            case 29066:     // Antharas
                            case 29067:     // Antharas
                            case 29068:     // Antharas
                                forDecay = 12000;
                                break;
                            case 29014:     // Orfen
                                forDecay = 150000;
                                break;
                            case 29001:     // Queen Ant
                                forDecay = 150000;
                                break;
                            case 29046:     // Scarlet Van Halisha lvl 85 -> Morphing
                                forDecay = 2000;
                                break;
                            case 29045:     // Frintezza
                                forDecay = 9500;
                                break;
                            case 29047:     // Scarlet Van Halisha lvl 90
                                forDecay = 7500;
                                break;
                            default:
                                forDecay = 8500;
                        }
                    }
                }
                else
                    forDecay = 8500; // [L2J_JP EDIT END]

                if((current - _decayTasks.get(actor)) > forDecay)
                {
                    actor.onDecay();
                    _decayTasks.remove(actor);
                }
            }
        }
    }

    public String toString()
    {
        String ret = "============= DecayTask Manager Report ============\r\n";
        ret += "Tasks count: "+_decayTasks.size()+"\r\n";
        ret += "Tasks dump:\r\n";
        
        Long current = System.currentTimeMillis();
        for( L2Character actor : _decayTasks.keySet())
        {
            ret += "Class/Name: "+actor.getClass().getSimpleName()+"/"+actor.getName()
            +" decay timer: "+(current - _decayTasks.get(actor))+"\r\n";
        }
        
        return ret;
    }
}

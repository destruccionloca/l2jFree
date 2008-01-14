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
package net.sf.l2j.gameserver.boat.events;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.boat.service.BoatService;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.tools.L2Registry;

public class Boatrun implements Runnable
{
    private int            _state;

    private L2BoatInstance _boat;
    
    private static final BoatService boatService = (BoatService)L2Registry.getBean(IServiceRegistry.BOAT);
    
    /**
     * @param i
     * @param instance
     */
    public Boatrun(int i, L2BoatInstance instance)
    {
        _state = i;
        _boat = instance;
    }

    public void run()
    {
        _boat.setVehicleDeparture(null);
        _boat.setNeedOnVehicleCheckLocation(false);
        if (_boat.getCycle() == L2BoatInstance.TRAJET_WAY_1)
        {
            int time = boatService.getTimeState(_state, _boat, _boat.getTrajet1().getIdWaypoint1());
            if (time > 0)
            {
                _state++;
                Boatrun bc = new Boatrun(_state, _boat);
                ThreadPoolManager.getInstance().scheduleGeneral(bc, time);

            } else if (time == 0)
            {
                _boat.setCycle(L2BoatInstance.TRAJET_WAY_2);
                _boat.say(10);
                BoatCaptain bc = new BoatCaptain(1, _boat);
                ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
            } else
            {
                _boat.setNeedOnVehicleCheckLocation(true);
                _state++;
                _boat.setRunstate(_state);
            }
        } else if (_boat.getCycle() == L2BoatInstance.TRAJET_WAY_2)
        {
            int time = boatService.getTimeState(_state, _boat, _boat.getTrajet2().getIdWaypoint1());
            if (time > 0)
            {
                _state++;
                Boatrun bc = new Boatrun(_state, _boat);
                ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
            } else if (time == 0)
            {
                _boat.setCycle(L2BoatInstance.TRAJET_WAY_1);
                _boat.say(10);
                BoatCaptain bc = new BoatCaptain(1, _boat);
                ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
            } else
            {
                _boat.setNeedOnVehicleCheckLocation(true);
                _state++;
                _boat.setRunstate(_state);
            }
        }
    }
}
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.boat.events;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;

public class BoatCaptain implements Runnable
{
    private int            _state;

    private L2BoatInstance _boat;

    /**
     * @param i
     * @param instance
     */
    public BoatCaptain(int i, L2BoatInstance instance)
    {
        _state = i;
        _boat = instance;
    }

    public void run()
    {
        BoatCaptain bc;
        switch (_state)
        {
        case 1:
            _boat.say(L2BoatInstance.DEPARTURE_IN_5_MINUTES);
            bc = new BoatCaptain(2, _boat);
            ThreadPoolManager.getInstance().scheduleGeneral(bc, 240000);
            break;
        case 2:
            _boat.say(L2BoatInstance.DEPARTURE_IN_1_MINUTES);
            bc = new BoatCaptain(3, _boat);
            ThreadPoolManager.getInstance().scheduleGeneral(bc, 40000);
            break;
        case 3:
            _boat.say(L2BoatInstance.DEPARTURE);
            bc = new BoatCaptain(4, _boat);
            ThreadPoolManager.getInstance().scheduleGeneral(bc, 20000);
            break;
        case 4:
            _boat.say(-1);
            _boat.begin();
            break;
        }
    }
}
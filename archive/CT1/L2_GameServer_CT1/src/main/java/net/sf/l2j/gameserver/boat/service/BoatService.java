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
package net.sf.l2j.gameserver.boat.service;

import java.util.Collection;

import net.sf.l2j.gameserver.boat.dao.IBoatDAO;
import net.sf.l2j.gameserver.boat.dao.IBoatTrajetDAO;
import net.sf.l2j.gameserver.boat.model.L2BoatPoint;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;

/**
 * A service associated with the boat daos.
 *
 */
public class BoatService
{
	private IBoatDAO boatDAO;
    private IBoatTrajetDAO boatTrajetDAO;
	/**
     * Return the boat instance associated to this id
     * 
	 * @param boatId
	 * @return
	 */
	public L2BoatInstance getBoat(int boatId)
	{			
       return boatDAO.getBoat (boatId);
	}
    
    /**
     * @param state
     * @param boat
     * @return the time
     * 
     * time   : -  > 0:  if the boat has reached the point before the time is spent, the boat wait !
     *                    else if the time is spent and the boat has not reached the point, server will pass to the next point
     *           -  < 0:  lets server calculate when the boat reachs the point (/!\ don't use time < 0 when you do a rotation with the boat.)
     * 
     */
    public int getTimeState(int state, L2BoatInstance _boat, int idWayPoint)
    {
        if (state < boatTrajetDAO.getNumberOfBoatPoints(idWayPoint))
        {
            L2BoatPoint bp = boatTrajetDAO.getBoatPoint(idWayPoint,state);
            double dx = (bp.x - _boat.getX());
            double dy = (bp.y - _boat.getX());
            double distance = Math.sqrt(dx * dx + dy * dy);
            double cos;
            double sin;
            sin = dy / distance;
            cos = dx / distance;

            int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
            heading += 32768;
            _boat.getPosition().setHeading(heading);

            _boat.setVehicleDeparture ( new VehicleDeparture(_boat, bp.speed1, bp.speed2, bp.x, bp.y, bp.z));
            // _boat.getTemplate().baseRunSpd = bp.speed1;
            _boat.moveToLocation(bp.x, bp.y, bp.z, bp.speed1);
            Collection<L2PcInstance> knownPlayers = _boat.getKnownList().getKnownPlayers().values();
            if (knownPlayers == null || knownPlayers.isEmpty())
                return bp.time;
            for (L2PcInstance player : knownPlayers)
            {
                player.sendPacket(_boat.getVehicleDeparture());
            }
            if (bp.time == 0)
            {
                bp.time = 1;
            }
            return bp.time;
        }
        return 0;
    }    
    
    /**
     * Load boat data from dao
     */
    public void loadBoatDatas ()
    {
        boatDAO.load();
        boatTrajetDAO.load();
    }

    /**
     * @return the boatDAO
     */
    public IBoatDAO getBoatDAO()
    {
        return boatDAO;
    }

    /**
     * @param boatDAO the boatDAO to set
     */
    public void setBoatDAO(IBoatDAO boatDAO)
    {
        this.boatDAO = boatDAO;
    }

    /**
     * @return the boatTrajetDAO
     */
    public IBoatTrajetDAO getBoatTrajetDAO()
    {
        return boatTrajetDAO;
    }

    /**
     * @param boatTrajetDAO the boatTrajetDAO to set
     */
    public void setBoatTrajetDAO(IBoatTrajetDAO boatTrajetDAO)
    {
        this.boatTrajetDAO = boatTrajetDAO;
    }
}

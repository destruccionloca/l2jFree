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
package net.sf.l2j.gameserver.boat.dao;

import net.sf.l2j.gameserver.boat.model.L2BoatPoint;

/**
 * 
 * DAO to access to boat point. A point is a step in a trajet. 
 * 
 */
public interface IBoatTrajetDAO
{
    /**
     * return a L2BoatPoint associated with this id
     * @param boatPoint
     * @return a L2BoatPoint
     */
    public L2BoatPoint getBoatPoint (int idWayPoint,int boatPoint);

    /**
     * return the number of loaded boat points for a trajet
     * @return the number of boat points
     */
    public int getNumberOfBoatPoints (int idWayPoint);

    /**
     * return the number of loaded boat trajet
     * @return the number of boat trajets
     */
    public int getNumberOfBoatTrajet ();
    
    /**
     * load all boats trajet from data source (file or database)
     */
    public void load ();    

}

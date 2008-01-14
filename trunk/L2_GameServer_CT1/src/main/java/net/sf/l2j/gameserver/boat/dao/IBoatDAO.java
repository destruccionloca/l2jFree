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

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;

/**
 * 
 * DAO to access to boat 
 * 
 */
public interface IBoatDAO
{
    /**
     * return a L2BoatInstance associated with this id
     * @param boatId
     * @return a L2BoatInstance
     */
    public L2BoatInstance getBoat (int boatId);

    /**
     * return the number of loaded boats
     * @return the number of boat
     */
    public int getNumberOfBoat ();
    
    /**
     * load all boats from data source (file or database)
     */
    public void load ();

}

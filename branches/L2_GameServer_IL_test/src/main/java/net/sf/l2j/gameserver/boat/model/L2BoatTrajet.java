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
package net.sf.l2j.gameserver.boat.model;

/**
 * A boat trajet is :
 * - an id of waypoint, that we will use to find the way paths
 * - a ticket id
 * - a boat name 
 * - message for arrival and departure
 *
 */
public class L2BoatTrajet
{
    private int                            _IdWaypoint1;
    private int                            _IdWTicket1;
    private int                            _ntx1;
    private int                            _nty1;
    private int                            _ntz1;
    private String                         _boatname;
    private String                         _npc1;
    private String                         _sysmess10_1;
    private String                         _sysmess5_1;
    private String                         _sysmess1_1;
    private String                         _sysmessb_1;
    private String                         _sysmess0_1;

    
    
    /**
     * @param idWaypoint1
     * @param idWTicket1
     * @param ntx1
     * @param nty1
     * @param ntz1
     * @param idnpc1
     * @param sysmess10_1
     * @param sysmess5_1
     * @param sysmess1_1
     * @param sysmessb_1
     */
    public L2BoatTrajet(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String npc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1,
            String boatname)
    {
        _IdWaypoint1 = idWaypoint1;
        _IdWTicket1 = idWTicket1;
        _ntx1 = ntx1;
        _nty1 = nty1;
        _ntz1 = ntz1;
        _npc1 = npc1;
        _sysmess10_1 = sysmess10_1;
        _sysmess5_1 = sysmess5_1;
        _sysmess1_1 = sysmess1_1;
        _sysmessb_1 = sysmessb_1;
        _sysmess0_1 = sysmess0_1;
        _boatname = boatname;
    }



    /**
     * @return the _boatname
     */
    public String getBoatname()
    {
        return _boatname;
    }



    /**
     * @param _boatname the _boatname to set
     */
    public void setBoatname(String _boatname)
    {
        this._boatname = _boatname;
    }



    /**
     * @return the _IdWaypoint1
     */
    public int getIdWaypoint1()
    {
        return _IdWaypoint1;
    }



    /**
     * @param idWaypoint1 the _IdWaypoint1 to set
     */
    public void setIdWaypoint1(int idWaypoint1)
    {
        _IdWaypoint1 = idWaypoint1;
    }



    /**
     * @return the _IdWTicket1
     */
    public int getIdWTicket1()
    {
        return _IdWTicket1;
    }



    /**
     * @param idWTicket1 the _IdWTicket1 to set
     */
    public void setIdWTicket1(int idWTicket1)
    {
        _IdWTicket1 = idWTicket1;
    }



    /**
     * @return the _npc1
     */
    public String getNpc1()
    {
        return _npc1;
    }



    /**
     * @param _npc1 the _npc1 to set
     */
    public void setNpc1(String _npc1)
    {
        this._npc1 = _npc1;
    }



    /**
     * @return the _ntx1
     */
    public int getNtx1()
    {
        return _ntx1;
    }



    /**
     * @param _ntx1 the _ntx1 to set
     */
    public void setNtx1(int _ntx1)
    {
        this._ntx1 = _ntx1;
    }



    /**
     * @return the _nty1
     */
    public int getNty1()
    {
        return _nty1;
    }



    /**
     * @param _nty1 the _nty1 to set
     */
    public void setNty1(int _nty1)
    {
        this._nty1 = _nty1;
    }



    /**
     * @return the _ntz1
     */
    public int getNtz1()
    {
        return _ntz1;
    }



    /**
     * @param _ntz1 the _ntz1 to set
     */
    public void setNtz1(int _ntz1)
    {
        this._ntz1 = _ntz1;
    }



    /**
     * @return the _sysmess0_1
     */
    public String getSysmess0_1()
    {
        return _sysmess0_1;
    }



    /**
     * @param _sysmess0_1 the _sysmess0_1 to set
     */
    public void setSysmess0_1(String _sysmess0_1)
    {
        this._sysmess0_1 = _sysmess0_1;
    }



    /**
     * @return the _sysmess1_1
     */
    public String getSysmess1_1()
    {
        return _sysmess1_1;
    }



    /**
     * @param _sysmess1_1 the _sysmess1_1 to set
     */
    public void setSysmess1_1(String _sysmess1_1)
    {
        this._sysmess1_1 = _sysmess1_1;
    }



    /**
     * @return the _sysmess10_1
     */
    public String getSysmess10_1()
    {
        return _sysmess10_1;
    }



    /**
     * @param _sysmess10_1 the _sysmess10_1 to set
     */
    public void setSysmess10_1(String _sysmess10_1)
    {
        this._sysmess10_1 = _sysmess10_1;
    }



    /**
     * @return the _sysmess5_1
     */
    public String getSysmess5_1()
    {
        return _sysmess5_1;
    }



    /**
     * @param _sysmess5_1 the _sysmess5_1 to set
     */
    public void setSysmess5_1(String _sysmess5_1)
    {
        this._sysmess5_1 = _sysmess5_1;
    }



    /**
     * @return the _sysmessb_1
     */
    public String getSysmessb_1()
    {
        return _sysmessb_1;
    }



    /**
     * @param _sysmessb_1 the _sysmessb_1 to set
     */
    public void setSysmessb_1(String _sysmessb_1)
    {
        this._sysmessb_1 = _sysmessb_1;
    }


}

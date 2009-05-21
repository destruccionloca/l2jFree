/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.loginserver.services;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.loginserver.beans.Gameservers;
import net.sf.l2j.loginserver.dao.GameserversDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Account service to handle gameservers management
 * 
 */
public class GameserversServices
{
    private static Log _log = LogFactory.getLog(GameserversServices.class);
    
    private GameserversDAO __dao = null;
    
    public void setGameserversDAO (GameserversDAO dao)
    {
        __dao =  dao;
    }
    
    /**
     * Return list of gameservers
     * @return 
     */
    public List<Gameservers> getAllGameservers ()
    {
        try
        {
            List<Gameservers> servers = __dao.getAllGameservers();
            return servers;
        } 
        catch (ObjectRetrievalFailureException e)
        {
            _log.warn("Unable to retrieve gameservers.",e);
            return new ArrayList<Gameservers>();
        }
    }
    
    /**
     * 
     * @param id - the server id
     * @return  the server name or null if no server was found
     */ 
    public String getGameserverName (int id)
    {
        try
        {
            return __dao.getGameserverByServerId(id).getServerName();
        }
        catch (DataAccessException e)
        {
            _log.warn("Unable to retrieve gameserver : " + id,e);
            return null;
        }
    }
    
    /**
     * 
     * @param gs
     * @return id of created server or -1 if server was not created.
     */
    public int createGameserver (Gameservers gs)
    {
        try
        {
            return __dao.createGameserver(gs);
        }
        catch (DataAccessException e)
        {
            _log.warn("Unable to create gameserver.",e);
            return -1;
        }
    }
    
    /**
     * 
     * @param id
     */
    public void deleteGameserver(int id)
    {
        try
        {
            __dao.removeGameserverByServerId(id);
        }
        catch (DataAccessException e)
        {
            _log.warn("Error while deleting gameserver :" + e, e);
        }
    }    
}

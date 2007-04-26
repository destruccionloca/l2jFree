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
package net.sf.l2j.loginserver.dao.impl;

import java.util.Collection;
import java.util.List;

import net.sf.l2j.loginserver.beans.Gameservers;
import net.sf.l2j.loginserver.dao.GameserversDAO;

import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * DAO object for domain model class Gameservers.
 * @see net.sf.l2j.loginserver.beans.Gameservers
 */
public class GameserversDAOHib extends BaseRootDAOHib implements GameserversDAO
{
    //private static final Log _log = LogFactory.getLog(AccountsDAOHib.class);

    /**
     * Search by id
     * @param id
     * @return
     */
    public Gameservers getGameserverByServerId(int id)
    {
        Gameservers gameserver = (Gameservers) get(Gameservers.class, id);
        if ( gameserver == null )
            throw new ObjectRetrievalFailureException("Gameserver",id);
        return gameserver;
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createGameserver(Gameservers)
     */
    public int createGameserver(Gameservers obj)
    {
        return (Integer)save(obj);
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createOrUpdate(Gameservers)
     */
    public void createOrUpdate(Gameservers obj)
    {
        saveOrUpdate(obj);
        
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createOrUpdateAll(java.util.Collection)
     */
    public void createOrUpdateAll(Collection entities)
    {
        saveOrUpdateAll(entities);
        
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#getAllGameservers()
     */
    @SuppressWarnings("unchecked")
    public List <Gameservers> getAllGameservers()
    {
        return findAllOrderById(Gameservers.class);
    }
    
    /**
     * Return all objects related to the implementation of this DAO with no filter.
     */
    public List findAllOrderById (Class refClass) {
        return getCurrentSession().createQuery("from " + refClass.getName() +" order by serverId").list();
    }    

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeGameservers(Gameservers)
     */
    public void removeGameserver(Gameservers obj)
    {
        delete(obj);
        
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeAccountById(java.io.Serializable)
     */
    public void removeGameserverByServerId(int id)
    {
        removeObject(Gameservers.class, id);        
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeAll()
     */
    public void removeAll()
    {
    	removeAll(getAllGameservers());
    }
}

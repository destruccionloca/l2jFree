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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.l2j.loginserver.beans.Gameservers;
import net.sf.l2j.loginserver.dao.GameserversDAO;

import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * DAO object for domain model class Gameservers.
 * @see net.sf.l2j.loginserver.beans.Gameservers
 */
public class GameserversDAOMock  implements GameserversDAO
{
    private Map<Integer,Gameservers> referential = new HashMap<Integer,Gameservers>();
    
    public GameserversDAOMock()
    {
        referential.put(1, new Gameservers(1,"548545","*"));
    }
    

    /**
     * Search by id
     * @param id
     * @return
     */
    public Gameservers getGameserverByServerId(int id)
    {
        if ( ! referential.containsKey(id))
        {
            throw new ObjectRetrievalFailureException ("Gameservers",id);
        }
        return referential.get(id);
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createGameserver(java.lang.Object)
     */
    public int createGameserver(Gameservers obj)
    {
        referential.put(obj.getServerId(),obj);
        return obj.getServerId();
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createOrUpdate(java.lang.Object)
     */
    public void createOrUpdate(Gameservers obj)
    {
        createGameserver(obj);
        
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#createOrUpdateAll(java.util.Collection)
     */
    public void createOrUpdateAll(Collection entities)
    {
        Iterator it = entities.iterator();
        while (it.hasNext())
        {
            createGameserver((Gameservers)it.next());
        }
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#getAlGameservers()
     */
    public List <Gameservers> getAllGameservers()
    {
        return new ArrayList<Gameservers> (referential.values());
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeGameserver(java.lang.Object)
     */
    public void removeGameserver(Gameservers obj)
    {
        referential.remove(obj.getServerId());
        
    }

    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeGameserverById(java.io.Serializable)
     */
    public void removeGameserverByServerId(int id)
    {
        referential.remove(id);
    }


    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#removeAll(java.util.Collection)
     */
    public void removeAll(Collection entities)
    {
        Iterator it = entities.iterator();
        while (it.hasNext())
        {
            removeGameserver((Gameservers)it.next());
        }        
    }


    /**
     * @see net.sf.l2j.loginserver.dao.GameserversDAO#update(java.lang.Object)
     */
    public void update(Object obj)
    {
        Gameservers acc = (Gameservers)obj;
        removeGameserverByServerId(acc.getServerId());
        createGameserver(acc);
        
    }

}

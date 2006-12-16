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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Facade for Hibernate DAO
 * 
 * @version $Revision: $ $Date: $
 */
public abstract class BaseRootDAOHib extends HibernateDaoSupport
{

    
    /**
     * Load object matching the given key and return it.
     */
    public Object load(Class refClass, Serializable key) {
       Object obj = getHibernateTemplate().get(refClass, key);
       return obj;
    }

    /**
     * Return all objects related to the implementation of this DAO with no filter.
     */
    public List findAll (Class refClass) {
        return getHibernateTemplate().loadAll(refClass);
    }
    
    
    /**
     * Persist the given transient instance, first assigning a generated identifier.
     * (Or using the current value of the identifier property if the assigned generator is used.)
     * @throws DataIntegrityViolationException - error in insertion
     */
    public Serializable save(Object obj) 
    {
        Serializable ser = getHibernateTemplate().save(obj);
        return ser;
    }

    /**
     * Either save() or update() the given instance, depending upon the value of its
     * identifier property.
     */
    public void saveOrUpdate(Object obj) {
        getHibernateTemplate().saveOrUpdate(obj);
    }
    
    /**
     * Either save() or update() the given instances, depending upon the value of its
     * identifier property.
     */
    public void saveOrUpdateAll(Collection entities) {
        getHibernateTemplate().saveOrUpdateAll(entities);
    }
    

    /**
     * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
     * instance with the same identifier in the current session.
     * @param obj a transient instance containing updated state
     */
    public void update(Object obj) {
        getHibernateTemplate().update(obj);
    }

    /**
     * Delete an object.
     */
    public void delete(Object obj) {
        getHibernateTemplate().delete(obj);
    }
    
    /**
     * Delete a collection.
     */
    public void removeAll(Collection entities) {
        getHibernateTemplate().deleteAll(entities);
    }


    /**
     * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
     * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
     */
    public void refresh(Object obj) {
        getHibernateTemplate().refresh(obj);
    }
    
    /**
     * Get an object
     */
    public Object get(Class clazz, Serializable id) {
        Object  o = getHibernateTemplate().get(clazz, id);
        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }

        return o;
    }


    /**
     * Delete an object by id
     */
    public void removeObject(Class clazz, Serializable id) {
        getHibernateTemplate().delete(get(clazz, id));
    }    

}

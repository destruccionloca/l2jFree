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

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 */
public abstract class BaseRootDAOHib extends HibernateDaoSupport
{
    /**
     * Load object matching the given key and return it.
     */
    public Object load(Class refClass, Serializable key) {
       return this.getHibernateTemplate().get(refClass, key);
    }

    /**
     * Return all objects related to the implementation of this DAO with no filter.
     */
    public java.util.List findAll (Class refClass) {
        return this.getHibernateTemplate().loadAll(refClass);
    }
    
    
    /**
     * Persist the given transient instance, first assigning a generated identifier.
     * (Or using the current value of the identifier property if the assigned generator is used.)
     */
    public Serializable save(Object obj) 
    {
        return this.getHibernateTemplate().save(obj);
    }

    /**
     * Either save() or update() the given instance, depending upon the value of its
     * identifier property.
     */
    public void saveOrUpdate(Object obj) {
        this.getHibernateTemplate().saveOrUpdate(obj);
    }

    /**
     * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
     * instance with the same identifier in the current session.
     * @param obj a transient instance containing updated state
     */
    public void update(Object obj) {
        this.getHibernateTemplate().update(obj);
    }

    /**
     * Delete an object.
     */
    public void delete(Object obj) {
        this.getHibernateTemplate().delete(obj);
    }

    /**
     * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
     * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
     */
    public void refresh(Object obj) {
        this.getHibernateTemplate().refresh(obj);
    }

}

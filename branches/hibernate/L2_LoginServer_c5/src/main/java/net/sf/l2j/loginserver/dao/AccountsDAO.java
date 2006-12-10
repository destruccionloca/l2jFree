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
package net.sf.l2j.loginserver.dao;

import java.io.Serializable;

import net.sf.l2j.loginserver.beans.Accounts;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 */
public interface AccountsDAO
{
    /**
     * Load object matching the given key and return it.
     */
    public Object load(Class refClass, Serializable key) ;

    /**
     * Return all objects related to the implementation of this DAO with no filter.
     */
    public java.util.List findAll (Class refClass) ;
    
    
    /**
     * Persist the given transient instance, first assigning a generated identifier.
     * (Or using the current value of the identifier property if the assigned generator is used.)
     */
    public Serializable save(Object obj);

    /**
     * Either save() or update() the given instance, depending upon the value of its
     * identifier property.
     */
    public void saveOrUpdate(Object obj);

    /**
     * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
     * instance with the same identifier in the current session.
     * @param obj a transient instance containing updated state
     */
    public void update(Object obj);

    /**
     * Delete an object.
     */
    public void delete(Object obj);

    /**
     * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
     * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
     */
    public void refresh(Object obj) ;
    
    /**
     * Search by id
     * @param id the id  (login)
     * @return the account
     */
    public Accounts findById(java.lang.String id);
    
}

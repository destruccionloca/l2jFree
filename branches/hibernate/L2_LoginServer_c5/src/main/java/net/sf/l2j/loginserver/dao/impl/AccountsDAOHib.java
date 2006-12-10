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

import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.dao.AccountsDAO;

import org.apache.log4j.Logger;

/**
 * DAO object for domain model class Accounts.
 * @see net.sf.l2j.loginserver.beans.Accounts
 */
public class AccountsDAOHib extends BaseRootDAOHib implements AccountsDAO
{
    private static final Logger log = Logger.getLogger(AccountsDAOHib.class);

    /**
     * Search by id
     * @param id
     * @return
     */
    public Accounts findById(java.lang.String id)
    {
        log.debug("getting Accounts instance with id: " + id);
        Accounts instance = (Accounts) load(net.sf.l2j.loginserver.beans.Accounts.class, id);
        
        if (instance == null)
        {
            if ( log.isDebugEnabled()) log.debug("get successful, no instance found");
        }
        else
        {
            if ( log.isDebugEnabled()) log.debug("get successful, instance found");
        }
        return instance;
    }
}

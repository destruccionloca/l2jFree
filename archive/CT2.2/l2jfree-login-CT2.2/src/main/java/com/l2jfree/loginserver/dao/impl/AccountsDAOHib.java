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
package com.l2jfree.loginserver.dao.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.l2jfree.loginserver.beans.Accounts;
import com.l2jfree.loginserver.dao.AccountsDAO;

/**
 * DAO object for domain model class Accounts.
 * @see com.l2jfree.loginserver.beans.Accounts
 */
public class AccountsDAOHib extends BaseRootDAOHib implements AccountsDAO
{
	//private static final Log _log = LogFactory.getLog(AccountsDAOHib.class);

	/**
	 * Search by id
	 * @param id
	 * @return
	 */
	public Accounts getAccountById(String id)
	{
		Accounts account = (Accounts) get(Accounts.class, id);
		if (account == null)
			throw new ObjectRetrievalFailureException("Accounts", id);
		return account;
	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#createAccount(java.lang.Object)
	 */
	public String createAccount(Object obj)
	{
		return (String) save(obj);
	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#createOrUpdate(java.lang.Object)
	 */
	public void createOrUpdate(Object obj)
	{
		saveOrUpdate(obj);

	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#createOrUpdateAll(java.util.Collection)
	 */
	public void createOrUpdateAll(Collection<?> entities)
	{
		saveOrUpdateAll(entities);

	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#getAllAccounts()
	 */
	@SuppressWarnings("unchecked")
	public List<Accounts> getAllAccounts()
	{
		return (List<Accounts>)findAll(Accounts.class);
	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#removeAccount(java.lang.Object)
	 */
	public void removeAccount(Object obj)
	{
		delete(obj);

	}

	/**
	 * @see com.l2jfree.loginserver.dao.AccountsDAO#removeAccountById(java.io.Serializable)
	 */
	public void removeAccountById(String login)
	{
		removeObject(Accounts.class, login);
	}
}

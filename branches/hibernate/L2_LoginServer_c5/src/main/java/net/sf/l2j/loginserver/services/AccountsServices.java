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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.dao.AccountsDAO;
import net.sf.l2j.loginserver.services.exception.AccountModificationException;
import net.sf.l2j.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Account service to handle account management
 * 
 */
public class AccountsServices
{
    private static Log _log = LogFactory.getLog(AccountsServices.class);
    
    private AccountsDAO __accDAO = null;
    
    public void setAccountsDAO (AccountsDAO accDAO)
    {
        __accDAO =  accDAO;
    }
    
    /**
     * Add or update an account
     * @param account
     * @param password
     * @param level
     * @return the new account
     * @throws AccountModificationException 
     */
    public Accounts addOrUpdateAccount(String account, String password, String level) 
    throws AccountModificationException
    {
        // o initialization
        // ---------------
        Accounts acc = null;
        
        // o Encode Password
        // ----------------
        MessageDigest md;
        byte[] newpass;
        try
        {
            md = MessageDigest.getInstance("SHA");
            newpass = password.getBytes("UTF-8");
            newpass = md.digest(newpass);
        }
        catch (NoSuchAlgorithmException e1)
        {
            throw new AccountModificationException ("No algorithm to encode password.",e1);
        }
        catch (UnsupportedEncodingException e1)
        {
            throw new AccountModificationException ("Unsupported encoding.",e1);
        }
        
        // o update account
        // ---------------
        try
        {
            acc = new Accounts ();
            Integer iLevel = new Integer (level);
            acc.setLogin(account);
            acc.setAccessLevel(iLevel);
            acc.setPassword(Base64.encodeBytes(newpass));
            __accDAO.createOrUpdate(acc);
            if (_log.isDebugEnabled()) _log.info("Account " + account + " has been updated.");
        }
        catch (NumberFormatException e)
        {
            throw new AccountModificationException ("Error : level ("+level+") should be an integer.",e);
        }
        return acc;
    }

    /**
     * Change account level
     * @param account - the account to upadte
     * @param level - the new level
     * @throws AccountModificationException
     */
    public void changeAccountLevel(String account, String level) 
    throws AccountModificationException
    {
        // Search account
        // ---------------
        Accounts acc = __accDAO.getAccountById(account);
        
        if ( acc == null )
            throw new AccountModificationException("Account "+account+" doesn't exist.");

        // Update account
        // --------------
        try
        {
            Integer iLevel = new Integer (level);
            acc.setAccessLevel(iLevel);
            __accDAO.createOrUpdate(acc);
            if (_log.isDebugEnabled()) _log.info("Account " + account + " has been updated.");
        }
        catch (NumberFormatException e)
        {
            throw new AccountModificationException ("Error : level ("+level+") should be an integer.",e);
        }
    }

    /**
     * Delete account and all linked objects
     * @param account
     * @throws AccountModificationException 
     */
    public void deleteAccount(String account) throws AccountModificationException 
    {
        // Search and delete account
        // ---------------
        Accounts acc = __accDAO.getAccountById(account);
        __accDAO.removeAccount(acc);
    }
    

    /**
     * Get accounts information
     *
     */
    public List<Accounts> getAccountsInfo()
    {
        List<Accounts> list = __accDAO.getAllAccounts();
        return list;
    }    
    
    
}

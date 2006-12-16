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

import net.sf.l2j.Base64;
import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.dao.AccountsDAO;
import net.sf.l2j.loginserver.services.exception.AccountModificationException;

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
        // Search account
        // ---------------
        Accounts acc = __accDAO.getAccountById(account);
        
        if ( acc == null )
            throw new AccountModificationException("Account "+account+" doesn't exist.");
        
        __accDAO.removeAccount(acc);
        // remove all from clan
        

//            // Get Accounts ID
//            ResultSet rcln;
//            statement = con.prepareStatement("SELECT obj_Id, char_name, clanid FROM characters WHERE account_name=?;");
//            statement.setEscapeProcessing(true);
//            statement.setString(1, account);
//            rset = statement.executeQuery();
//            while (rset.next())
//            {
//                System.out.println("Deleting character " + rset.getString("char_name") + ".");
//
//                // Check If clan leader Remove Clan and remove all from it
//                statement = con.prepareStatement("SELECT COUNT(*) FROM clan_data WHERE leader_id=?;");
//                statement.setString(1, rset.getString("clanid"));
//                rcln = statement.executeQuery();
//                rcln.next();
//                if (rcln.getInt(1) > 0)
//                {
//                    rcln.close();
//                    // Clan Leader
//
//                    // Get Clan Name
//                    statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE leader_id=?;");
//                    statement.setString(1, rset.getString("clanid"));
//                    rcln = statement.executeQuery();
//                    rcln.next();
//
//                    System.out.println("Deleting clan " + rcln.getString("clan_name") + ".");
//
//                    // Delete Clan Wars
//                    statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?;");
//                    statement.setEscapeProcessing(true);
//                    statement.setString(1, rcln.getString("clan_name"));
//                    statement.setString(2, rcln.getString("clan_name"));
//                    statement.executeUpdate();
//
//                    rcln.close();
//
//                    // Remove All From clan
//                    statement = con.prepareStatement("UPDATE characters SET clanid=0 WHERE clanid=?;");
//                    statement.setString(1, rset.getString("clanid"));
//                    statement.executeUpdate();
//
//                    // Delete Clan
//                    statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?;");
//                    statement.setString(1, rset.getString("clanid"));
//                    statement.executeUpdate();
//
//                }
//                else
//                {
//                    rcln.close();
//                }
//
//                // skills
//                statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // shortcuts 
//                statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // items 
//                statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // recipebook 
//                statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // quests 
//                statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // macroses
//                statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // friends
//                statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // merchant_lease 
//                statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//                // boxaccess
//                statement = con.prepareStatement("DELETE FROM boxaccess WHERE charname=?;");
//                statement.setString(1, rset.getString("char_name"));
//                statement.executeUpdate();
//
//                // characters 
//                statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?;");
//                statement.setString(1, rset.getString("obj_Id"));
//                statement.executeUpdate();
//
//            }
//
//            // Delete Account
//            statement = con.prepareStatement("DELETE FROM accounts WHERE login=?;");
//            statement.setEscapeProcessing(true);
//            statement.setString(1, account);
//            statement.executeUpdate();
//
//            System.out.println("Account " + account + " has been deleted.");
//
//        }
//        else
//        {
//            // Not Exist      
//            System.out.println("Account " + account + " does not exist.");
//        }
//
//        // Close Connection
//        statement.close();
//    }
    }
    

    /**
     * Print all accounts information (login + level)
     *
     */
    public void printAccInfo()
    {
        List<Accounts> list = __accDAO.getAllAccounts();

        for (Accounts account : list)
        {
            System.out.println(account.getLogin() + " -> " + account.getAccessLevel());
        }
        System.out.println("Number of accounts: " + list.size() + ".");
    }    
    
    
}

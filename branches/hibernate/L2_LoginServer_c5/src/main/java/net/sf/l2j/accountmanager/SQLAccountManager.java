/*
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
package net.sf.l2j.accountmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2ApplicationContext;
import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.dao.AccountsDAO;

/**
 * This class SQL Account Manager
 * 
 * @author netimperia
 * @version $Revision: 2.3.2.1.2.3 $ $Date: 2005/08/08 22:47:12 $
 */
public class SQLAccountManager
{
    private static String _uname = "";
    private static String _pass = "";
    private static String _level = "";
    private static String _mode = "";
    private LineNumberReader _in=null;

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException
    {
        // Load config
        Config.load();
        // load application context
        L2ApplicationContext.getInstance();

        SQLAccountManager accountManager = new SQLAccountManager();

        boolean bContinue = true;
        while (bContinue)
        {
            bContinue = accountManager.displayMenu();
        }

    }

    /**
     * Init interface with user 
     */
    public SQLAccountManager()
    {
        _in = new LineNumberReader(new InputStreamReader(System.in));
    }
    
    /**
     * Display menu and return true or false if we should continue
     * @return true or false if user want to stop
     * @throws IOException 
     */
    private boolean displayMenu() throws IOException
    {
        boolean bResponse = true;

        // o display user choices
        // ---------------------
        displayChoices();
        
        // 
        if (_mode.equals("1"))
        {
            // Add or Update
            AddOrUpdateAccount(_uname, _pass, _level);
        }
        else if (_mode.equals("2"))
        {
            // Change Level
            ChangeAccountLevel(_uname, _level);
        }
        else if (_mode.equals("3"))
        {
            // Delete
            System.out.print("Do you really want to delete this account ? Y/N : ");
            String yesno = _in.readLine();
            if (yesno.equals("Y"))
            {
                // Yes      
                DeleteAccount(_uname);
            }

        }
        else if (_mode.equals("4"))
        {
            // List
            printAccInfo();
        }
        else if (_mode.equals("5"))
        {
            bResponse = false;
        }

        return bResponse;
    }

    /**
     * Display menu and user choices
     * @throws IOException 
     * 
     */
    private void displayChoices() throws IOException
    {
        _mode = "";
        System.out.println("Please choose an option:");
        System.out.println("");
        System.out.println("1 - Create new account or update existing one (change pass and access level).");
        System.out.println("2 - Change access level.");
        System.out.println("3 - Delete existing account.");
        System.out.println("4 - List accounts & access levels.");
        System.out.println("5 - Exit.");
        while (!(_mode.equals("1") || _mode.equals("2") || _mode.equals("3") || _mode.equals("4") || _mode.equals("5")))
        {
            System.out.print("Your choice: ");
            _mode = _in.readLine();
        }

        if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
        {
            if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
                while (_uname.length() == 0)
                {
                    System.out.print("Username: ");
                    _uname = _in.readLine();
                }

            if (_mode.equals("1")) while (_pass.length() == 0)
            {
                System.out.print("Password: ");
                _pass = _in.readLine();
            }

            if (_mode.equals("1") || _mode.equals("2")) while (_level.length() == 0)
            {
                System.out.print("Access level: ");
                _level = _in.readLine();
            }
        }
    }

    /**
     * Print all accounts information (login + level)
     *
     */
    private void printAccInfo()
    {
        AccountsDAO accDAO = (AccountsDAO) L2ApplicationContext.getInstance().getApplicationContext().getBean("AccountsDAO");
        List<Accounts> list = accDAO.findAll(Accounts.class);

        for (Accounts account : list)
        {
            System.out.println(account.getLogin() + " -> " + account.getAccessLevel());
        }
        System.out.println("Number of accounts: " + list.size() + ".");
    }

    /**
     * Add or update an account
     * @param account
     * @param password
     * @param level
     * @throws IOException
     */
    private void AddOrUpdateAccount(String account, String password, String level) 
    throws IOException
    {
        AccountsDAO accDAO = (AccountsDAO) L2ApplicationContext.getInstance()
                                                                .getApplicationContext()
                                                                .getBean("AccountsDAO");
        
        // Encode Password		
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
            System.out.println("Account " + account + " could not be update :" + e1.getMessage());
            return;
        }
        
        
        // Search account
        // ---------------
        Accounts acc = accDAO.findById("account");
        if ( acc == null )
        {
            acc = new Accounts ();
        }
        
        // update account
        // ---------------
        try
        {
            Integer iLevel = new Integer (level);
            acc.setAccessLevel(iLevel);
            acc.setPassword(Base64.encodeBytes(newpass));
            accDAO.saveOrUpdate(acc);
            System.out.println("Account " + account + " has been updated.");
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error : level ("+level+") should be an integer.");
        }
    }

    /**
     * Change account level
     * @param account - the account to upadte
     * @param level - the new level
     */
    private void ChangeAccountLevel(String account, String level)
    {
        AccountsDAO accDAO = (AccountsDAO) L2ApplicationContext.getInstance()
                                                               .getApplicationContext()
                                                               .getBean("AccountsDAO");
        // Search account
        // ---------------
        Accounts acc = accDAO.findById("account");
        
        if ( acc == null )
        {
            System.out.println("Account " + account + " does not exist.");
        }
        else
        {
            // Update account
            // --------------
            try
            {
                Integer iLevel = new Integer (level);
                acc.setAccessLevel(iLevel);
                accDAO.save(acc);
                System.out.println("Account " + account + " has been updated.");
            }
            catch (NumberFormatException e)
            {
                System.out.println("Error : level ("+level+") should be an integer.");
            }
        }
    }

    /**
     * Delete account and all linked objects
     * @param account
     * @throws SQLException
     */
    private void DeleteAccount(String account) 
    {
        AccountsDAO accDAO = (AccountsDAO) L2ApplicationContext.getInstance()
                                                                .getApplicationContext()
                                                                .getBean("AccountsDAO");
        // Search account
        // ---------------
        Accounts acc = accDAO.findById("account");
        
        if ( acc == null )
        {
            System.out.println("Account " + account + " does not exist.");
        }
        else
        {
            accDAO.delete(acc);
        }

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
}

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

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.loginserver.beans.Accounts;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;

/**
 * Test account DAO
 * 
 */
public class AccountsDAOTest extends DatabaseTestCase
{
    private Accounts account = null;
    private AccountsDAO dao = null;
    
    private ClassPathXmlApplicationContext context = null;

    public void setAccountDao(AccountsDAO _dao) {
        this.dao = _dao;
    }
    
    protected IDataSet getDataSet() throws Exception {
        return new XmlDataSet(this.getClass().getResourceAsStream("accounts.xml"));

    }

    protected IDatabaseConnection getConnection() throws Exception 
    {
        DatabaseDataSourceConnection ddsc = (DatabaseDataSourceConnection) context.getBean("DatabaseDataSourceConnection");

        return ddsc;
    }    
    protected void setUp() throws Exception {

        context = new ClassPathXmlApplicationContext(
                "classpath*:/**/dao/applicationContext-*.xml");
        setAccountDao ( (AccountsDAO) context.getBean("AccountsDAO"));

        super.setUp();
    }    
    
    public void testFindAccount() throws Exception {
        account = dao.getAccountById("player1");

        assertEquals("player1", account.getLogin());
        assertEquals(4, account.getAccessLevel().intValue());
    }    
    
    public void testModifyAccount() throws Exception {
        // retrieve object
        account = dao.getAccountById("player1");

        assertEquals("player1", account.getLogin());
        assertEquals(4, account.getAccessLevel().intValue());
        
        // modify object
        account.setAccessLevel(7);
        dao.createOrUpdate(account);
        
        
        // check modification
        account = dao.getAccountById("player1");
        assertEquals("player1", account.getLogin());
        assertEquals(7, account.getAccessLevel().intValue());
        
        // cancel modification
        account.setAccessLevel(4);
        dao.createOrUpdate(account);
    }    


    public void testAddAndRemoveAccounts() throws Exception {
        
        // Add account
        account = new Accounts();
        account.setLogin("Bill");
        account.setPassword("testPw");
        account.setEmail("toto@test.com");
        
        dao.createAccount(account);

        assertEquals(account.getLogin(), "Bill");
        
        // delete account
        dao.removeAccount(account);

        try {
            account = dao.getAccountById("Bill");
            fail("Accounts found in database");
        } catch (DataAccessException dae) {
            assertNotNull(dae);
        }
    }
    
   public void testFindNonExistentAccount() throws Exception {

        try {
            account = dao.getAccountById("Unknown");
            fail("Accounts found in database");
        } catch (DataAccessException dae) {
            assertNotNull(dae);
        }
    }
    
   public void testFindAll() throws Exception {
       
       List list = dao.getAllAccounts();
       
       assertEquals(1,list.size());
       
       // Add account
       account = new Accounts();
       account.setLogin("Bill");
       account.setPassword("testPw");
       account.setEmail("toto@test.com");
       
       dao.createAccount(account);
       
       assertEquals(1,list.size());

       list = dao.getAllAccounts();

       assertEquals(2,list.size());
       
       dao.removeAccount(account);

       list = dao.getAllAccounts();

       assertEquals(1,list.size());
   }   
   
  public void testRemoveObject() throws Exception {
       
       // Add account
       account = new Accounts();
       account.setLogin("Bill");
       account.setPassword("testPw");
       account.setEmail("toto@test.com");
       
       dao.createAccount(account);
       
       dao.removeAccount("Bill");

       List list = dao.getAllAccounts();

       assertEquals(1,list.size());
   } 
  
  public void testAddAllAndRemove() throws Exception {
      
      // Add multiple account
      List<Accounts> listAccount = new ArrayList<Accounts>();
      
      Accounts acc = new Accounts ();
      acc.setLogin("Bill");
      acc.setPassword("testPw");
      acc.setEmail("toto@test.com");
      
      listAccount.add(acc);
      
      acc = new Accounts ();
      acc.setLogin("BigBill");
      acc.setPassword("anotherPw");
      acc.setEmail("toto2@test.com");      

      listAccount.add(acc);

      acc = new Accounts ();
      acc.setLogin("Matt");
      acc.setPassword("anotherPw2");
      acc.setEmail("toto3@test.com");      

      listAccount.add(acc);
      
      dao.createOrUpdateAll(listAccount);
      
      List list = dao.getAllAccounts();

      assertEquals(4,list.size());
      
      dao.removeAll(listAccount);

      list = dao.getAllAccounts();

      assertEquals(1,list.size());
      
  }     
    
}
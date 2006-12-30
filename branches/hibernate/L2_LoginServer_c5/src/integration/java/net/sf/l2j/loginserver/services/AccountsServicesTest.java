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

import java.sql.Connection;

import javax.sql.DataSource;

import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.services.exception.AccountModificationException;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Test class for AccountsServices 
 * 
 */
public class AccountsServicesTest extends DatabaseTestCase
{
    private ClassPathXmlApplicationContext context = null;
    
    private AccountsServices services = null;
    
    private void setAccountsServices (AccountsServices _services)
    {
        services = _services ;
    }

    protected IDataSet getDataSet() throws Exception {
        return new XmlDataSet(this.getClass().getResourceAsStream("accounts.xml"));

    }

    protected IDatabaseConnection getConnection() throws Exception 
    {
        Connection jdbcConnection = DataSourceUtils.getConnection((DataSource)context.getBean("dataSource"));
        return new DatabaseConnection(jdbcConnection);
    }    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        context = new ClassPathXmlApplicationContext("classpath*:/**/services/applicationContext-ServicesTest.xml");
        setAccountsServices ( (AccountsServices) context.getBean("AccountsServices"));
        super.setUp();
        
        // initialize your database connection here
        IDatabaseConnection connection = new DatabaseDataSourceConnection(
                (DataSource)context.getBean("dataSource"));
        // initialize your dataset here, from the file containing the test
        // dataset
        IDataSet dataSet = new FlatXmlDataSet(this.getClass().getResourceAsStream("accounts.xml"));

        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } finally {
            connection.close();
        }        
    }
    
    public void testAddAccount () throws Exception
    {
        assertEquals(1,services.getAccountsInfo().size());
        Accounts acc = services.addOrUpdateAccount("player2", "pass", "1");
        
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(1));        
        assertEquals(2,services.getAccountsInfo().size());
    }
    
    public void testAddAccountWithBadLevel () throws Exception
    {
        assertEquals(1,services.getAccountsInfo().size());
        try
        {
            services.addOrUpdateAccount("player2", "pass", "x");
            fail("No error");
        }
        catch (AccountModificationException e)
        {
            assertNotNull(e);
        }
    }
    
    public void testUpdateLevel () throws Exception
    {
        assertEquals(1,services.getAccountsInfo().size());
        Accounts acc = services.addOrUpdateAccount("player2", "pass", "1");
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(1));
        acc = services.addOrUpdateAccount("player2", "pass1", "2");
        
        services.changeAccountLevel("player2", "2");
        
        acc = services.getAccountById("player2");
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(2));
    }    
    
    public void testUpdateLevelIncorretValue () throws Exception
    {
        assertEquals(1,services.getAccountsInfo().size());
        Accounts acc = services.addOrUpdateAccount("player2", "pass", "1");
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(1));
        acc = services.addOrUpdateAccount("player2", "pass1", "2");
        
        try
        {
            services.changeAccountLevel("player2", "x");
        }
        catch (AccountModificationException e)
        {
            assertNotNull(e);
        }
    }       
    
    public void testGetUnknownAccount ()
    {
        Accounts acc = services.getAccountById("unknown");
        assertEquals(acc, null);
        
    }
    
    public void testDeleteAccount () throws Exception
    {
        assertEquals(1,services.getAccountsInfo().size());
        Accounts acc = services.addOrUpdateAccount("player2", "pass", "1");
        
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(1));        
        assertEquals(2,services.getAccountsInfo().size());
        
        services.deleteAccount("player2");
        assertEquals(1,services.getAccountsInfo().size());
    }    
    
    public void testDeleteUnknownAccount ()
    {
        assertEquals(1,services.getAccountsInfo().size());
        Accounts acc=null;
        try
        {
            acc = services.addOrUpdateAccount("player2", "pass", "1");
        }
        catch (AccountModificationException e1)
        {
            fail (e1.getMessage());
        }
        
        assertEquals(acc.getLogin(), "player2");
        assertEquals(acc.getAccessLevel(), new Integer(1));        
        assertEquals(2,services.getAccountsInfo().size());
        
        try
        {
            services.deleteAccount("unknown");
            fail("able to delete unknown object ?");
        }
        catch (AccountModificationException e)
        {
            assertNotNull(e);
        }
        assertEquals(2,services.getAccountsInfo().size());
    }    

}

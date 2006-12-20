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

import javax.sql.DataSource;

import net.sf.l2j.loginserver.beans.Gameservers;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;

/**
 * Test account DAO
 * 
 */
public class GameserversDAOTest extends DatabaseTestCase
{
    private Gameservers gameserver = null;
    private GameserversDAO dao = null;
    
    private ClassPathXmlApplicationContext context = null;

    public void setGameserversDao(GameserversDAO _dao) {
        this.dao = _dao;
    }
    
    protected IDataSet getDataSet() throws Exception {
        return new XmlDataSet(this.getClass().getResourceAsStream("gameservers.xml"));

    }

    protected IDatabaseConnection getConnection() throws Exception 
    {
        DatabaseDataSourceConnection ddsc = (DatabaseDataSourceConnection) context.getBean("DatabaseDataSourceConnection");

        return ddsc;
    }    
    protected void setUp() throws Exception {

        context = new ClassPathXmlApplicationContext(
                "classpath*:/**/dao/applicationContext-*.xml");
        setGameserversDao ( (GameserversDAO) context.getBean("GameserversDAO"));

        // initialize your database connection here
        IDatabaseConnection connection = new DatabaseDataSourceConnection(
                (DataSource)context.getBean("dataSource"));
        // initialize your dataset here, from the file containing the test
        // dataset
        IDataSet dataSet = new FlatXmlDataSet(this.getClass().getResourceAsStream("gameservers.xml"));

        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } finally {
            connection.close();
        }
       
    }    
    
    public void testFindGameserver() throws Exception {
        gameserver = dao.getGameserverByServerId(0);

        assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
        assertEquals("*", gameserver.getHost());
    }    
    
    public void testModifyGameserver() throws Exception {
        // retrieve object
        gameserver = dao.getGameserverByServerId(0);

        assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
        assertEquals("*", gameserver.getHost());
        
        // modify object
        gameserver.setHost("localhost");
        dao.update(gameserver);
        
        
        // check modification
        gameserver = dao.getGameserverByServerId(0);
        assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
        assertEquals("localhost", gameserver.getHost());
        
        // cancel modification
        gameserver.setHost("");
        dao.update(gameserver);
    }    


    public void testAddAndRemoveGameservers() throws Exception {
        
        // Add Gameserver
        gameserver = new Gameservers();
        gameserver.setHexid("hexid1");
        gameserver.setHost("*");
        
        int id = dao.createGameserver(gameserver);
        System.out.println("Gameserver created with id : " +id);
        assertEquals(gameserver.getHexid(), "hexid1");
        
        // delete Gameserver
        dao.removeGameserver(gameserver);

        try {
            gameserver = dao.getGameserverByServerId(id);
            fail("Gameservers found in database");
        } catch (DataAccessException dae) {
            assertNotNull(dae);
        }
    }
    
   public void testFindNonExistentGameserver() throws Exception {

        try {
            gameserver = dao.getGameserverByServerId(666);
            fail("Gameservers found in database");
        } catch (DataAccessException dae) {
            assertNotNull(dae);
        }
    }
    
   public void testFindAll() throws Exception {
       
       List list = dao.getAllGameservers();
       
       assertEquals(1,list.size());
       
       // Add Gameserver
       gameserver = new Gameservers();
       gameserver.setHexid("hexid2");
       gameserver.setHost("*");
       
       dao.createGameserver(gameserver);
       
       assertEquals(1,list.size());

       list = dao.getAllGameservers();

       assertEquals(2,list.size());
       
       dao.removeGameserver(gameserver);

       list = dao.getAllGameservers();

       assertEquals(1,list.size());
   }   
   
  public void testRemoveObject() throws Exception {
       
       // Add Gameserver
       gameserver = new Gameservers();
       gameserver.setHexid("hexid2");
       gameserver.setHost("hexid2");
       
       dao.createGameserver(gameserver);
       
       dao.removeGameserver(gameserver);

       List list = dao.getAllGameservers();

       assertEquals(1,list.size());
   } 
  
  public void testAddAllAndRemove() throws Exception {
      
      // Add multiple Gameserver
      List<Gameservers> listGameserver = new ArrayList<Gameservers>();
      
      Gameservers acc = new Gameservers ();
      acc.setHexid("hexid1");
      acc.setHost("toto@test.com");
      
      listGameserver.add(acc);
      
      acc = new Gameservers ();
      acc.setHexid("hexid2");
      acc.setHost("toto2@test.com");      

      listGameserver.add(acc);

      acc = new Gameservers ();
      acc.setHexid("hexid3");
      acc.setHost("toto3@test.com");      

      listGameserver.add(acc);
      
      dao.createOrUpdateAll(listGameserver);
      
      List list = dao.getAllGameservers();

      assertEquals(4,list.size());
      
      dao.removeAll(listGameserver);

      list = dao.getAllGameservers();

      assertEquals(1,list.size());
      
  }     
    
}
/**
 * 
 */
package net.sf.l2j.gameserver.boat.dao.impl;

import java.io.File;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConfigHelper;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;

/**
 *
 */
public class TestBoatDaoCsv extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ConfigHelper.configure();
    }
    
    public void testLoadDataWithValidFile ()
    {
        // by default, datapack root is set to where the caller play the test
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " "));
        Config.ALLOW_BOAT =true;
        BoatDAOCsv boatDAOCsv = new BoatDAOCsv();
        boatDAOCsv.load();
        
        assertEquals(2, boatDAOCsv.getNumberOfBoat());
    }
    
    public void testLoadDataWithFileNotFound ()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home")); 
        Config.ALLOW_BOAT =true;
        BoatDAOCsv boatDAOCsv = new BoatDAOCsv();
        boatDAOCsv.load();

        assertEquals(0, boatDAOCsv.getNumberOfBoat());
    }    
    
    /**
     * This test make an assertion on the objectid of a boat
     * don't forget that this test needs to be fork in another jvm because previous test
     * could change the object id. 
     * In eclipse, if you launch it on alone, it will succeed, but with other tests
     * it may fails.
     *
     */
    public void testGetBoat ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        Config.ALLOW_BOAT =true;
        BoatDAOCsv boatDAOCsv = new BoatDAOCsv();
        boatDAOCsv.load();

        assertEquals(2, boatDAOCsv.getNumberOfBoat());
        
        // we know that we use increment id factory, so the first boat 
        // will use the FIRST_OID + the 2 boat that we didn't succeed to parse
        L2BoatInstance l2BoatInstance = boatDAOCsv.getBoat(IdFactory.FIRST_OID+2);
        assertNotNull(l2BoatInstance);
        L2BoatInstance l2BoatInstanceNull = boatDAOCsv.getBoat(36);
        assertNull(l2BoatInstanceNull);
    }       

}

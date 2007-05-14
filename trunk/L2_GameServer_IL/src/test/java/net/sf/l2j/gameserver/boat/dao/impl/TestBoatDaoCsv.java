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
    
    public void testGetBoat ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        Config.ALLOW_BOAT =true;
        BoatDAOCsv boatDAOCsv = new BoatDAOCsv();
        boatDAOCsv.load();

        assertEquals(2, boatDAOCsv.getNumberOfBoat());
        
        L2BoatInstance l2BoatInstance = boatDAOCsv.getBoat(IdFactory.getInstance().getCurrentId()-1);
        assertNotNull(l2BoatInstance);
        L2BoatInstance l2BoatInstanceNull = boatDAOCsv.getBoat(36);
        assertNull(l2BoatInstanceNull);
    }       

}

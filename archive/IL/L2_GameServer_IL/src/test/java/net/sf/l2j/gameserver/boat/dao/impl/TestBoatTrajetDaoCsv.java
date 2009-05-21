/**
 * 
 */
package net.sf.l2j.gameserver.boat.dao.impl;

import java.io.File;

import junit.framework.TestCase;
import net.sf.l2j.Config;

/**
 *
 */
public class TestBoatTrajetDaoCsv extends TestCase
{
    
    public void testLoadDataWithValidFile ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        Config.ALLOW_BOAT =true;
        
        BoatTrajetDAOCsv boatTrajetDAOCsv = new BoatTrajetDAOCsv();
        boatTrajetDAOCsv.load();
        assertEquals(7, boatTrajetDAOCsv.getNumberOfBoatTrajet());
    }
    
    public void testLoadDataWithFileNotFound ()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home")); 
        
        BoatTrajetDAOCsv boatTrajetDAOCsv = new BoatTrajetDAOCsv();
        boatTrajetDAOCsv.load();
        assertEquals(0, boatTrajetDAOCsv.getNumberOfBoatTrajet());
    }    

    public void testGetNumberBoaPointsForTrajet ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        BoatTrajetDAOCsv boatTrajetDAOCsv = new BoatTrajetDAOCsv();
        boatTrajetDAOCsv.load();
        assertEquals(7, boatTrajetDAOCsv.getNumberOfBoatTrajet());
        assertEquals(17,boatTrajetDAOCsv.getNumberOfBoatPoints(3));
    }       

}

/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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

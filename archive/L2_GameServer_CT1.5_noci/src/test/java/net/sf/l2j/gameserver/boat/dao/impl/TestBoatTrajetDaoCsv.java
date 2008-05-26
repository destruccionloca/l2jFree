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

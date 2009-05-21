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
package net.sf.l2j.gameserver.jdklogextensions;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Advi
 *
 */
public class AuditLogFormatterTest extends TestCase
{
   private StatsSet statsSetForTestItem = null;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        statsSetForTestItem = new StatsSet();
        statsSetForTestItem.set("item_id",32);
        statsSetForTestItem.set("item_display_id",32);
        statsSetForTestItem.set("name","Dark Crystal Boots Light Armor");
        statsSetForTestItem.set("type1",1);  // needed for item list (inventory)
        statsSetForTestItem.set("type2",1);  // different lists for armor, weapon, etc
        statsSetForTestItem.set("weight",1);
        statsSetForTestItem.set("crystallizable",true);
        statsSetForTestItem.set("material",1);
        statsSetForTestItem.set("duration",1);
        statsSetForTestItem.set("bodypart",1);
        statsSetForTestItem.set("price",1);
        
    }
     
    /**
     * test for ticket 94
     *  
     */
    public void testAuditLogFormatter ()
    {
        try
        {
            // Backup system.out
            PrintStream psSys = System.err; //backup
            // instantiate a buffer to store output loggging 
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            System.setErr(new PrintStream (by,true));
            
            // Create a dummy item
            L2ItemInstance item = new L2ItemInstance(216565,new L2EtcItem(L2EtcItemType.MATERIAL,statsSetForTestItem));
            
            // prepare a list to be logged
            List<Object> param = new ArrayList<Object>();
            param.add("CHANGE : Pickup " );
            param.add("player corwin" );
            param.add(item);
            param.add(null);

            // Create input stream for log file -- or store file data into memory
            InputStream is =  new FileInputStream(getClass().getResource("logging.properties").getFile().replace("%20", " ")); 
            LogManager.getLogManager().readConfiguration(is);
            is.close();        
            
            Log _logItems = LogFactory.getLog("audit");
            _logItems.info(param);
            
            //restore system outputstream
            by.close();            
            System.setErr(psSys);
            
            // Check value
            assertTrue(by.toString(),by.toString().endsWith("[CHANGE : Pickup , player corwin, item 216565:Dark Crystal Boots Light Armor(1), null]"+ System.getProperty("line.separator")));
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail (e.getMessage()); 
        }        
    }
}

/**
 * Added copyright notice
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
package com.l2jfree.tools.log4jextension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * Class for L2Skill testing
 * 
 */
public class ExtendedPatternLayoutTest extends TestCase
{   
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    /**
     * Test Basic logging (just a string)
     */
    public void testBasicString()
    {
        try
        {
            // Backup system.out
            PrintStream psSys = System.out; //backup
            // instantiate a buffer to store output loggging 
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            System.setOut(new PrintStream (by,true));
            
            // log in consoleAppender
            Logger logger = Logger.getLogger(ExtendedPatternLayoutTest.class);
            logger.info("Basic string");
            
            //restore system outputstream
            by.close();            
            System.setOut(psSys);
            
            // Check value
            assertEquals(by.toString().trim(),"Basic string");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail (e.getMessage()); 
        }
    }    

    /**
     * Test throwable string
     */
    public void testThrowable()
    {
        try
        {
            // Backup system.out
            PrintStream psSys = System.out; //backup
            // instantiate a buffer to store output loggging 
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            System.setOut(new PrintStream (by,true));
            
            // log in consoleAppender
            Logger logger = Logger.getLogger(ExtendedPatternLayoutTest.class);
            Exception e = new Exception ("Exception for bad error...");
            e.setStackTrace(new StackTraceElement[]{new StackTraceElement("class toto","methode titi","file fifi",1)});
            logger.info("exception",e);
            
            //restore system outputstream
            by.close();            
            System.setOut(psSys);
            
            // Check value
            assertEquals("exception java.lang.Exception: Exception for bad error..."+ System.getProperty("line.separator")+
                         "\tat class toto.methode titi(file fifi:1)"
                         ,by.toString().trim());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail (e.getMessage()); 
        }
    }    

	/**
	 * Test content for a logging event With a a list containing a L2ItemInstance and a null value
	 */
	public void testLogItemEvent()
	{
        try
        {
            // Backup system.out
            PrintStream psSys = System.out; //backup
            // instantiate a buffer to store output loggging 
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            System.setOut(new PrintStream (by,true));
            
            // Create a dummy item
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("val1","player1");
            item.put("val2",216565);
            
            // prepare a list to be logged
            List<Object> param = new ArrayList<Object>();
            param.add("CHANGE : Pickup " );
            param.add("player corwin" );
            param.add(item);
            param.add(null);
            
            // log in consoleAppender
            Logger logger = Logger.getLogger(ExtendedPatternLayoutTest.class);
            logger.info(param);
            
            //restore system outputstream
            by.close();            
            System.setOut(psSys);
            
            // Check value
            assertEquals(by.toString().trim(),"[CHANGE : Pickup , player corwin, {val1=player1, val2=216565}, null]");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail (e.getMessage()); 
        }
	}
    


	

}

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
package net.sf.l2j;

import junit.framework.TestCase;
import net.sf.l2j.tools.L2Registry;

/**
 * Class for L2Registry testing
 * 
 */
public class L2RegistryTest extends TestCase
{   
    
	public void testLoadRegistry()
	{
        try
        {
            L2Registry.loadRegistry(new String[]{"net/sf/l2j/springbasic.xml"});
        }
        catch (Throwable e1)
        {
            fail (e1.getMessage());
        }
	}
    
    public void testLoadRegistryWithUnknownFile()
    {
        try
        {
            L2Registry.loadRegistry(new String[]{"toto.xml"});
            fail ("File toto.xml was found ?");
        }
        catch (Throwable e1)
        {
            assertNotNull(e1);
        }
    }
    
    public void testGetBean()
    {
        try
        {
            L2Registry.loadRegistry(new String[]{"net/sf/l2j/springbasic.xml"});
        }
        catch (Throwable e)
        {
            fail (e.getMessage());
        }
        
        Object o = L2Registry.getBean("basicSingletonString");
        assertNotNull(o);
        assertTrue (o instanceof String);        
    }

    public void testGetWrongBean()
    {
        try
        {
            L2Registry.loadRegistry(new String[]{"net/sf/l2j/springbasic.xml"});
        }
        catch (Throwable e)
        {
            fail (e.getMessage());
        }
        
        Object o = L2Registry.getBean("toto");
        assertNull(o);
    }
}

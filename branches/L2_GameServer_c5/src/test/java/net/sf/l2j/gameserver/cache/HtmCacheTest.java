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
package net.sf.l2j.gameserver.cache;

import java.io.File;

import net.sf.l2j.Config;

import junit.framework.TestCase;

/**
 * Class for HtmCache testing
 * 
 */
public class HtmCacheTest extends TestCase
{   
    
	/**
	 * Test method isLoadable
	 */
	public final void testLoadInvalidFile()
	{
        Config.LAZY_CACHE = true;
        HtmCache cache = HtmCache.getInstance();
        assertTrue (!cache.isLoadable("./config"));        
	}
    
    /**
     * Test method loadfile with a valid file
     */
    public final void testLoadValidFile()
    {
        Config.LAZY_CACHE = true;
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home"));
        HtmCache cache = HtmCache.getInstance();
        
        // load resource
        String file = getClass().getResource("npcdefault.htm").getFile().replace("%20", " "); 
        
        // check if it is loadable
        assertTrue (cache.isLoadable(file));
        
        assertEquals ("<html><body>I have nothing to say to you<br><a action=\"bypass -h npc_%objectId%_Quest\">Quest</a></body></html>",cache.loadFile(new File(file)));
        
        assertEquals (1, cache.getLoadedFiles() );
    }

    
    /**
     * Test where text is missing
     */
    public final void testMissingText()
    {
        Config.LAZY_CACHE = true;
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home"));
        HtmCache cache = HtmCache.getInstance();
        
        assertEquals ("<html><body>My text is missing:<br>dummy.htm</body></html>",cache.getHtmForce("dummy.htm"));
    }


}

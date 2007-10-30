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

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.Config.CacheType;

/**
 * Class for HtmCache testing
 * 
 */
public class HtmCacheTest extends TestCase
{   
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Config.CACHE_MAX_ELEM_IN_MEMORY=3;
        Config.CACHE_TIMETOIDLESECONDS=5;
        Config.CACHE_TIMETOLIVESECONDS=10;
    }
    
	/**
	 * Test method isLoadable
	 */
	public final void testLoadInvalidFile()
	{
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " "));
        Config.TYPE_CACHE = CacheType.ehcache;
        loadInvalidFile();
        Config.TYPE_CACHE = CacheType.mapcache;
        loadInvalidFile();
        Config.TYPE_CACHE = CacheType.none;
        loadInvalidFile();
	}
    
    private void loadInvalidFile ()
    {
        HtmCache cache = HtmCache.getInstance();
        cache.reload();
        assertTrue (!cache.isLoadable("./config"));        
    }
    
    /**
     * Test method loadfile with a valid file and lazy cache
     */
    public final void testLoadValidFile()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home"));
        Config.TYPE_CACHE = CacheType.ehcache;
        loadValidFile();
        Config.TYPE_CACHE = CacheType.mapcache;
        loadValidFile();
    }

    private void loadValidFile()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " "));
        HtmCache cache = HtmCache.getInstance();
        cache.reload();
        
        // check if it is loadable
        assertTrue (cache.isLoadable("npcdefault.htm"));
        
        assertEquals ("<html><body>I have nothing to say to you<br><a action=\"bypass -h npc_%objectId%_Quest\">Quest</a></body></html>",
                      cache.loadFile(new File(Config.DATAPACK_ROOT,"npcdefault.htm")));
        
        assertEquals (1, cache.getLoadedFiles() );
    }

    
    /**
     * Test where text is missing
     */
    public final void testMissingText()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home"));
        
        Config.TYPE_CACHE = CacheType.ehcache;
        HtmCache cache = HtmCache.getInstance();
        cache.reload();
        assertEquals ("<html><body>My text is missing:<br>dummy.htm</body></html>",cache.getHtmForce("dummy.htm"));
        Config.TYPE_CACHE = CacheType.mapcache;
        cache = HtmCache.getInstance();
        cache.reload();
        assertEquals ("<html><body>My text is missing:<br>dummy.htm</body></html>",cache.getHtmForce("dummy.htm"));
    }
    
    /**
     * Test method getHtm with a valid file and lazy cache (map and ehcache)
     */
    public final void testCache()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " "));
        Config.TYPE_CACHE = CacheType.ehcache;
        getHtmInCache();
        Config.TYPE_CACHE = CacheType.mapcache;
        getHtmInCache();
    }    

    private void getHtmInCache()
    {
        HtmCache cache = HtmCache.getInstance();
        cache.reload();
        
        assertEquals ("<html><body>I have nothing to say to you<br><a action=\"bypass -h npc_%objectId%_Quest\">Quest</a></body></html>",cache.getHtm("npcdefault.htm"));
        assertEquals (1, cache.getLoadedFiles() );

        assertEquals ("<html><body>I have nothing to say to you<br><a action=\"bypass -h npc_%objectId%_Quest\">Quest</a></body></html>",cache.getHtm("npcdefault.htm"));
        assertEquals (1, cache.getLoadedFiles() );
    }    
    
    /**
     * Test method reload cache with lazy cache (map and ehcache)
     */
    public final void testReloadCache()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " "));
        Config.TYPE_CACHE = CacheType.ehcache;
        reloadCache();
        Config.TYPE_CACHE = CacheType.mapcache;
        reloadCache();
    }  
    
    private void reloadCache()
    {
        HtmCache cache = HtmCache.getInstance();
        cache.reload();
        
        assertEquals ("<html><body>I have nothing to say to you<br><a action=\"bypass -h npc_%objectId%_Quest\">Quest</a></body></html>",cache.getHtm("npcdefault.htm"));
        assertEquals (1, cache.getLoadedFiles() );
        
        cache.reload();
        assertEquals (0, cache.getLoadedFiles() );
    }        

}

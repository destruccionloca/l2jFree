/*
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.l2j.Config;
import net.sf.l2j.Config.CacheType;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cache for html files 
 * 
 * Html files could be cached in a ehcache cache.
 * If we choose Config.TYPE_CACHE=none, all files are automatically read on startup and put in the cache.
 * This is faster on execution but need more memory
 * 
 * If we choose Config.TYPE_CACHE=ehcache, files are read whenever we need it and stores in ehcache cache.
 * It is possible to determine the time to live period in this cache, so a file that is not requested for more than
 * x minutes will be deleted from cache, saving memory. 
 * Slower on execution but need less memory
 * 
 * If we choose Config.TYPE_CACHE=mapcache, files are read whenever we need it and stores in a map.
 * Files are always added, so the cache will grow to max size and never won't decreased.
 * Slower on execution and need memory
 *
 * Note : For all cache, the key of the file in the cache is the hashcode of the file path
 * 
 * If you use ehcache, it is possible to configure :
 * maxElementsInMemory : default 9700
 * timeToIdleSeconds default 3600
 * timeToLiveSeconds default 7200
 * 
 */
public class HtmCache implements HtmCacheMBean
{
    
    /**
     * Logger
     */
    private final static Log _log = LogFactory.getLog(HtmCache.class.getName());
    
    /**
     * Instance for htmcache (retrieve by getInstance)
     */
    private static HtmCache _instance;
    
    /**
     * ehcache manager (only used on Config.TYPE_CACHE=ehcache)
     */
    private CacheManager cacheManager;
    
    /**
     * cache for ehcache (only used on Config.TYPE_CACHE=ehcache)
     */
    private Cache ehCache;
    
    /**
     * cache for mapcache (only used on Config.TYPE_CACHE=mapcache)
     */
    private Map<Integer, String> mapCache;
    
    /**
     * number of loaded files (only used on Config.TYPE_CACHE=mapcache)
     */
    private int _loadedFiles;
    
    /**
     * size of mapcache (only used on Config.TYPE_CACHE=mapcache)
     */
    private long _bytesBuffLen;
    
    /**
     * cache name of ehcache ((only used on Config.TYPE_CACHE=ehcache)
     */
    private static final String CACHENAME = "net.sf.l2j.datapack.html";
    
    /**
     * 
     * @return the instance
     */
    public static HtmCache getInstance()
    {
        if (_instance == null)
            _instance = new HtmCache();
        
        return _instance;
    }
    
    /**
     * private constructor to initialize cache
     *
     */
    private HtmCache()
    {
        reload();
    }
    
    /**
     * reload the cache 
     * @see reload(File f)
     */    
    public void reload()
    {
        reload(Config.DATAPACK_ROOT);
    }
    
    /**
     * reload all files in the folder f
     * 
     * if TYPE_CACHE = none, reload all files 
     * if TYPE_CACHE = mapcache, use lazy cache for map cache
     * if TYPE_CACHE = ehcache, drop and create ehcache cache
     * 
     * @param f
     */
    public void reload(File f)
    {
        if (Config.TYPE_CACHE == CacheType.none )
        {
            mapCache = new FastMap<Integer, String>();
            mapCache.clear();
        	_log.info("Html cache start...");
            parseDir(f);
            _log.info("Cache[HTML]: " + String.format("%.3f",getMemoryUsage())  + " megabytes on " + getLoadedFiles() + " files loaded");
        }
        else if (Config.TYPE_CACHE == CacheType.mapcache )
        {
            mapCache = new FastMap<Integer, String>();
        	mapCache.clear();
        	_loadedFiles = 0;
        	_bytesBuffLen = 0;
            _log.info("Cache[HTML]: Running lazy cache");
        }
        else if (Config.TYPE_CACHE == CacheType.ehcache )
        {
            cacheManager = CacheManager.getInstance();
            cacheManager.removeCache(CACHENAME);
            Cache htmCache = new Cache(CACHENAME, Config.CACHE_MAX_ELEM_IN_MEMORY, true, false, Config.CACHE_TIMETOLIVESECONDS,  Config.CACHE_TIMETOIDLESECONDS);
            cacheManager.addCache(htmCache);
            ehCache = cacheManager.getCache(CACHENAME);
            _log.info("HtmCache: Running ehcache - Max elements in memory : "+ Config.CACHE_MAX_ELEM_IN_MEMORY); 
            _log.info("HtmCache: timeToIdleSeconds="+Config.CACHE_TIMETOIDLESECONDS); 
            _log.info("HtmCache: timeToLiveSeconds="+Config.CACHE_TIMETOLIVESECONDS);
        }
    }
    
    /**
     * Partially reload one folder of the datapack html
     * @param f
     */
    public void reloadPath(File f)
    {
    	parseDir(f);
    	_log.info("Cache[HTML]: Reloaded specified path.");
    }
    
    /**
     * return the size of the cache
     * 
     * if TYPE_CACHE = none, return  _bytesBuffLen/1048576
     * if TYPE_CACHE = mapcache, return  _bytesBuffLen/1048576
     * if TYPE_CACHE = ehcache, return calculateInMemorySize/1048576
     * 
     * @return the size of the cache
     */
    public double getMemoryUsage()
    {
        if ( Config.TYPE_CACHE == CacheType.ehcache )
        {
            return ehCache.calculateInMemorySize()/1048576;
        }
        return ((float)_bytesBuffLen/1048576);
    }
    
    /**
     * @return the number of elements in cache
     */
    public int getLoadedFiles()
    {
        if ( Config.TYPE_CACHE == CacheType.ehcache )
        {
            return ehCache.getSize();
        }
        return _loadedFiles;
    }
    
    /**
     * Private class used to filter html file when we load datapack html files
     * 
     */
    private class HtmFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            if (!file.isDirectory())
            {
                return (file.getName().endsWith(".htm") || file.getName().endsWith(".html"));
            }
            return true;
        }
    }
    
    /**
     * Load all files in a folder and all subfolders
     * @param dir the root folder
     */
    private void parseDir(File dir)
    {
        if (dir == null )
            return;
        FileFilter filter = new HtmFilter();
        File[] files = dir.listFiles(filter);
        
        for (File file : files)
        {
            if (!file.isDirectory())
                loadFile(file);
            else
                parseDir(file);
        }
    }
    
    /**
     * Load a file in the cache 
     * @param file the file to load
     * @return the content of the file
     */
    public String loadFile(File file)
    {
        HtmFilter filter = new HtmFilter();
        
        // check if file exist, is accepted by 
        // the filter and is not a directory
        // -----------------------------------
        if (file.exists() && filter.accept(file) && !file.isDirectory())
        {
            String content;
            FileInputStream fis = null;
            
            try
            {
                fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int bytes = bis.available();
                byte[] raw = new byte[bytes];
                
                bis.read(raw);
                content = new String(raw, "UTF-8");
                content = content.replaceAll("\r\n","\n");
                
                String relpath = Util.getRelativePath(Config.DATAPACK_ROOT,file);
                int hashcode = relpath.hashCode();
                
                // For ehcache, we just need to store the element in cache
                // -------------------------------------------------------
                if ( Config.TYPE_CACHE == CacheType.ehcache )
                {
                    ehCache.put(new Element(hashcode,content));
                }
                // For mapcache, we store the element in the map and recalculate statistics
                // ------------------------------------------------------------------------
                else
                {
                    // get the old content from the cache
                    // if the file was previously cached, we substract his old size 
                    // to the _bytesBuffLen to keep statistics up to date
                    String oldContent = mapCache.get(hashcode);
                    if (oldContent == null)
                    {
                        _bytesBuffLen += bytes;
                        _loadedFiles++;
                    }
                    else
                    {
                        _bytesBuffLen = _bytesBuffLen - oldContent.length() + bytes;
                    }
                    
                    mapCache.put(hashcode,content);
                }
                
                return content;
            }
            catch (Exception e)
            {
                _log.warn("problem with htm file " + e);
            }
            finally
            {
                try { fis.close(); } catch (Exception e1) { }
            }   
        }
        
        return null;
    }
    
    /**
     * Force the lecture of a file
     * If the file is not found in the cache, we send a default html content
     * @param path the file to load
     * @return the html
     */
    public String getHtmForce(String path)
    {
        String content = getHtm(path);
        
        if (content == null)
        {
            content = "<html><body>My text is missing:<br>" + path + "</body></html>";
            _log.warn("Cache[HTML]: Missing HTML page: " + path);
        }
        
        return content;
    }
    
    /**
     * Get the file from the cache. If the file was not in cache, load it from disk
     * @param path the file to search
     * @return the content or null of the file wasn't found
     */
    public String getHtm(String path)
    {
        String content=null;
        
        // for ehcache, we search in the cache, if we don't find it, we load the file
        if ( Config.TYPE_CACHE == CacheType.ehcache)
        {
            Element element = ehCache.get(path.hashCode());
            if (element == null )
            {
                content = loadFile(new File(Config.DATAPACK_ROOT,path));
            }
            else
            {
                content = (String)element.getObjectValue();
            }
        }
        // when we don't use a cache, we search in the cache, if we don't find it, we return null
        else if ( Config.TYPE_CACHE == CacheType.none )
        {
            content = mapCache.get(path.hashCode());
        
        }
        // for mapcache, we search in the cache, if we don't find it, we load the file
        else if ( Config.TYPE_CACHE == CacheType.mapcache )
        {
            content = mapCache.get(path.hashCode());
            if (content == null )
            {
                content = loadFile(new File(Config.DATAPACK_ROOT,path));
            }            
        }
        return content;
    }
    
    /**
     * Check if a file is in the cache
     * @param path the file to search
     * @return true or false if the file is in the cache
     */
    public boolean contains(String path)
    {
        if ( Config.TYPE_CACHE == CacheType.ehcache)
        {
            return ehCache.isElementInMemory(path.hashCode());
        }
        return mapCache.containsKey(path.hashCode());
    }
   
    /** 
     * Check if an HTM exists and can be loaded 
     * @param
     * path The path to the HTM
     * */
    public boolean isLoadable(String path)
    {
    	File file = new File(Config.DATAPACK_ROOT,path);
        HtmFilter filter = new HtmFilter();
        
        if (file.exists() && filter.accept(file) && !file.isDirectory())
	        return true;
        
    	return false;
    }
}

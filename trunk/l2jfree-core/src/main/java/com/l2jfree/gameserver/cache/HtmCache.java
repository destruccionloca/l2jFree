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
package com.l2jfree.gameserver.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

import javolution.util.FastMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.util.Util;

/**
 * @author Layane
 */

public final class HtmCache
{
	private static final Log _log = LogFactory.getLog(HtmCache.class);
	
	private static final FileFilter HTM_FILTER = new FileFilter() {
		public boolean accept(File file)
		{
			return file.isDirectory() || file.getName().endsWith(".htm") || file.getName().endsWith(".html");
		}
	};
	
	private static HtmCache _instance;
	
	public static HtmCache getInstance()
	{
		if (_instance == null)
			_instance = new HtmCache();
		
		return _instance;
	}
	
	private final FastMap<String, String> _cache = new FastMap<String, String>();
	private int _loadedFiles;
	private int _size;
	
	private HtmCache()
	{
		reload();
	}
	
	public synchronized void reload()
	{
		_cache.clear();
		_loadedFiles = 0;
		_size = 0;
		
		if (!Config.LAZY_CACHE)
		{
			_log.info("Cache[HTML]: Caching started.");
			
			parseDir(Config.DATAPACK_ROOT);
		}
		else
			_log.info("Cache[HTML]: Running lazy cache.");
		
		_log.info(this);
	}
	
	public void reloadPath(File f)
	{
		parseDir(f);
		
		_log.info("Cache[HTML]: Reloaded specified path.");
	}
	
	public void parseDir(File dir)
	{
		for (File file : dir.listFiles(HTM_FILTER))
		{
			if (!file.isDirectory())
				loadFile(file);
			else
				parseDir(file);
		}
	}
	
	public String loadFile(File file)
	{
		if (isLoadable(file))
		{
			BufferedInputStream bis = null;
			try
			{
				bis = new BufferedInputStream(new FileInputStream(file));
				byte[] raw = new byte[bis.available()];
				bis.read(raw);
				
				String content = new String(raw, "UTF-8").replaceAll("\r\n", "\n");
				String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
				
				_size += content.length();
				
				String oldContent = _cache.get(relpath);
				if (oldContent == null)
					_loadedFiles++;
				else
					_size -= oldContent.length();
				
				_cache.put(relpath, content);
				
				return content;
			}
			catch (Exception e)
			{
				_log.warn("Problem with htm file:", e);
			}
			finally
			{
				IOUtils.closeQuietly(bis);
			}
		}
		
		return null;
	}
	
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
	
	public String getHtm(String path)
	{
		String content = _cache.get(path);
		
		if (content == null && Config.LAZY_CACHE)
			content = loadFile(new File(Config.DATAPACK_ROOT, path));
		
		return content;
	}
	
	public boolean isLoadable(File file)
	{
		return file.exists() && !file.isDirectory() && HTM_FILTER.accept(file);
	}
	
	public boolean pathExists(String path)
	{
		if (_cache.containsKey(path))
			return true;
		
		if (Config.LAZY_CACHE && isLoadable(new File(Config.DATAPACK_ROOT, path)))
			return true;
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder(64).append("Cache[HTML]: ").append(String.format("%.3f", (float)_size / 1048576))
			.append(" megabytes on ").append(_loadedFiles).append(" file(s) loaded.").toString();
	}
}

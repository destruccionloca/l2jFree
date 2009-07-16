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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcHtmlMessage;
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
	
	public static HtmCache getInstance()
	{
		return SingletonHolder._instance;
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
		
		_log.info("Cache[HTML]: Caching started...");
		
		parseDir(Config.DATAPACK_ROOT);
		
		_log.info(this);
		_log.info("Cache[HTML]: Compacting htmls...");
		
		final StringBuilder sb = new StringBuilder(8192);
		
		for (Entry<String, String> entry : _cache.entrySet())
		{
			try
			{
				final String oldHtml = entry.getValue();
				final String newHtml = compactHtml(sb, oldHtml);
				
				_size -= oldHtml.length();
				_size += newHtml.length();
				
				entry.setValue(newHtml);
			}
			catch (RuntimeException e)
			{
				_log.warn("Cache[HTML]: Error during compaction of " + entry.getKey(), e);
			}
		}
		
		_log.info("Cache[HTML]: Validating htmls...");
		
		validate();
		
		_log.info(this);
	}
	
	private void validate()
	{
		final Set<String> set = new HashSet<String>();
		
		for (Entry<String, String> entry : _cache.entrySet())
		{
			final String html = entry.getValue();
			
			outer: for (int begin = 0; (begin = html.indexOf("<", begin)) != -1; begin++)
			{
				int end;
				
				for (end = begin; end < html.length(); end++)
				{
					if (html.charAt(end) == '>' || html.charAt(end) == ' ')
						break;
					
					// some special quest-replaced tag
					if (end == begin + 1 && html.charAt(end) == '?')
						continue outer;
				}
				
				end++;
				
				set.add(html.substring(begin + 1, end - 1).toLowerCase().replaceAll("/", ""));
			}
		}
		
		//_log.info("Tags used: " + set.size());
		//for (String tag : set)
		//	_log.info("'" + tag + "'");
		
		set.remove("!--"); // comment
		for (String tag : AbstractNpcHtmlMessage.VALID_TAGS)
			set.remove(tag);
		
		if (!set.isEmpty())
		{
			_log.info("Invalid tags used: " + set.size());
			for (String tag : set)
				_log.info("'" + tag + "'");
		}
	}
	
	private static final String[] TAGS_TO_COMPACT;
	
	static
	{
		// TODO: is there any other tag that should be replaced?
		final String[] tagsToCompact = { "html", "title", "body", "br", "br1", "p", "table", "tr", "td" };
		
		final List<String> list = new ArrayList<String>();
		
		for (String tag : tagsToCompact)
		{
			list.add("<" + tag + ">");
			list.add("</" + tag + ">");
			list.add("<" + tag + "/>");
			list.add("<" + tag + " />");
		}
		
		final List<String> list2 = new ArrayList<String>();
		
		for (String tag : list)
		{
			list2.add(tag);
			list2.add(tag + " ");
			list2.add(" " + tag);
		}
		
		TAGS_TO_COMPACT = list2.toArray(new String[list.size()]);
	}
	
	private String compactHtml(StringBuilder sb, String html)
	{
		sb.setLength(0);
		sb.append(html);
		
		for (int i = 0; i < sb.length(); i++)
			if (Character.isWhitespace(sb.charAt(i)))
				sb.setCharAt(i, ' ');
		
		replaceAll(sb, "  ", " ");
		
		replaceAll(sb, "< ", "<");
		replaceAll(sb, " >", ">");
		
		for (int i = 0; i < TAGS_TO_COMPACT.length; i += 3)
		{
			replaceAll(sb, TAGS_TO_COMPACT[i + 1], TAGS_TO_COMPACT[i]);
			replaceAll(sb, TAGS_TO_COMPACT[i + 2], TAGS_TO_COMPACT[i]);
		}
		
		replaceAll(sb, "  ", " ");
		
		// String.trim() without additional garbage
		int fromIndex = 0;
		int toIndex = sb.length();
		
		while (fromIndex < toIndex && sb.charAt(fromIndex) == ' ')
			fromIndex++;
		
		while (fromIndex < toIndex && sb.charAt(toIndex - 1) == ' ')
			toIndex--;
		
		return sb.substring(fromIndex, toIndex);
	}
	
	private void replaceAll(StringBuilder sb, String pattern, String value)
	{
		for (int index = 0; (index = sb.indexOf(pattern, index)) != -1;)
			sb.replace(index, index + pattern.length(), value);
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
				
				String content = new String(raw, "UTF-8");
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
		return _cache.get(path);
	}
	
	private boolean isLoadable(File file)
	{
		return file.exists() && !file.isDirectory() && HTM_FILTER.accept(file);
	}
	
	public boolean pathExists(String path)
	{
		return _cache.containsKey(path);
	}
	
	@Override
	public String toString()
	{
		return "Cache[HTML]: " + String.format("%.3f", (float)_size / 1024) + " kilobytes on " + _loadedFiles
			+ " file(s) loaded.";
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HtmCache _instance = new HtmCache();
	}
}

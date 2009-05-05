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
package com.l2jfree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.config.L2Properties;
import com.l2jfree.util.HandlerRegistry;

/**
 * @author evill33t
 */
public abstract class L2Config
{
	static
	{
		System.setProperty("line.separator", "\r\n");
		System.setProperty("file.encoding", "UTF-8");
	}
	
	protected static final Log _log = LogFactory.getLog(L2Config.class);
	
	public static final String TELNET_FILE = "./config/telnet.properties";
	
	protected L2Config()
	{
		throw new InternalError();
	}
	
	private static final HandlerRegistry<String, ConfigLoader> _loaders = new HandlerRegistry<String, ConfigLoader>(true) {
		@Override
		public String standardizeKey(String key)
		{
			return key.trim().toLowerCase();
		}
	};
	
	protected static void registerConfig(ConfigLoader loader)
	{
		_loaders.register(loader.getName(), loader);
	}
	
	public static void loadConfigs() throws Exception
	{
		for (ConfigLoader loader : _loaders.getHandlers().values())
			loader.load();
	}
	
	public static String loadConfig(String name) throws Exception
	{
		final ConfigLoader loader = _loaders.get(name);
		
		if (loader == null)
			throw new Exception();
		
		try
		{
			loader.load();
			return "'" + loader.getFileName() + "' reloaded!";
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
	}
	
	public static String getLoaderNames()
	{
		return StringUtils.join(_loaders.getHandlers().keySet().iterator(), "|");
	}
	
	protected static abstract class ConfigLoader
	{
		protected abstract String getName();
		
		protected String getFileName()
		{
			return "./config/" + getName().trim() + ".properties";
		}
		
		protected void load() throws Exception
		{
			_log.info("loading '" + getFileName() + "'");
			
			try
			{
				loadReader(new BufferedReader(new FileReader(getFileName())));
			}
			catch (Exception e)
			{
				_log.fatal("Failed to load '" + getFileName() + "'!", e);
				
				throw new Exception("Failed to load '" + getFileName() + "'!");
			}
		}
		
		protected void loadReader(BufferedReader reader) throws Exception
		{
			loadImpl(new L2Properties(reader));
		}
		
		protected void loadImpl(Properties properties) throws Exception
		{
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof ConfigLoader && getClass().equals(obj.getClass());
		}
	}
}

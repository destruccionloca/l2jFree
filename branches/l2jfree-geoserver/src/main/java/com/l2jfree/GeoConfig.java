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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.config.L2Properties;
import com.l2jfree.geoserver.util.Util;

public final class GeoConfig
{
	private final static Log	_log				= LogFactory.getLog(GeoConfig.class.getName());
	public static final String	CONFIGURATION_FILE	= "./config/geoserver.properties";

	public static int			GEODATA_MODE;
	public static String		SERVER_BIND_HOST;
	public static String		CLIENT_TARGET_HOST;
	public static int			PORT;
	public static boolean		PATH_CLEAN;
	public static boolean		ALLOW_DOORS;
	public static final int		MAP_MIN_X			= -163840;
	public static final int		MAP_MAX_X			= 229375;
	public static final int		MAP_MIN_Y			= -262144;
	public static final int		MAP_MAX_Y			= 294911;
	public static final int		MAP_MIN_Z			= -32768;
	public static final int		MAP_MAX_Z			= 32767;

	public static void loadConfiguration()
	{
		_log.info("loading " + CONFIGURATION_FILE);
		try
		{
			Properties serverSettings = new L2Properties(CONFIGURATION_FILE);
			SERVER_BIND_HOST = serverSettings.getProperty("GeoServerHost");
			CLIENT_TARGET_HOST = serverSettings.getProperty("GeoClientTargetHost");
			PORT = Integer.parseInt(serverSettings.getProperty("GeoPort"));
			GEODATA_MODE = Integer.parseInt(serverSettings.getProperty("GeoData", "0"));
			PATH_CLEAN = Boolean.parseBoolean(serverSettings.getProperty("PathClean", "True"));
			ALLOW_DOORS = Boolean.parseBoolean(serverSettings.getProperty("AllowDoors", "false"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
		}
	}

	public static final String	LOG_FILE		= "./config/logging.properties";
	final static String			LOG_FOLDER		= "log";							// Name of folder for log file
	final static String			LOG_FOLDER_GAME	= "game";

	public static void loadLogConfig()
	{
		try
		{
			InputStream is = new FileInputStream(new File(LOG_FILE));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch (Exception e)
		{
			throw new Error("Failed to Load logging.properties File.");
		}
		_log.info("logging initialized");
		File logFolder = new File(LOG_FOLDER);
		logFolder.mkdir();
	}

	public static void load()
	{
		loadLogConfig(); // must be loaded b4 first log output
		Util.printSection("Configuration");
		loadConfiguration();
	}
}

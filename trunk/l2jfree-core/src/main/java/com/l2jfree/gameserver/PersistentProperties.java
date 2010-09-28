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
package com.l2jfree.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.config.L2Properties;

/**
 * @author NB4L1
 */
public final class PersistentProperties
{
	private static final Log _log = LogFactory.getLog(PersistentProperties.class);
	
	private static final Map<String, Map<String, String>> _propertiesByClassNames = new HashMap<String, Map<String, String>>();
	
	static
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			final PreparedStatement ps = con.prepareStatement("SELECT * FROM persistent_properties");
			final ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				final String className = rs.getString("class_name");
				final String propertyName = rs.getString("property_name");
				final String propertyValue = rs.getString("property_value");
				
				final Map<String, String> map = getInnerMap(className, true);
				
				map.put(propertyName, propertyValue);
			}
			
			rs.close();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		int classCount = 0;
		int propertyCount = 0;
		for (Map<String, String> properties : _propertiesByClassNames.values())
		{
			classCount++;
			propertyCount += properties.size();
		}
		
		_log.info("PersistentProperties: " + propertyCount + " properties loaded for " + classCount + " classes.");
	}
	
	@Deprecated
	private synchronized static Map<String, String> getInnerMap(String className, boolean force)
	{
		Map<String, String> map = _propertiesByClassNames.get(className);
		
		if (map == null && force)
			_propertiesByClassNames.put(className, map = new HashMap<String, String>());
		
		return map;
	}
	
	public synchronized static void setProperty(Class<?> clazz, String propertyName, Object propertyValue)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), true);
		
		map.put(propertyName, String.valueOf(propertyValue));
	}
	
	public synchronized static void setProperties(Class<?> clazz, L2Properties properties)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), true);
		
		map.clear();
		
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
	}
	
	public synchronized static void clearProperty(Class<?> clazz, String propertyName)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), false);
		
		if (map == null)
			return;
		
		map.remove(propertyName);
	}
	
	public synchronized static void clearProperties(Class<?> clazz)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), false);
		
		if (map == null)
			return;
		
		map.clear();
	}
	
	public synchronized static String getProperty(Class<?> clazz, String propertyName)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), false);
		
		if (map == null)
			return null;
		
		return map.get(propertyName);
	}
	
	public synchronized static String getProperty(Class<?> clazz, String propertyName, String defaultValue)
	{
		final String propertyValue = getProperty(clazz, propertyName);
		
		return propertyValue == null ? defaultValue : propertyValue;
	}
	
	public synchronized static L2Properties getProperties(Class<?> clazz)
	{
		final Map<String, String> map = getInnerMap(clazz.getName(), true);
		
		final L2Properties properties = new L2Properties();
		
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			final String propertyName = entry.getKey();
			final String propertyValue = entry.getValue();
			
			properties.setProperty(propertyName, propertyValue);
		}
		
		return properties;
	}
	
	public interface StoreListener
	{
		public void update();
	}
	
	private static final Set<StoreListener> _storeListeners = new HashSet<StoreListener>();
	
	public synchronized static void addStoreListener(StoreListener listener)
	{
		_storeListeners.add(listener);
	}
	
	public synchronized static void store()
	{
		for (StoreListener listener : _storeListeners)
			listener.update();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			{
				final PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE persistent_properties");
				ps.execute();
				ps.close();
			}
			
			{
				final PreparedStatement ps = con
						.prepareStatement("INSERT INTO persistent_properties (class_name, property_name, property_value) VALUES (?,?,?)");
				
				for (Map.Entry<String, Map<String, String>> entry1 : _propertiesByClassNames.entrySet())
				{
					final String className = entry1.getKey();
					final Map<String, String> properties = entry1.getValue();
					
					for (Map.Entry<String, String> entry2 : properties.entrySet())
					{
						final String propertyName = entry2.getKey();
						final String propertyValue = entry2.getValue();
						
						ps.setString(1, className);
						ps.setString(2, propertyName);
						ps.setString(3, propertyValue);
						ps.execute();
					}
				}
				
				ps.close();
			}
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}

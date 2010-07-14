package com.l2jfree.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;

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
				
				final Map<String, String> properties = getProperties(className, true);
				
				properties.put(propertyName, propertyValue);
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
	
	public static Map<String, String> getProperties(String className, boolean force)
	{
		Map<String, String> properties = _propertiesByClassNames.get(className);
		
		if (properties == null && force)
			_propertiesByClassNames.put(className, properties = new HashMap<String, String>());
		
		return properties;
	}
	
	public synchronized static void setProperty(Class<?> clazz, String propertyName, Object propertyValue)
	{
		final Map<String, String> properties = getProperties(clazz.getName(), true);
		
		properties.put(propertyName, String.valueOf(propertyValue));
	}
	
	public synchronized static String getProperty(Class<?> clazz, String propertyName)
	{
		final Map<String, String> properties = getProperties(clazz.getName(), false);
		
		if (properties == null)
			return null;
		
		return properties.get(propertyName);
	}
	
	public synchronized static String getProperty(Class<?> clazz, String propertyName, String defaultValue)
	{
		final String propertyValue = getProperty(clazz, propertyName);
		
		return propertyValue == null ? defaultValue : propertyValue;
	}
	
	public synchronized static void store()
	{
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

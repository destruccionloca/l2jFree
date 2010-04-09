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
package com.l2jfree.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Noctarius
 */
public final class L2Properties extends Properties
{
	private static final long serialVersionUID = -4599023842346938325L;
	
	private static final Log _log = LogFactory.getLog(L2Properties.class);
	
	private boolean _warn = true;
	
	public L2Properties()
	{
	}
	
	public L2Properties setLog(boolean warn)
	{
		_warn = warn;
		
		return this;
	}
	
	// ===================================================================================
	
	public L2Properties(String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public L2Properties(File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	public L2Properties(InputStream inStream) throws IOException
	{
		load(inStream);
	}
	
	public L2Properties(Reader reader) throws IOException
	{
		load(reader);
	}
	
	// ===================================================================================
	
	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	@Override
	public void load(InputStream inStream) throws IOException
	{
		try
		{
			super.load(inStream);
		}
		finally
		{
			inStream.close();
		}
	}
	
	@Override
	public void load(Reader reader) throws IOException
	{
		try
		{
			super.load(reader);
		}
		finally
		{
			reader.close();
		}
	}
	
	// ===================================================================================
	
	@Override
	public String getProperty(String key)
	{
		String property = super.getProperty(key);
		
		if (property == null)
		{
			if (_warn)
				_log.warn("L2Properties: Missing property for key - " + key);
			
			return null;
		}
		
		return property.trim();
	}
	
	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = super.getProperty(key, defaultValue);
		
		if (property == null)
		{
			if (_warn)
				_log.warn("L2Properties: Missing defaultValue for key - " + key);
			
			return null;
		}
		
		return property.trim();
	}
	
	// ===================================================================================
	
	public boolean getBool(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Boolean value required, but not specified");
		if (val instanceof Boolean)
			return (Boolean)val;
		try
		{
			return Boolean.parseBoolean((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}
	
	public boolean getBool(String name, boolean deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Boolean)
			return (Boolean)val;
		try
		{
			return Boolean.parseBoolean((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}
	
	public byte getByte(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Byte value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).byteValue();
		try
		{
			return Byte.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	public byte getByte(String name, byte deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).byteValue();
		try
		{
			return Byte.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	public short getShort(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Short value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).shortValue();
		try
		{
			return Short.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	public short getShort(String name, short deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).shortValue();
		try
		{
			return Short.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	public int getInteger(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).intValue();
		try
		{
			return Integer.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public int getInteger(String name, int deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).intValue();
		try
		{
			return Integer.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public long getLong(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).longValue();
		try
		{
			return Long.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public long getLong(String name, int deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).longValue();
		try
		{
			return Long.decode((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public float getFloat(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).floatValue();
		try
		{
			return (float)Double.parseDouble((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public float getFloat(String name, float deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).floatValue();
		try
		{
			return (float)Double.parseDouble((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public double getDouble(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).doubleValue();
		try
		{
			return Double.parseDouble((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public double getDouble(String name, float deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).doubleValue();
		try
		{
			return Double.parseDouble((String)val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public String getString(String name)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("String value required, but not specified");
		return String.valueOf(val);
	}
	
	public String getString(String name, String deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		return String.valueOf(val);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = get(name);
		if (val == null)
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName()
					+ " required, but not specified");
		if (enumClass.isInstance(val))
			return (T)val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: "
					+ val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = get(name);
		if (val == null)
			return deflt;
		if (enumClass.isInstance(val))
			return (T)val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: "
					+ val);
		}
	}
}

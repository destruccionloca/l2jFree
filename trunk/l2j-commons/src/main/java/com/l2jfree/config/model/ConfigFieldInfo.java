package com.l2jfree.config.model;

import java.io.PrintStream;
import java.lang.reflect.Field;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.config.L2Properties;
import com.l2jfree.config.annotation.ConfigField;
import com.l2jfree.config.annotation.ConfigGroupBeginning;
import com.l2jfree.config.annotation.ConfigGroupEnding;
import com.l2jfree.config.converters.Converter;

public final class ConfigFieldInfo
{
	private static final Log _log = LogFactory.getLog(ConfigFieldInfo.class);
	
	private final Field _field;
	private final ConfigField _configField;
	private final Converter _converter;
	private final ConfigGroupBeginning _configGroupBeginning;
	private final ConfigGroupEnding _configGroupEnding;
	
	private ConfigGroup _beginningGroup;
	private ConfigGroup _endingGroup;
	
	private volatile boolean _fieldValueLoaded = false;
	
	public ConfigFieldInfo(Field field) throws InstantiationException, IllegalAccessException
	{
		_field = field;
		_configField = field.getAnnotation(ConfigField.class);
		_converter = getConfigField().converter().newInstance();
		_configGroupBeginning = field.getAnnotation(ConfigGroupBeginning.class);
		_configGroupEnding = field.getAnnotation(ConfigGroupEnding.class);
	}
	
	public Field getField()
	{
		return _field;
	}
	
	public String getCurrentValue()
	{
		Object obj = null;
		
		try
		{
			obj = getField().get(null);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		
		return getConverter().convertToString(getField().getType(), obj);
	}
	
	public void setCurrentValue(String value)
	{
		Object obj = getConverter().convertFromString(getField().getType(), value);
		
		if (_fieldValueLoaded && getConfigField().eternal())
			_log.warn("Eternal config field (" + getField() + ") (" + getConfigField() + ") assigned multiple times!");
		
		try
		{
			getField().set(null, obj);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		
		_fieldValueLoaded = true;
	}
	
	public void setCurrentValue(L2Properties properties)
	{
		final String newValue = properties.getProperty(getConfigField().name(), getConfigField().value());
		
		setCurrentValue(newValue);
	}
	
	public ConfigField getConfigField()
	{
		return _configField;
	}
	
	public boolean isModified()
	{
		final String value = getCurrentValue();
		
		// config value wasn't initialized
		if (value ==  null)
			return false;
		
		return !getConfigField().value().equals(value);
	}
	
	public Converter getConverter()
	{
		return _converter;
	}
	
	public ConfigGroupBeginning getConfigGroupBeginning()
	{
		return _configGroupBeginning;
	}
	
	public ConfigGroupEnding getConfigGroupEnding()
	{
		return _configGroupEnding;
	}
	
	public ConfigGroup getBeginningGroup()
	{
		return _beginningGroup;
	}
	
	public void setBeginningGroup(ConfigGroup beginningGroup)
	{
		_beginningGroup = beginningGroup;
	}
	
	public ConfigGroup getEndingGroup()
	{
		return _endingGroup;
	}
	
	public void setEndingGroup(ConfigGroup endingGroup)
	{
		_endingGroup = endingGroup;
	}
	
	public void print(PrintStream out)
	{
		if (getBeginningGroup() != null && getBeginningGroup().isModified())
		{
			out.println("########################################");
			out.println("## " + getConfigGroupBeginning().name());
			
			if (!ArrayUtils.isEmpty(getConfigGroupBeginning().comment()))
				for (String line : getConfigGroupBeginning().comment())
					out.println("# " + line);
			
			out.println();
		}
		
		if (isModified())
		{
			if (!ArrayUtils.isEmpty(getConfigField().comment()))
				for (String line : getConfigField().comment())
					out.println("# " + line);
			
			out.println("# Default: " + getConfigField().value());
			out.println(getConfigField().name() + " = " + getCurrentValue());
			out.println();
		}
		
		if (getEndingGroup() != null && getEndingGroup().isModified())
		{
			if (!ArrayUtils.isEmpty(getConfigGroupEnding().comment()))
				for (String line : getConfigGroupEnding().comment())
					out.println("# " + line);
			
			out.println("## " + getConfigGroupEnding().name());
			out.println("########################################");
			
			out.println();
		}
	}
}

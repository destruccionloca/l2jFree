package com.l2jfree.config.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.config.L2Properties;
import com.l2jfree.config.annotation.ConfigClass;
import com.l2jfree.config.annotation.ConfigField;

public final class ConfigClassInfo
{
	private static final Log _log = LogFactory.getLog(ConfigClassInfo.class);
	
	private static final Map<Class<?>, ConfigClassInfo> _ConfigClasses = new HashMap<Class<?>, ConfigClassInfo>();
	
	public synchronized static ConfigClassInfo valueOf(Class<?> clazz) throws InstantiationException, IllegalAccessException
	{
		ConfigClassInfo info = _ConfigClasses.get(clazz);
		
		if (info == null)
			_ConfigClasses.put(clazz, info = new ConfigClassInfo(clazz));
		
		return info;
	}
	
	private final Class<?> _clazz;
	private final ConfigClass _configClass;
	private final List<ConfigFieldInfo> _infos = new ArrayList<ConfigFieldInfo>();
	
	public ConfigClassInfo(Class<?> clazz) throws InstantiationException, IllegalAccessException
	{
		_clazz = clazz;
		_configClass = _clazz.getAnnotation(ConfigClass.class);
		
		final Map<String, ConfigGroup> activeGroups = new HashMap<String, ConfigGroup>();
		
		for (Field field : _clazz.getFields())
		{
			final ConfigField configField = field.getAnnotation(ConfigField.class);
			
			if (configField == null)
				continue;
			
			if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
			{
				_log.warn("Invalid modifiers for " + field);
				continue;
			}
			
			final ConfigFieldInfo info = new ConfigFieldInfo(field);
			
			_infos.add(info);
			
			if (info.getConfigGroupBeginning() != null)
			{
				final ConfigGroup group = new ConfigGroup();
				
				activeGroups.put(info.getConfigGroupBeginning().name(), group);
				
				info.setBeginningGroup(group);
			}
			
			for (ConfigGroup group : activeGroups.values())
				group.add(info);
			
			if (info.getConfigGroupEnding() != null)
			{
				final ConfigGroup group = activeGroups.remove(info.getConfigGroupEnding().name());
				
				info.setEndingGroup(group);
			}
		}
		
		if (!activeGroups.isEmpty())
			_log.warn("Invalid config grouping!");
	}
	
	public File getConfigFile()
	{
		return new File(_configClass.fileName());
	}
	
	public synchronized void load() throws IOException
	{
		final L2Properties properties = new L2Properties(getConfigFile()).setLog(false);
		
		for (ConfigFieldInfo info : _infos)
			info.setCurrentValue(properties);
	}
	
	public synchronized void store() throws IOException
	{
		final File configFile = getConfigFile();
		
		if (!configFile.getParentFile().exists())
			if (!configFile.getParentFile().mkdirs())
				throw new IOException("Couldn't create required folder structure for " + configFile);
		
		PrintStream ps = null;
		try
		{
			ps = new PrintStream(configFile);
			
			print(ps);
		}
		finally
		{
			IOUtils.closeQuietly(ps);
		}
	}
	
	public synchronized void print(PrintStream out)
	{
		for (ConfigFieldInfo info : _infos)
			info.print(out);
	}
}

package com.l2jfree.config.converters;

import java.lang.reflect.Array;

import com.l2jfree.lang.L2TextBuilder;

public class DefaultArrayConverter implements Converter
{
	public static final DefaultArrayConverter INSTANCE = new DefaultArrayConverter();
	
	@Override
	public Object convertFromString(Class<?> type, String value)
	{
		final Class<?> componentType = type.getComponentType();
		
		if (value.isEmpty())
			return Array.newInstance(componentType, 0);
		
		final String[] splitted = value.split(",");
		final Object array = Array.newInstance(componentType, splitted.length);
		
		for (int i = 0; i < splitted.length; i++)
		{
			Array.set(array, i, DefaultConverter.INSTANCE.convertFromString(componentType, splitted[i]));
		}
		
		return array;
	}
	
	@Override
	public String convertToString(Class<?> type, Object obj)
	{
		final Class<?> componentType = type.getComponentType();
		
		if (obj == null)
			return "";
		
		final int length = Array.getLength(obj);
		final L2TextBuilder tb = L2TextBuilder.newInstance();
		
		for (int i = 0; i < length; i++)
		{
			if (i > 0)
				tb.append(",");
			
			tb.append(DefaultConverter.INSTANCE.convertToString(componentType, Array.get(obj, i)));
		}
		
		return tb.moveToString();
	}
}

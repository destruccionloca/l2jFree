package com.l2jfree.config.converters;

@SuppressWarnings("unchecked")
public final class DefaultConverter implements Converter
{
	public static final DefaultConverter INSTANCE = new DefaultConverter();
	
	@Override
	public Object convertFromString(Class<?> type, String value)
	{
		if (type.isArray())
			return DefaultArrayConverter.INSTANCE.convertFromString(type, value);
		
		if (type == Boolean.class || type == Boolean.TYPE)
		{
			return Boolean.parseBoolean(value);
		}
		else if (type == Long.class || type == Long.TYPE)
		{
			return Long.decode(value);
		}
		else if (type == Integer.class || type == Integer.TYPE)
		{
			return Integer.decode(value);
		}
		else if (type == Short.class || type == Short.TYPE)
		{
			return Short.decode(value);
		}
		else if (type == Byte.class || type == Byte.TYPE)
		{
			return Byte.decode(value);
		}
		else if (type == Double.class || type == Double.TYPE)
		{
			return Double.parseDouble(value);
		}
		else if (type == Float.class || type == Float.TYPE)
		{
			return Float.parseFloat(value);
		}
		else if (type == String.class)
		{
			return value;
		}
		else if (type.isEnum())
		{
			return Enum.valueOf((Class<? extends Enum>)type, value);
		}
		else
		{
			throw new IllegalArgumentException("Not covered type: " + type + "!");
		}
	}
	
	@Override
	public String convertToString(Class<?> type, Object obj)
	{
		if (type.isArray())
			return DefaultArrayConverter.INSTANCE.convertToString(type, obj);
		
		if (obj == null)
			return "";
		
		return obj.toString();
	}
}

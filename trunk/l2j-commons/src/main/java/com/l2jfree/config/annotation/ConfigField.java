package com.l2jfree.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.l2jfree.config.converters.Converter;
import com.l2jfree.config.converters.DefaultConverter;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField
{
	public String name();
	
	public String value();
	
	public String[] comment() default {};
	
	// TODO: handle
	public boolean eternal() default false;
	
	public Class<? extends Converter> converter() default DefaultConverter.class;
}

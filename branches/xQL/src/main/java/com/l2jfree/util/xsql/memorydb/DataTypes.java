/**
 * 
 */
package com.l2jfree.util.xsql.memorydb;

import com.l2jfree.util.xsql.parser.XsQLBaseTupple;

/**
 * @author noctarius
 *
 */
public enum DataTypes {
	STRING("string", String.class),
	
	CLASS("class", Class.class),
	ENUM("enum", Enum.class),
	
	BYTE("byte", Byte.class),
	SHORT("short", Short.class),
	INTEGER("integer", Integer.class),
	LONG("long", Long.class),
	
	FLOAT("float", Float.class),
	
	BOOLEAN("boolean", Boolean.class),
	
	TUPPLE("tupple", XsQLBaseTupple.class),
	;
	
	private final Class _clazz;
	private final String _name;
	
	private DataTypes(String name, Class clazz) {
		_name = name;
		_clazz = clazz;
	}
	
	public String getName() {
		return _name;
	}
	
	public Class getDataTypeClass() {
		return _clazz;
	}
}

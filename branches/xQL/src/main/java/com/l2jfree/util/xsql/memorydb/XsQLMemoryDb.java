/**
 * 
 */
package com.l2jfree.util.xsql.memorydb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @author noctarius
 *
 */
public class XsQLMemoryDb<T extends Class> {
	private class Datafolder {
		private List<?> _rows;
		
		public Datafolder(DataTypes type) {
			_rows = new LinkedList<T>();
		}
	}
	
	private final String _category;
	private final String _datasource;
	
	private Map<String, Datafolder> _columns;
	
	public XsQLMemoryDb(String category, String datasource, Node node) {
		_category = category;
		_datasource = datasource;
	}
}

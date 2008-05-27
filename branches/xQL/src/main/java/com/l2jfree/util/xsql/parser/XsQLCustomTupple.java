/**
 * 
 */
package com.l2jfree.util.xsql.parser;

import com.l2jfree.util.xsql.XsQLDataSource;

/**
 * @author noctarius
 *
 */
public class XsQLCustomTupple extends XsQLBaseTupple {
	private String _name;
	
	private XsQLCustomTupple(String tupple, String method, XsQLDataSource datasource) {
		super(tupple, method, datasource);
	}

	public String getName() {
		return _name;
	}
}

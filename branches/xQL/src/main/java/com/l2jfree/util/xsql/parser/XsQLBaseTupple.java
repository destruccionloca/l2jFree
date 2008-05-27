/**
 * 
 */
package com.l2jfree.util.xsql.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfree.util.xsql.XsQLDataSource;

/**
 * @author noctarius
 *
 */
public class XsQLBaseTupple {
	private static List<XsQLBaseTupple> _tuppleList = new ArrayList<XsQLBaseTupple>();
	private static Map<Integer, List<XsQLCustomTupple>> _customTuppleMap = new HashMap<Integer, List<XsQLCustomTupple>>();
	
	private String _tupple;
	private String _regex;
	private String _method = null;
	
	
	private XsQLBaseTupple(String tupple, String regex) {
		_tupple = tupple;
		_regex = regex;
	}
	protected XsQLBaseTupple(String tupple, String method, XsQLDataSource datasource) {
		_tupple = tupple;
		_method = method;
	}

	
	public String getName() {
		return _tupple;
	}
	
	
	public static final XsQLBaseTupple forName(String tupple, XsQLDataSource datasource) throws XsQLException {
		try {
			return forName(tupple);
		} catch (XsQLException e) {
			if (e.getError() == XsQLException.XsQLExceptionError.XSQL_TUPPLE_NOT_FOUND_EXCEPTION) {
				List<XsQLCustomTupple> tupples = _customTuppleMap.get(datasource.getInMemoryId());
				
				if (tupples != null)
					for (XsQLCustomTupple t : tupples)
						if (t.getName().equalsIgnoreCase(tupple))
							return new XsQLBaseTupple(t.getName(), "");
			}
		}
		
		throw new XsQLException(XsQLException.XsQLExceptionError.XSQL_TUPPLE_NOT_FOUND_EXCEPTION);
	}
	public static final XsQLBaseTupple forName(String tupple) throws XsQLException {
		for (XsQLBaseTupple sql : _tuppleList)
			if (sql.getName().equalsIgnoreCase(tupple))
				return sql;
		
		throw new XsQLException(XsQLException.XsQLExceptionError.XSQL_TUPPLE_NOT_FOUND_EXCEPTION);
	}
	
	public static final void register(String tupple, String regex) {
		
	}
}

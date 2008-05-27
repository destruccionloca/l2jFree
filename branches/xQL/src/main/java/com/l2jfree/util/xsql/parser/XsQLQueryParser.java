/**
 * 
 */
package com.l2jfree.util.xsql.parser;

/**
 * @author noctarius
 *
 */
public class XsQLQueryParser {
	private enum SqlTupple {
		SELECT,
		FROM,
		WHERE,
		AND,
		OR,
		ORDER_BY,
		IN;
		
		public SqlTupple forName(String tupple) throws XsQLException {
			for (SqlTupple sql : SqlTupple.values())
				if (sql.name().equalsIgnoreCase(tupple))
					return sql;
			
			throw new XsQLException(XsQLException.XsQLExceptionError.XSQL_TUPPLE_NOT_FOUND_EXCEPTION);
		}
	}
}

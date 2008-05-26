/**
 * 
 */
package com.l2jfree.util.xql.parser;

/**
 * @author noctarius
 *
 */
public class XQLQueryParser {
	private enum SqlTupple {
		SELECT,
		FROM,
		WHERE,
		AND,
		OR,
		ORDER_BY,
		IN;
		
		public SqlTupple forName(String tupple) throws XQLException {
			for (SqlTupple sql : SqlTupple.values())
				if (sql.name().equalsIgnoreCase(tupple))
					return sql;
			
			throw new XQLException(XQLException.XQLExceptionError.XQL_TUPPLE_NOT_FOUND_EXCEPTION);
		}
	}
}

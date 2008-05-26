/**
 * 
 */
package com.l2jfree.util.xql.parser;

/**
 * @author noctarius
 *
 */
public class XQLException extends Exception {
	private static final long serialVersionUID = 1119150299542112087L;

	public enum XQLExceptionError {
		XQL_TUPPLE_NOT_FOUND_EXCEPTION ("XQL statement tupple is invalid"),
		XQL_NO_ACCESS_RIGHT_EXCEPTION ("No accessrights to datafile"),
		XQL_NO_SUCH_DATAFILE_EXCEPTION ("Datafile was not found"),
		XQL_NO_SUCH_CATEGORY_EXCEPTION ("Category was not found"),
		XQL_NO_SUCH_COLUMN_EXCEPTION ("Column was not found"),
		XQL_PARSE_XML_EXCEPTION ("Datafile could not be parsed"),
		XQL_XML_IMPORT_EXCEPTION ("Import xml points to a none existing file at line"),
		XQL_INCORRECT_XQL_STATEMENT ("Statement error at line"),
		XQL_NO_DATA_EXCEPTION ("No more data available"),
		XQL_INDEX_OUT_OF_BOUND_EXCEPTION ("No column exists with index"),
		XQL_WRONG_DATATYPE_REQUEST_EXCEPTION ("Wrong datatype requested for column"),
		XQL_WRONG_XQL_FILEFORMAT_VERSION ("Wrong xQL format revision in file"),
		XQL_NO_XQL_FILEFORMAT_VERSION ("Missing xQL header in file"),
		;
		
		private String _message;
		
		private XQLExceptionError(String message) {
			_message = message;
		}
		
		public String getMessage() {
			return _message;
		}
	}
	
	public XQLException(XQLExceptionError error) {
		
	}
}

/**
 * 
 */
package com.l2jfree.util.xsql.parser;

/**
 * @author noctarius
 *
 */
public class XsQLException extends Exception {
	private static final long serialVersionUID = 1119150299542112087L;

	public enum XsQLExceptionError {
		XSQL_NO_ERROR_OCCURED ("No error occured"),
		XSQL_TUPPLE_NOT_FOUND_EXCEPTION ("XsQL statement tupple is invalid"),
		XSQL_NO_ACCESS_RIGHT_EXCEPTION ("No accessrights to datafile"),
		XSQL_NO_SUCH_DATAFILE_EXCEPTION ("Datafile was not found"),
		XSQL_NO_SUCH_CATEGORY_EXCEPTION ("Category was not found"),
		XSQL_NO_SUCH_COLUMN_EXCEPTION ("Column was not found"),
		XSQL_PARSE_XML_EXCEPTION ("Datafile could not be parsed"),
		XSQL_XML_IMPORT_EXCEPTION ("Import xml points to a none existing file at line"),
		XSQL_INCORRECT_XSQL_STATEMENT ("Statement error at line"),
		XSQL_NO_DATA_EXCEPTION ("No more data available"),
		XSQL_INDEX_OUT_OF_BOUND_EXCEPTION ("No column exists with index"),
		XSQL_WRONG_DATATYPE_REQUEST_EXCEPTION ("Wrong datatype requested for column"),
		XSQL_WRONG_XSQL_FILEFORMAT_VERSION ("Wrong XsQL format revision in file"),
		XSQL_NO_XSQL_FILEFORMAT_VERSION ("Missing XsQL header in file"),
		;
		
		private String _message;
		
		private XsQLExceptionError(String message) {
			_message = message;
		}
		
		public String getMessage() {
			return _message;
		}
	}
	
	
	private XsQLExceptionError _error = XsQLExceptionError.XSQL_NO_ERROR_OCCURED;
	private String _info = "";
	
	public XsQLException(XsQLExceptionError error) {
		_error = error;
	}

	public XsQLException(XsQLExceptionError error, String info) {
		_error = error;
		_info = info;
	}
	
	public String getMessage() {
		return _error.getMessage().concat(" ").concat(_info);
	}
	public String getLocalizedMessage() {
		return _error.getMessage().concat(" ").concat(_info);
	}
	
	public String toString() {
		return _error.getMessage().concat(" ").concat(_info);
	}
	
	public XsQLExceptionError getError() {
		return _error;
	}
}

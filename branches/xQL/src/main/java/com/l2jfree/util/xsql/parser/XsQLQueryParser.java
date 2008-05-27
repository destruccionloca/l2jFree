/**
 * 
 */
package com.l2jfree.util.xsql.parser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author noctarius
 *
 */
public class XsQLQueryParser {
	private final String[][] sqlTupples = {
		new String[] {"select", "SELECT [a-zA-Z0-9]*"},
		new String[] {"from", ""},
		new String[] {"in", ""},
		new String[] {"where", ""},
		new String[] {"order by", ""},
		new String[] {"limit", ""},

		new String[] {"load", ""},
		new String[] {"unload", ""},
		new String[] {"reload", ""},

		new String[] {"handler", ""},

		new String[] {"execute", ""},

		new String[] {"use", ""},
		new String[] {"count", ""},

		new String[] {"and", ""},
		new String[] {"or", ""},
		new String[] {">", ""},
		new String[] {"<", ""},
		new String[] {"=", ""},
		new String[] {"!=", ""},
		new String[] {"like", ""},
		new String[] {"between", ""},
	};
		
	private static final XsQLQueryParser _instance = new XsQLQueryParser();
	
	public static final XsQLQueryParser getInstance() {
		return _instance;
	}
	 
	private XsQLQueryParser() {
		// Building list with standard XsQL tupples
		initBaseTupples();
	}
	
	public void initDataDirectory(String path) throws XsQLException {
		initDataDirectory((new File(path)));
	}
	public void initDataDirectory(File path) throws XsQLException {
		//TODO: implementing datasource loading
	}
	
	private void initBaseTupples() {
		for (String[] tupple : sqlTupples)
			XsQLBaseTupple.register(tupple[0], tupple[1]);
	}
}

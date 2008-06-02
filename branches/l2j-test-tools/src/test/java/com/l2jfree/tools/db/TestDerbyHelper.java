package com.l2jfree.tools.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.l2jfree.tools.db.DerbyHelper;

import junit.framework.TestCase;

public class TestDerbyHelper extends TestCase {
	public void testStartup() {
		DerbyHelper.startup();
		try {
			Connection conn = DerbyHelper.getConnection();
			assertNotNull(conn);
		} catch (InstantiationException e) {
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			fail(e.getMessage());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		DerbyHelper.shutdown();
	}
}

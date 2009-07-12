package com.l2jfree;

import junit.framework.TestCase;

public class TestConfig extends TestCase
{
	/**
	 * test the good loading
	 */
	public void testLoadConfig()
	{
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * test that db properties are in system properties
	 */
	public void testInitDbProperties()
	{
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail(e.getMessage());
		}
		assertNotNull(System.getProperty("com.l2jfree.db.driverclass"));
		assertNotNull(System.getProperty("com.l2jfree.db.urldb"));
		assertNotNull(System.getProperty("com.l2jfree.db.user"));
		assertNotNull(System.getProperty("com.l2jfree.db.password"));

	}

}

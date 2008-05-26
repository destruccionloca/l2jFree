package net.sf.l2j;

import junit.framework.TestCase;

public class TestConfig extends TestCase 
{
	/**
	 * test the good loading
	 */
	public void testLoadConfig ()
	{	
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail (e.getMessage());
		}
	}
	
	/**
	 * test that db properties are in system properties
	 */
	public void testInitDbProperties ()
	{	
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail (e.getMessage());
		}
		assertNotNull (System.getProperty("net.sf.l2j.db.driverclass"));
		assertNotNull (System.getProperty("net.sf.l2j.db.urldb"));
		assertNotNull (System.getProperty("net.sf.l2j.db.user"));
		assertNotNull (System.getProperty("net.sf.l2j.db.password"));
		
	}	
	
	
}

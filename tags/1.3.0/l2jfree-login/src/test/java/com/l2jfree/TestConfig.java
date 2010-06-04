/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

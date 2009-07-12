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
package com.l2jfree.tools.log4jextension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for ExtendedPatternLayoutTest<BR>
 * Broken behavior fixed by savormix
 */
public class ExtendedPatternLayoutTest extends TestCase
{
	private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private static int position = 0;
	private PrintStream stdout;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		stdout = System.out;
		System.setOut(new PrintStream(baos, true));
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		System.setOut(stdout);
		super.tearDown();
	}

	/**
	 * Test Basic logging (just a string)
	 */
	public void testBasicString()
	{
		try
		{
			// log in consoleAppender
			Log logger = LogFactory.getLog(ExtendedPatternLayoutTest.class);
			logger.info("Basic string");

			String content = baos.toString().substring(position);
			// Check value
			assertEquals("Basic string", content.trim());
			position += content.length();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test throwable string
	 */
	public void testThrowable()
	{
		try
		{
			// log in consoleAppender
			Log logger = LogFactory.getLog(ExtendedPatternLayoutTest.class);
			Exception e = new Exception("Exception for bad error...");
			e.setStackTrace(new StackTraceElement[] { new StackTraceElement(
					"class toto", "methode titi", "file fifi", 1) });
			logger.info("exception", e);

			String content = baos.toString().substring(position);
			// Check value
			assertEquals(
					"exception java.lang.Exception: Exception for bad error..."
							+ System.getProperty("line.separator")
							+ "\tat class toto.methode titi(file fifi:1)", content.trim());
			position += content.length();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test content for a logging event With a a list containing a
	 * L2ItemInstance and a null value
	 */
	public void testLogItemEvent()
	{
		try
		{
			// Create a dummy item
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("val1", "player1");
			item.put("val2", 216565);

			// prepare a list to be logged
			List<Object> param = new ArrayList<Object>();
			param.add("CHANGE : Pickup ");
			param.add("player corwin");
			param.add(item);
			param.add(null);

			// log in consoleAppender
			Log logger = LogFactory.getLog(ExtendedPatternLayoutTest.class);
			logger.info(param);

			String content = baos.toString().substring(position);
			// Check value
			assertEquals("[CHANGE : Pickup , player corwin, {val1=player1, val2=216565}, null]",
					content.trim());
			position += content.length();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}

/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree.loginserver.manager;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.l2jfree.L2Registry;

/**
 * This class test ban management
 * 
 */
public class GameServerManagerTest extends TestCase
{
	private ClassPathXmlApplicationContext	context	= null;
	private GameServerManager				gsManager;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		context = new ClassPathXmlApplicationContext("classpath*:/**/**/applicationContext-TestMock.xml");
		L2Registry.setApplicationContext(context);
		gsManager = GameServerManager.getInstance();
	}

	/**
	 * Check if 127 server from servernames.xml are loaded
	 *
	 */
	public void testIsLoaded()
	{
		assertEquals(127, gsManager.getServers().size());
	}
}

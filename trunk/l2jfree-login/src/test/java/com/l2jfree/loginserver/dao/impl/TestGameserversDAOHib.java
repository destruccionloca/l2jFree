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
package com.l2jfree.loginserver.dao.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.l2jfree.loginserver.beans.Gameservers;
import com.l2jfree.tools.db.hibernate.ADAOTestCase;

/**
 * Test account DAO
 * 
 */
public class TestGameserversDAOHib extends ADAOTestCase
{
	private Gameservers			gameserver	= null;
	private GameserversDAOHib	dao			= null;

	public TestGameserversDAOHib(String name)
	{
		super(name);
	}

	public void setGameserversDao(GameserversDAOHib _dao)
	{
		this.dao = _dao;
	}

	@Override
	public String[] getMappings()
	{
		return new String[]
		{ "Gameservers.hbm.xml" };
	}

	@Override
	protected List<IDataSet> getDataSet() throws Exception
	{
		String[] dataSetNameList =
		{ "gameservers.xml" };
		String dtdName = "database/l2jdb.dtd";
		List<IDataSet> dataSetList = new ArrayList<IDataSet>();

		InputStream inDTD = this.getClass().getResourceAsStream(dtdName);
		FlatDtdDataSet dtdDataSet = new FlatDtdDataSet(inDTD);
		for (int indice = 0; indice < dataSetNameList.length; indice++)
		{
			InputStream in = this.getClass().getResourceAsStream(dataSetNameList[indice]);
			IDataSet dataSet = new FlatXmlDataSet(in, dtdDataSet);
			dataSetList.add(dataSet);
		}
		return dataSetList;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setUp() throws Exception
	{

		super.setUp();
		// Set DAO to test
		setGameserversDao(new GameserversDAOHib());
		dao.setCurrentSession(getSession());
	}

	public void testFindGameserver() throws Exception
	{
		gameserver = dao.getGameserverByServerId(0);

		assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
		assertEquals("*", gameserver.getHost());
	}

	public void testModifyGameserver() throws Exception
	{
		// retrieve object
		gameserver = dao.getGameserverByServerId(0);

		assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
		assertEquals("*", gameserver.getHost());

		// modify object
		gameserver.setHost("localhost");
		dao.update(gameserver);

		// check modification
		gameserver = dao.getGameserverByServerId(0);
		assertEquals("651de5d23464e255346a36d0bbb1966a", gameserver.getHexid());
		assertEquals("localhost", gameserver.getHost());

		// cancel modification
		gameserver.setHost("");
		dao.update(gameserver);
	}

	public void testAddAndRemoveGameservers() throws Exception
	{

		// Add Gameserver
		gameserver = new Gameservers();
		gameserver.setHexid("hexid1");
		gameserver.setHost("*");

		int id = dao.createGameserver(gameserver);
		System.out.println("Gameserver created with id : " + id);
		assertEquals(gameserver.getHexid(), "hexid1");

		// delete Gameserver
		dao.removeGameserver(gameserver);

		try
		{
			gameserver = dao.getGameserverByServerId(id);
			fail("Gameservers found in database");
		}
		catch (ObjectRetrievalFailureException dae)
		{
			assertNotNull(dae);
		}
	}

	public void testFindNonExistentGameserver() throws Exception
	{

		try
		{
			gameserver = dao.getGameserverByServerId(666);
			fail("Gameservers found in database");
		}
		catch (DataAccessException dae)
		{
			assertNotNull(dae);
		}
	}

	public void testFindAll() throws Exception
	{

		List<Gameservers> list = dao.getAllGameservers();

		assertEquals(1, list.size());

		// Add Gameserver
		gameserver = new Gameservers();
		gameserver.setHexid("hexid2");
		gameserver.setHost("*");

		dao.createGameserver(gameserver);
		dao.getCurrentSession().flush();

		assertEquals(1, list.size());

		list = dao.getAllGameservers();

		assertEquals(2, list.size());

		dao.removeGameserver(gameserver);
		dao.getCurrentSession().flush();

		list = dao.getAllGameservers();

		assertEquals(1, list.size());

		Iterator<Gameservers> it = list.iterator();
		int idPrevious = -1;
		// check that the list is ordered by serverId
		while (it.hasNext())
		{
			Gameservers gs = it.next();
			assertTrue(gs.getServerId() > idPrevious);
			idPrevious = gs.getServerId();
		}

	}

	public void testRemoveObject() throws Exception
	{

		// Add Gameserver
		gameserver = new Gameservers();
		gameserver.setHexid("hexid2");
		gameserver.setHost("hexid2");

		dao.createGameserver(gameserver);

		dao.removeGameserver(gameserver);

		List<?> list = dao.getAllGameservers();

		assertEquals(1, list.size());
	}

	public void testRemoveAll() throws Exception
	{

		testAddAllAndRemove();

		dao.removeAll();

		dao.getCurrentSession().flush();
		List<?> list;
		list = dao.getAllGameservers();
		assertEquals(0, list.size());
	}

	public void testAddAllAndRemove() throws Exception
	{

		// Add multiple Gameserver
		List<Gameservers> listGameserver = new ArrayList<Gameservers>();

		Gameservers acc = new Gameservers();
		acc.setHexid("hexid1");
		acc.setHost("toto@test.com");

		listGameserver.add(acc);

		acc = new Gameservers();
		acc.setHexid("hexid2");
		acc.setHost("toto2@test.com");

		listGameserver.add(acc);

		acc = new Gameservers();
		acc.setHexid("hexid3");
		acc.setHost("toto3@test.com");

		listGameserver.add(acc);

		dao.createOrUpdateAll(listGameserver);
		dao.getCurrentSession().flush();

		List<?> list = dao.getAllGameservers();

		assertEquals(4, list.size());

		dao.removeAll(listGameserver);
		dao.getCurrentSession().flush();

		list = dao.getAllGameservers();

		assertEquals(1, list.size());

	}

}
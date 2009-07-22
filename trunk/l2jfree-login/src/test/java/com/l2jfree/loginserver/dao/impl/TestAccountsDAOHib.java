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
package com.l2jfree.loginserver.dao.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.l2jfree.loginserver.beans.Accounts;
import com.l2jfree.tools.db.hibernate.ADAOTestCase;

/**
 * Test account DAO
 * 
 */
public class TestAccountsDAOHib extends ADAOTestCase
{
	private Accounts		account	= null;
	private AccountsDAOHib	dao		= null;

	public TestAccountsDAOHib(String name)
	{
		super(name);
	}

	@Override
	public String[] getMappings()
	{
		return new String[]
		{ "Accounts.hbm.xml" };
	}

	public void setAccountDao(AccountsDAOHib _dao)
	{
		dao = _dao;
	}

	@Override
	protected List<IDataSet> getDataSet() throws Exception
	{
		String[] dataSetNameList =
		{ "accounts.xml" };
		String dtdName = "database/l2jdb.dtd";
		List<IDataSet> dataSetList = new ArrayList<IDataSet>();

		InputStream inDTD = this.getClass().getResourceAsStream(dtdName);
		FlatDtdDataSet dtdDataSet = new FlatDtdDataSet(inDTD);
		for (String finalElement : dataSetNameList)
		{
			InputStream in = this.getClass().getResourceAsStream(finalElement);
			IDataSet dataSet = new FlatXmlDataSet(in, dtdDataSet);
			dataSetList.add(dataSet);
		}
		return dataSetList;
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		// Set DAO to test
		setAccountDao(new AccountsDAOHib());
		dao.setCurrentSession(getSession());
	}

	public void testFindAccount() throws Exception
	{

		account = dao.getAccountById("player1");

		assertEquals("player1", account.getLogin());
		assertEquals(4, account.getAccessLevel().intValue());

	}

	public void testModifyAccount() throws Exception
	{
		// retrieve object
		account = dao.getAccountById("player1");

		assertEquals("player1", account.getLogin());
		assertEquals(4, account.getAccessLevel().intValue());

		// modify object
		account.setAccessLevel(7);
		account.setLastactive(new BigDecimal(System.currentTimeMillis()));
		account.setLastIp("127.0.0.1");
		dao.createOrUpdate(account);
		dao.getCurrentSession().flush();

		// check modification
		account = dao.getAccountById("player1");
		assertEquals("player1", account.getLogin());
		assertEquals(7, account.getAccessLevel().intValue());

		// cancel modification
		account.setAccessLevel(4);
		account.setLastactive(new BigDecimal(System.currentTimeMillis()));
		account.setLastIp("127.0.0.1");
		dao.createOrUpdate(account);
		dao.getCurrentSession().flush();
	}

	public void testAddAndRemoveAccounts() throws Exception
	{

		// Add account
		account = new Accounts();
		account.setLogin("Bill");
		account.setPassword("testPw");
		account.setLastIp("127.0.0.1");

		dao.createAccount(account);

		assertEquals(account.getLogin(), "Bill");

		// delete account
		dao.removeAccount(account);

		try
		{
			account = dao.getAccountById("Bill");
			fail("Accounts found in database");
		}
		catch (ObjectRetrievalFailureException dae)
		{
			assertNotNull(dae);
		}
	}

	public void testFindNonExistentAccount() throws Exception
	{

		try
		{
			account = dao.getAccountById("Unknown");
			fail("Accounts found in database");
		}
		catch (DataAccessException dae)
		{
			assertNotNull(dae);
		}
	}

	public void testFindAll() throws Exception
	{

		List<?> list = dao.getAllAccounts();

		assertEquals(1, list.size());

		// Add account
		account = new Accounts();
		account.setLogin("Bill");
		account.setPassword("testPw");
		account.setLastIp("127.0.0.4");

		dao.createAccount(account);
		dao.getCurrentSession().flush();

		assertEquals(1, list.size());

		list = dao.getAllAccounts();

		assertEquals(2, list.size());

		dao.removeAccount(account);
		dao.getCurrentSession().flush();

		list = dao.getAllAccounts();

		assertEquals(1, list.size());
	}

	public void testRemoveObject() throws Exception
	{

		// Add account
		account = new Accounts();
		account.setLogin("Bill");
		account.setPassword("testPw");
		account.setLastIp("127.0.0.1");

		dao.createAccount(account);

		dao.removeAccount(account);

		List<?> list = dao.getAllAccounts();

		assertEquals(1, list.size());
	}

	public void testAddAllAndRemove() throws Exception
	{

		// Add multiple account
		List<Accounts> listAccount = new ArrayList<Accounts>();

		Accounts acc = new Accounts();
		acc.setLogin("Bill");
		acc.setPassword("testPw");
		acc.setLastIp("127.0.0.1");

		listAccount.add(acc);

		acc = new Accounts();
		acc.setLogin("BigBill");
		acc.setPassword("anotherPw");
		acc.setLastIp("127.0.0.2");

		listAccount.add(acc);

		acc = new Accounts();
		acc.setLogin("Matt");
		acc.setPassword("anotherPw2");
		acc.setLastIp("127.0.0.3");

		listAccount.add(acc);

		dao.createOrUpdateAll(listAccount);
		dao.getCurrentSession().flush();

		List<?> list = dao.getAllAccounts();

		assertEquals(4, list.size());

		dao.removeAll(listAccount);
		dao.getCurrentSession().flush();

		list = dao.getAllAccounts();

		assertEquals(1, list.size());

	}

}

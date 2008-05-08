/*
 *  
 */

package net.sf.l2j.tools.db.spring;

import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractSingleSpringContextTests;


/**
 * Test class made to use DBUnit and spring.
 * 
 * 
 */
public abstract class ADAOTestWithSpring extends AbstractSingleSpringContextTests {
	
	private static final String SPRING_DBEMPTY_XML = "springDBEmpty.xml";

	/** Logger commons-logging for this class */
	private static final Log s_log = LogFactory
			.getLog(ADAOTestWithSpring.class);

	public ADAOTestWithSpring(String name) {
		super(name);
	}

	protected String[] getConfigLocations() {
		return new String[] { SPRING_DBEMPTY_XML };
	}

	protected void onSetUp() throws Exception {
		setUpDBUnit();
	}

	/** Return a bean for the default context */
	public Object getBean(final String _beanName) {
		try {
			ApplicationContext applicationContext = getContext(getConfigLocations());
			Object bean = applicationContext.getBean(_beanName);
			return bean;
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}

	/**
	 * @throws DatabaseUnitException
	 * @throws SQLException
	 */
	protected void setUpDBUnit() throws Exception {
		IDatabaseConnection connection = getConnection();

		try {
			DatabaseOperation.CLEAN_INSERT.execute(connection,
					getInitialDataSet());
		} catch (Exception e) {
			// on loggue en debug (seulement pour le dev)
			if (s_log.isDebugEnabled()) {
				s_log.debug(e);
			}

			fail(e.getMessage());
		} finally {
			connection.close();
		}
	}

	/**
	 * Return a dbunit connection. <b>Warning</b> This connection have 
	 * to be close.
	 * 
	 * @return a  DBUnit connection
	 * @throws SQLException
	 */
	public IDatabaseConnection getConnection() throws SQLException {
		// Get a connection
		DataSource ds = (DataSource) getBean("dataSource");
		assertNotNull(ds);

		// Initialize a connection
		IDatabaseConnection connection = null;
		connection = new DatabaseConnection(ds.getConnection());
		return connection;
	}


	/** Return initial dataset to populate an empty database */
	public IDataSet getInitialDataSet() {
		return getDataSet(getInitialDataSetName());
	}

	/** Return the initial dataset name */
	public abstract String getInitialDataSetName();

	/** Return the folder that holds datasets */
	public abstract String getRootDirName();

	/**
	 * Return the dataset for a specific file
	 * <code>_fileName</code>
	 * 
	 * @param _fileName
	 *            filename that have to be in the classpath
	 * @return the dataset
	 */
	public IDataSet getDataSet(final String _fileName) {
		try {
			String dtdName = "database/database.dtd";
			InputStream in = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(_fileName);
			if (in == null) {
				if (s_log.isDebugEnabled())
					s_log.debug("File [" + _fileName
							+ "] not found in classpath");

				// Recherche à la racine
				String newFileName = getRootDirName() + "/" + _fileName;
				in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(newFileName);

				if (in == null) {
					fail("File [" + _fileName + "] not found in classpath");
				}
			}

			InputStream inDTD = this.getClass().getResourceAsStream(dtdName);
			if (inDTD == null) {
				fail("File [" + dtdName + "] not found in classpath");
			}

			FlatDtdDataSet dtdDataSet = new FlatDtdDataSet(inDTD);
			IDataSet dataset;

			dataset = new FlatXmlDataSet(in, dtdDataSet);

			return dataset;
		} catch (Exception e) {
			if (s_log.isDebugEnabled()) {
				s_log.debug(e);
			}

			fail(e.getMessage());
			return null;
		}
	}

	/**
	 * Check two dataset
	 * 
	 * @param _fileName
	 *            the expected dataset
	 * 
	 * @see Assertion
	 */
	public void assertEqualsDatabaseDataSet(final String _fileName) {
		// Get dataset in database
		IDataSet databaseDataSet = null;
		try {
			databaseDataSet = getConnection().createDataSet();

			IDataSet xmlDataSet = getDataSet(_fileName);

			for (int i = 0; i < xmlDataSet.getTableNames().length; i++) {
				String tableName = xmlDataSet.getTableNames()[i];

				ITable expectedTable = xmlDataSet.getTable(tableName);
				ITable actualTable = databaseDataSet.getTable(tableName);

				Assertion.assertEquals(new SortedTable(expectedTable),
						new SortedTable(actualTable));
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}

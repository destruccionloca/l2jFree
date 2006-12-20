package net.sf.l2j.loginserver.dbunit;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Wrapped version of DBUnits DatabaseDataSourceConnection to enable Spring
 * Transaction support.
 */
public class SpringDatabaseDataSourceConnection extends
		DatabaseDataSourceConnection {

	private DataSource dataSource;

	private Connection conn;

	/**
	 * @param dataSource
	 * @throws SQLException
	 */
	public SpringDatabaseDataSourceConnection(DataSource _dataSource)
			throws SQLException {
		super(_dataSource);
		this.dataSource = _dataSource;
	}

	/**
	 * @see org.dbunit.database.IDatabaseConnection#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		Connection _conn = DataSourceUtils.getConnection(dataSource);

		this.conn = new SpringConnection(dataSource, _conn);

		return this.conn;
	}

	public void setDataTypeFactory(IDataTypeFactory value) {
		getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, value);
	}

	public void close() throws java.sql.SQLException {
			conn.close();
	}
}
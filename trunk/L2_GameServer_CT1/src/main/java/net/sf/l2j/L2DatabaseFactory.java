/*
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
package net.sf.l2j;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sf.l2j.tools.L2Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;

import com.mchange.v2.c3p0.PooledDataSource;

public class L2DatabaseFactory
{
    private final static Log _log = LogFactory.getLog(L2DatabaseFactory.class.getName());

    public static enum ProviderType
    {
        MySql,
        MsSql
    }

    // =========================================================
    // Data Field
    private static L2DatabaseFactory _instance;
    private ProviderType _providerType;
	
    /**
     * Private constructor to avoid direct initialization
     * Load the registry if it does not already exist
     * Initialize the provider Type    
     * @throws Throwable if the registry wasn't load
     */
	private L2DatabaseFactory() throws Throwable
	{
		try
		{
            if ( L2Registry.getApplicationContext() == null )
            {
                L2Registry.loadRegistry(new String[]{"spring_dao.xml",
                                                     "spring_services.xml",
                                                     "spring_tech.xml",
                                                     "spring_manager.xml"});
            }
            
            if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
                _providerType = ProviderType.MsSql;
            else
                _providerType = ProviderType.MySql;
		}
		catch (Throwable e)
		{
			if (_log.isDebugEnabled()) _log.debug("Database Connection FAILED");
			throw e;
		}
	}
    
    // =========================================================
    // Method - Public
    public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
    {
        String msSqlTop1 = "";
        String mySqlTop1 = "";
        if (returnOnlyTopRecord)
        {
            if (getProviderType() == ProviderType.MsSql) msSqlTop1 = " Top 1 ";
            if (getProviderType() == ProviderType.MySql) mySqlTop1 = " Limit 1 ";
        }
        String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
        return query;
    }
    
    /**
     * Destroy the datasource
     * 
     * @deprecated don't use anymore, spring destroy the datasource for you
     */
    public void shutdown()
    {
    }

    public final String safetyString(String[] whatToCheck)
    {
        // NOTE: Use brace as a safty percaution just incase name is a reserved word
        String braceLeft = "`";
        String braceRight = "`";
        if (getProviderType() == ProviderType.MsSql)
        {
            braceLeft = "[";
            braceRight = "]";
        }

        String result = "";
        for(String word : whatToCheck)
        {
            if(result != "") result += ", ";
            result += braceLeft + word + braceRight;
        }
        return result;
    }
    
    /**
     * This method just return the singleton _instance
     * Don't forget to call initialize in the main of the program
     * before using this method
     * @return L2DatabaseFactory
     * @see net.sf.l2j.L2DatabaseFactory.initInstance
     */
	public static L2DatabaseFactory getInstance() 
	{
		return _instance;
	}
	
    /**
     * Temporarily create a initInstance that just create the _instance
     * Later, we will use L2Registry instead of L2DatabaseFactory 
     * but the first step should keep compatibility with old jdbc connexion
     * 
     * The goal of this method is to separate the problematic of instanciation
     * and the problem of the use.
     * During the instantiation of the Registry (first call), we may have throwable
     * but we don't want to catch Throwable every time we call getInstance
     *  
     * @return L2DatabaseFactory
     * @throws Throwable 
     */
    public static L2DatabaseFactory initInstance() throws Throwable 
    {
        if (_instance == null)
        {
            _instance = new L2DatabaseFactory();
        }
        return _instance;
    }    
	
    /**
     * if con is not null, return the same connection
     * dev have to close it !
     * @param con
     * @return
     */
    public Connection getConnection(Connection con) 
    {
        if (con == null)
        {   
            try
            {
                con = ((DataSource)L2Registry.getBean("dataSource")).getConnection();
            }
            catch (BeansException e)
            {
                _log.fatal("Unable to retrieve connection : " +e.getMessage(),e);
            }
            catch (SQLException e)
            {
                _log.fatal("Unable to retrieve connection : " +e.getMessage(),e);
            }
        }
        return con;
    }
	
	public int getBusyConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) L2Registry.getBean("dataSource")).getNumBusyConnectionsDefaultUser();
	}

	public int getIdleConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) L2Registry.getBean("dataSource")).getNumIdleConnectionsDefaultUser();
	}

    public final ProviderType getProviderType() { return _providerType; }
}

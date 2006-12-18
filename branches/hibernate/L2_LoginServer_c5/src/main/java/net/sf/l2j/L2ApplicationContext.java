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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class L2ApplicationContext
{
    private static Log _log = LogFactory.getLog(L2ApplicationContext.class.getName());
    
    private ApplicationContext __ctx = null;

    public static enum ProviderType
    {
        MySql,
        MsSql
    }

    // =========================================================
    // Data Field
    private static L2ApplicationContext _instance;
    private ProviderType _Provider_Type = ProviderType.MySql; // devault value
	
    // =========================================================
    // Constructor
	public L2ApplicationContext()
	{
        try
        {
            // init properties for spring (and database)
            String[] paths = {"spring.xml"};
            __ctx = new ClassPathXmlApplicationContext(paths);
        }
        catch (Throwable e)
        {
            _log.fatal("Unable to connect : " + e.getMessage(),e);
            System.exit(1);
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

    public static void shutdown()
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

    // =========================================================
    // Property - Public
	public static L2ApplicationContext getInstance() 
	{
		if (_instance == null)
		{
			_instance = new L2ApplicationContext();
		}
		return _instance;
	}
	
	public Connection getConnection() 
	{
		Connection con=null;
 
        try
        {
            con = ((DataSource)__ctx.getBean("dataSource")).getConnection();
        }
        catch (BeansException e)
        {
            _log.fatal("Unable to retrieve connection : " +e.getMessage(),e);
        }
        catch (SQLException e)
        {
            _log.fatal("Unable to retrieve connection : " +e.getMessage(),e);
        }
		return con;
	}
    
    public ApplicationContext getApplicationContext() 
    {
        return __ctx;
    }
        
	
    public final ProviderType getProviderType() { return _Provider_Type; }
}

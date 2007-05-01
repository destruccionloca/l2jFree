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
package net.sf.l2j.tools.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sf.l2j.tools.L2Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;

/**
 * Facade for Jdbc DAOs
 * 
 */
public abstract class BaseRootDAOJdbc
{
    private static final Log _log = LogFactory.getLog(BaseRootDAOJdbc.class);

    /**
     * if con is not null, return the same connection dev have to close it !
     * 
     * @param con
     * @return a connection
     */
    protected Connection getConnection(Connection con)
    {
        if (con == null)
        {
            try
            {
                con = ((DataSource) L2Registry.getBean("dataSource")).getConnection();
            } catch (BeansException e)
            {
                _log.fatal("Unable to retrieve connection : " + e.getMessage(), e);
            } catch (SQLException e)
            {
                _log.fatal("Unable to retrieve connection : " + e.getMessage(), e);
            }
        }
        return con;
    }

    /**
     * close a connection and don't send an error if it failed
     * @param con the connection to close
     */
    protected void closeConnectionQuietly(Connection con)
    {
        try
        {
            if ( con != null )
            {
                con.close();
            }
        } 
        catch (Exception e)
        {
            // Do nothing
        }
    }
}

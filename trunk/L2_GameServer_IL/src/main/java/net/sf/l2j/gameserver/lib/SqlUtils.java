/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlUtils
{
	private final static Log _log = LogFactory.getLog(SqlUtils.class.getName());
	
    // =========================================================
    // Data Field
	private static SqlUtils _instance;

    // =========================================================
    // Property - Public
	public static SqlUtils getInstance()
	{
        if (_instance == null) _instance = new SqlUtils();
		return _instance;
	}

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		long start = System.currentTimeMillis();

        String query = "";

		PreparedStatement statement = null;
		ResultSet rset = null;
        Connection con = null;

		Integer res[][] = null;

		try
		{
            query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
            statement = L2DatabaseFactory.getInstance().getConnection(con).prepareStatement(query);
			rset = statement.executeQuery();

			int rows = 0;
			while(rset.next())
				rows++;

			res = new Integer[rows-1][resultFields.length];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				for(int i=0; i<resultFields.length; i++)
			 		res[row][i] = rset.getInt(i+1);
				row++;
			}
		}
		catch(Exception e)
		{
			_log.warn("Error in query '" + query + "':"+e,e);
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
            try{ if ( con != null) con.close(); } catch(Exception e) {}
		}

		_log.debug("Get all rows in query '" + query + "' in " + (System.currentTimeMillis()-start) + "ms");
		return res;
	}
}

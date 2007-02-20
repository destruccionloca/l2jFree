package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2Registry;

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

    // =========================================================
    // Method - Public
	public static Integer getIntValue(String resultField, String tableName, String whereClause)
	{
        String query = "";
		Integer res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
            query = L2Registry.getInstance().prepQuerySelect(new String[] {resultField}, tableName, whereClause, true);

			statement = L2Registry.getInstance().getConnection().prepareStatement(query);
			rset = statement.executeQuery();
		
			if(rset.next()) res = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.warn("Error in query '" + query + "':"+e,e);
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
		}

		return res;
	}

    public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
    {
        String query = "";
        Integer[] res = null;
        
        PreparedStatement statement = null;
        ResultSet rset = null;

        try
        {
            query = L2Registry.getInstance().prepQuerySelect(new String[] {resultField}, tableName, whereClause, false);
            statement = L2Registry.getInstance().getConnection().prepareStatement(query);
            rset = statement.executeQuery();
            
            int rows = 0;
            
            while (rset.next())
                rows++;
            
            if (rows == 0) return new Integer[0];

            res = new Integer[rows-1];

            rset.first();
        
            int row = 0;
            while (rset.next())
            {
                res[row] = rset.getInt(1);
            }
        }
        catch(Exception e)
        {
            _log.warn("mSGI: Error in query '" + query + "':"+e,e);
        }
        finally
        {
            try{ rset.close();  } catch(Exception e) {}
            try{ statement.close(); } catch(Exception e) {}
        }

        return res;
    }

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		long start = System.currentTimeMillis();

        String query = "";

		PreparedStatement statement = null;
		ResultSet rset = null;

		Integer res[][] = null;

		try
		{
            query = L2Registry.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
            statement = L2Registry.getInstance().getConnection().prepareStatement(query);
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
		}

		_log.debug("Get all rows in query '" + query + "' in " + (System.currentTimeMillis()-start) + "ms");
		return res;
	}
}

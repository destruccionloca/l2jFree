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
package net.sf.l2j.gameserver.datatables;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.l2j.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PetNameTable
{
   private final static Log _log = LogFactory.getLog(PetNameTable.class.getName());
   
   private static PetNameTable _instance;
   
   public static PetNameTable getInstance()
   {
       if (_instance == null)
       {
           _instance = new PetNameTable();
       }
       return _instance;
   }
   
   public boolean doesPetNameExist(String name, int petNpcId)
   {
       boolean result = true;
       java.sql.Connection con = null;
       
       try
       {
           con = L2DatabaseFactory.getInstance().getConnection(con);
           PreparedStatement statement = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id=?");
           statement.setString(1, name);
           statement.setString(2, Integer.toString(PetDataTable.getItemIdByPetId(petNpcId)));
           ResultSet rset = statement.executeQuery();
           result = rset.next();
           rset.close();
           statement.close();
       }
       catch (SQLException e)
       {
           _log.warn("could not check existing petname:"+e.getMessage());
       }
       finally
       {
           try { con.close(); } catch (Exception e) {}
       }
       return result;
   }
}
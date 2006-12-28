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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 * @author  DiezelMax
 */
public class ItemsOnGroundManager
{
    private static final Log _log = LogFactory.getLog(ItemsOnGroundManager.class.getName());
    
    private static ItemsOnGroundManager _Instance;

    public static final ItemsOnGroundManager getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing ItemsOnGroundManager");
            _Instance = new ItemsOnGroundManager();
            _Instance.load();
        }
        return _Instance;
    }

    private void load()
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            Statement s = con.createStatement();
            ResultSet result;
            int count=0;            
            result = s.executeQuery("select object_id,item_id,count,enchant_level,x,y,z,drop_time from ItemsOnGround");
            while (result.next())
            {
                L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
                L2World.getInstance().storeObject(item);
                if (item.isStackable() && result.getInt(3) > 1) item.setCount(result.getInt(3));
                item.setIsVisible(true);
                item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6) ,result.getInt(7));
                item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
                item.getPosition().getWorldRegion().addVisibleObject(item);
                item.setDropTime(System.currentTimeMillis());
                if (Config.AUTODESTROY_ITEM_AFTER > 0) ItemsAutoDestroy.getInstance().addItem(item);
                L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
                count++;
            }            
            result.close();
            s.close();
            _log.info("Restored " + count + " items on the ground");
            } catch (Exception e) {
                _log.fatal("error while loading Items on Ground " + e,e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
        
    }

    
    public void Save(L2ItemInstance item, int x, int y, int z)
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
        java.sql.Connection con = null;
        try
        {
            try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("insert into ItemsOnGround(object_id,item_id,count,enchant_level,x,y,z,drop_time) values(?,?,?,?,?,?,?,?)");
            statement.setInt(1, item.getObjectId());
            statement.setInt(2, item.getItemId());
            statement.setInt(3, item.getCount());
            statement.setInt(4, item.getEnchantLevel());
            statement.setInt(5, x);
            statement.setInt(6, y);
            statement.setInt(7, z);
            statement.setLong(8,item.getDropTime());
            statement.execute();
            statement.close();
            } catch (Exception e) {
                _log.fatal("error while inserting into table " + e,e);
            }
             
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
        
    }

    public void removeObject(L2Object item)
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
        java.sql.Connection con = null;
        try
        {
            try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("delete from ItemsOnGround where object_id=?");
            statement.setInt(1, item.getObjectId()); 
            statement.execute();
            statement.close();
            } catch (Exception e) {
                _log.fatal("error while inserting into table " + e,e);
            }
             
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
        
    }

}

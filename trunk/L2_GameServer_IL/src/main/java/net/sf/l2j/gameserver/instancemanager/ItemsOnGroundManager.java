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
import net.sf.l2j.gameserver.templates.L2EtcItemType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manage all items on ground
 * 
 * @version $Revision: $ $Date: $
 * @author  DiezelMax - original ideea and sql queue/performance improvements
 * @author  Enforcer  - actual build
 */
public class ItemsOnGroundManager
{
    protected static Log _log = LogFactory.getLog(ItemsOnGroundManager.class.getName());
    
    private static ItemsOnGroundManager _Instance;

    private ItemsOnGroundManager()
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
    }

    public static final ItemsOnGroundManager getInstance()
    {
        if (_Instance == null)
        {
            _Instance = new ItemsOnGroundManager();
            _Instance.load();
        }
        return _Instance;
    }

    private void load()
    {
        if(!Config.SAVE_DROPPED_ITEM) 
            return;
        
        // if DestroyPlayerDroppedItem was previously  false, items curently protected will be added to ItemsAutoDestroy
        if (Config.DESTROY_DROPPED_PLAYER_ITEM)
        {
            java.sql.Connection con = null;
            try 
            {
                String str = null;
                if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM) //Recycle misc. items only
                    str = "update itemsonground set drop_time=? where drop_time=-1 and equipable=0";        
                else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM) //Recycle all items including equipable
                    str = "update itemsonground set drop_time=? where drop_time=-1";
                con = L2DatabaseFactory.getInstance().getConnection(con);
                PreparedStatement statement = con.prepareStatement(str);
                statement.setLong(1, System.currentTimeMillis());
                statement.execute();
                statement.close();
             } 
             catch (Exception e)
             {
                 _log.fatal("error while updating table ItemsOnGround " + e,e);
             }
             finally 
             {
                  try { con.close(); } catch (Exception e) {}
            }
        }

        //Add items to world
        java.sql.Connection con = null;
        try
        {
            try {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            Statement s = con.createStatement();
            ResultSet result;
            int count=0;
            result = s.executeQuery("select object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable from itemsonground");
            while (result.next())
            {
                L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
                L2World.getInstance().storeObject(item);
                if (item.isStackable() && result.getInt(3) > 1) //this check and..
                    item.setCount(result.getInt(3));                
                if (result.getInt(4) > 0)           // this, are really necessary?
                    item.setEnchantLevel(result.getInt(4));
                item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6) ,result.getInt(7));
                item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
                item.getPosition().getWorldRegion().addVisibleObject(item);
                item.setDropTime(result.getLong(8));
                if (result.getLong(8) == -1)
                    item.setProtected(true);
                else
                    item.setProtected(false);
                item.setIsVisible(true);
                L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
                count++;                
                // add to ItemsAutoDestroy only items not protected
                if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId())){
                    if(result.getLong(8) > -1)
                    {
                        if((Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB)
                           ||(Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB))
                            ItemsAutoDestroy.getInstance().addItem(item);
                    }
                }                
            }
            result.close();
            s.close();
            if (count > 0)
                _log.info("ItemsOnGroundManager: restored " + count + " items.");
            else
                _log.info("Initializing ItemsOnGroundManager.");
            } catch (Exception e) {
                _log.fatal("error while loading ItemsOnGround " + e,e);
            }
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
    }

    public void Save(L2ItemInstance item)
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
        if(CursedWeaponsManager.getInstance().isCursed(item.getItemId())) return;
        SQLQueue.getInstance().add("insert into itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) values("+item.getObjectId()+", "+item.getItemId()+", "+item.getCount()+", "+item.getEnchantLevel()+", "+item.getX()+", "+item.getY()+", "+item.getZ()+", "+(item.isProtected()?-1:item.getDropTime())+", "+(item.isEquipable()?1:0)+");");
    }

    public void removeObject(L2Object item)
    {
        if(!Config.SAVE_DROPPED_ITEM) return;
        SQLQueue.getInstance().add("DELETE FROM itemsonground WHERE object_id=" + item.getObjectId() + ";");
    }

}

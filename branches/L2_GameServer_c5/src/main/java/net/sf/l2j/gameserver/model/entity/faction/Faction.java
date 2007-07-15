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
package net.sf.l2j.gameserver.model.entity.faction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class Faction
{
    private final static Log _log = LogFactory.getLog(Faction.class.getName());
    
    private int _Id                             = 0;
    private String _name                        = null;
    private float _points                       = 0;
    private int _joinprice                      = 0;
    private int _side                           = 0; // 0 = Neutral 1 = Good 2 = Evil
    private FastList<Integer> _list_classes     = new FastList<Integer>();
    private FastList<Integer> _list_npcs        = new FastList<Integer>();
    private FastMap<Integer, String> _list_title = new FastMap<Integer, String>();
    
    public Faction(int factionId)
    {
        this._Id = factionId;
        String _classlist                   = null;
        String _npclist                   = null;
        String _titlelist                   = null;
        int _tside  = 0;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from factions where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
                this._name = rs.getString("name");
                this._joinprice = rs.getInt("price");
                _classlist = rs.getString("allowed_classes");
                _titlelist = rs.getString("titlelist");
                _npclist = rs.getString("npcs");
                this._points = rs.getFloat("points");
                _tside = rs.getInt("side");
            }
            statement.close();
            
            if(_tside<=2)
                _side = _tside;
            
            if(_classlist.length()>0)
                for (String id : _classlist.split(",")) 
                    _list_classes.add(Integer.parseInt(id));

            if(_npclist.length()>0)
                for (String id : _npclist.split(",")) 
                    _list_npcs.add(Integer.parseInt(id));
            
            if(_titlelist.length()>0)
                for (String id : _titlelist.split(";"))
                    _list_title.put(Integer.valueOf(id.split(",")[0]),id.split(",")[1]);
        }
        catch (Exception e)
        {
            _log.error("Exception: Faction load: " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    private void updateDB()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("update factions set points = ? where id = ?");
            statement.setFloat(1, this._points);
            statement.setInt(2, this._Id);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.error("Exception: Faction.load(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public void addPoints(int points) 
    {
        this._points+=points;
        this.updateDB(); 
    }

    public void clearPoints()
    {
        this._points = 0;
        this.updateDB(); 
    }

    public final int getId() { return this._Id; }
    public final String getName() { return this._name; }
    public final float getPoints() { return this._points; }
    public final FastList<Integer> getClassList(){ return this._list_classes; } 
    public final FastList<Integer> getNpcList(){ return this._list_npcs; }
    public final FastMap<Integer, String> getTitle(){ return this._list_title; }
    public final int getPrice() { return this._joinprice; }
    public final int getSide() { return this._side; }
}

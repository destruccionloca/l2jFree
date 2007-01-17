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
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import net.sf.l2j.L2DatabaseFactory;

/** 
 * @author evill33t
 * 
 */
public class Faction
{
    protected static Logger _log = Logger.getLogger(Faction.class.getName());
    
    private int _Id                             = 0;
    private String _name                        = null;
    private float _points                       = 0;

    public Faction(int factionId)
    {
        this._Id = factionId;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from factions where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
                this._name = rs.getString("name");
                this._points = rs.getFloat("points");
            }
            statement.close();
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

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("update factions set points = ? where id = ?");
            statement.setFloat(1, this._points);
            statement.setInt(2, this._Id);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.error("Exception: Couple.load(): " + e.getMessage(),e);
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
}

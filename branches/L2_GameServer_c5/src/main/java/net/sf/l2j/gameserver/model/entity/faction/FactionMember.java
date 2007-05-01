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
import java.util.Calendar;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.FactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class FactionMember
{
    private static final Log _log = LogFactory.getLog(FactionMember.class.getName());
    
    // =========================================================
    // Data Field
    private int _playerId                      = 0;
    private int _factionId                     = 0;
    private int _factionPoints                 = 0;    
    private int _contributions                 = 0;
    private Calendar _joinDate;
    private int _side;


    // =========================================================
    // Constructor
    public FactionMember(int playerId)
    {
        this._playerId = playerId;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from faction_members where player_id = ?");
            statement.setInt(1, this._playerId);
            rs = statement.executeQuery();

            while (rs.next())
            {
                this._factionId = rs.getInt("faction_id");
                this._factionPoints = rs.getInt("faction_points");
                this._contributions = rs.getInt("contributions");
                this._joinDate = Calendar.getInstance();
                this._joinDate.setTimeInMillis(rs.getLong("join_date"));
                Faction faction = FactionManager.getInstance().getFactions(_factionId);
                if(faction!=null)
                {
                    _side = faction.getSide();
                }
                
            }
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionMember.load(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public FactionMember(int playerId, int factionId)
    {
        this._playerId = playerId;
        this._factionId = factionId;
        this._factionPoints = 0;
        this._contributions = 0;
        this._joinDate = Calendar.getInstance();
        this._joinDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("INSERT INTO faction_members (player_id, facion_id, faction_points, contributions, join_date) VALUES (?, ?, 0, 0, ?)");
            statement.setInt(1, this._playerId);
            statement.setInt(2, this._factionId);
            statement.setLong(3, this._joinDate.getTimeInMillis());            
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("",e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void quitFaction()
    {
        java.sql.Connection con = null;
        this._factionId = 0;
        this._factionPoints = 0;
        this._contributions = 0;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            statement = con.prepareStatement("DELETE FROM faction_members WHERE player_id=?");
            statement.setInt(1, this._playerId);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionMember.quitFaction(): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void updateDb()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            statement = con.prepareStatement("UPDATE faction_members SET faction_points=?,contributions=?,faction_id=? WHERE player_id=?");
            statement.setInt(1, this._factionPoints);
            statement.setInt(2, this._contributions);
            statement.setInt(3, this._factionId);
            statement.setInt(4, this._playerId);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionMember.updateDb(): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void addFactionPoints(int amount)
    {
        this._factionPoints += amount;
        this.updateDb();
    }

    public void addContributions(int amount)
    {
        this._contributions += amount;
        this.updateDb();
    }
    
    
    public boolean reduceFactionPoints(int amount)
    {
        if(amount<getFactionPoints())
        {
            this._factionPoints -= amount;
            this.updateDb();            
            return true;
        }
        else
            return false;
    }
    
    public void setFactionPoints(int amount)
    {
        this._factionPoints = amount;
        this.updateDb();
    }

    public void setContribution(int amount)
    {
        this._factionPoints = amount;
        this.updateDb();
    }

    public void setFactionId(int factionId)
    {
        this._factionId = factionId;
        this.updateDb();
    }

    public final int getPlayerId() { return this._playerId; }
    public final int getFactionId() { return this._factionId; }
    public final int getSide() { return this._side; }
    public final int getFactionPoints() { return this._factionPoints; }
    public final int getContributions() { return this._contributions; }
    public final Calendar getJoinDate() { return this._joinDate; }
}

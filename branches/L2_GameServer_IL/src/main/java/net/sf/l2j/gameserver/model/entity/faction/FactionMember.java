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
        _playerId = playerId;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from faction_members where player_id = ?");
            statement.setInt(1, _playerId);
            rs = statement.executeQuery();

            while (rs.next())
            {
                _factionId = rs.getInt("faction_id");
                _factionPoints = rs.getInt("faction_points");
                _contributions = rs.getInt("contributions");
                _joinDate = Calendar.getInstance();
                _joinDate.setTimeInMillis(rs.getLong("join_date"));
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
        _playerId = playerId;
        _factionId = factionId;
        _factionPoints = 0;
        _contributions = 0;
        _joinDate = Calendar.getInstance();
        _joinDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("INSERT INTO faction_members (player_id, facion_id, faction_points, contributions, join_date) VALUES (?, ?, 0, 0, ?)");
            statement.setInt(1, _playerId);
            statement.setInt(2, _factionId);
            statement.setLong(3, _joinDate.getTimeInMillis());            
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
        _factionId = 0;
        _factionPoints = 0;
        _contributions = 0;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            statement = con.prepareStatement("DELETE FROM faction_members WHERE player_id=?");
            statement.setInt(1, _playerId);
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
            statement.setInt(1, _factionPoints);
            statement.setInt(2, _contributions);
            statement.setInt(3, _factionId);
            statement.setInt(4, _playerId);
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
        _factionPoints += amount;
        updateDb();
    }

    public void addContributions(int amount)
    {
        _contributions += amount;
        updateDb();
    }
    
    
    public boolean reduceFactionPoints(int amount)
    {
        if(amount<getFactionPoints())
        {
            _factionPoints -= amount;
            updateDb();            
            return true;
        }
        else
            return false;
    }
    
    public void setFactionPoints(int amount)
    {
        _factionPoints = amount;
        updateDb();
    }

    public void setContribution(int amount)
    {
        _factionPoints = amount;
        updateDb();
    }

    public void setFactionId(int factionId)
    {
        _factionId = factionId;
        updateDb();
    }

    public final int getPlayerId() { return _playerId; }
    public final int getFactionId() { return _factionId; }
    public final int getSide() { return _side; }
    public final int getFactionPoints() { return _factionPoints; }
    public final int getContributions() { return _contributions; }
    public final Calendar getJoinDate() { return _joinDate; }
}
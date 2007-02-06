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
import java.util.Calendar;
import net.sf.l2j.L2DatabaseFactory;

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
    private int _contributions                 = 0;
    private Calendar _joinDate;


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

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from faction_members where id = ?");
            statement.setInt(1, this._playerId);
            rs = statement.executeQuery();

            while (rs.next())
            {
                this._factionId = rs.getInt("faction_id");
                this._contributions = rs.getInt("contributins");
                this._joinDate = Calendar.getInstance();
                this._joinDate.setTimeInMillis(rs.getLong("joinDate"));
            }
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionMember.load(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public void quitFaction()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
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
    
    public final int getPlayerId() { return this._playerId; }

    public final int getFactionId() { return this._factionId; }
    public final int getContributions() { return this._contributions; }
    public final Calendar getJoinDate() { return this._joinDate; }
}

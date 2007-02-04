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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.Faction;

/** 
 * @author evill33t
 * 
 */
public class FactionManager
{
    protected static Logger _log = Logger.getLogger(FactionManager.class.getName());

    // =========================================================
    private static FactionManager _Instance;
    public static final FactionManager getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing FactionManager");
            _Instance = new FactionManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================
    
    // =========================================================
    // Data Field
    private FastList<Faction> _Factions;

    
    // =========================================================
    // Method - Public
    public void reload()
    {
        this.getFactions().clear();
        this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select id from factions order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
                getFactions().add(new Faction(rs.getInt("id")));
            }

            statement.close();

            _log.info("Loaded: " + getFactions().size() + " couples(s)");
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionsManager.load(): " + e.getMessage(),e);
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Faction getFactions(int FactionId)
    {
        int index = getFactionIndex(FactionId);
        if (index >= 0) return getFactions().get(index);
        return null;
    }

    public final int getFactionIndex(int FactionId)
    {
        Faction faction;
        for (int i = 0; i < getFactions().size(); i++)
        {
            faction = getFactions().get(i);
            if (faction != null && faction.getId() == FactionId) return i;
        }
        return -1;
    }

    public final FastList<Faction> getFactions()
    {
        if (_Factions == null) _Factions = new FastList<Faction>();
        return _Factions;
    }
}
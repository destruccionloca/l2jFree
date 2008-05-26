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

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.faction.Faction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class FactionManager
{
    private final static Log _log = LogFactory.getLog(FactionManager.class.getName());

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
    private FastList<String> _list_titles       = new FastList<String>();
    
    // =========================================================
    // Method - Public
    public void reload()
    {
        this.getFactions().clear();
        this.getFactionTitles().clear();
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

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select id from factions order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
                Faction faction = new Faction(rs.getInt("id"));
                getFactions().add(faction);
                for(FastMap.Entry<Integer, String> e = faction.getTitle().head(), end = faction.getTitle().tail(); (e = e.getNext()) != end;)
                    _list_titles.add(e.getValue().toLowerCase());
                faction = null;
            }

            statement.close();

            _log.info("Loaded: " + getFactions().size() + " faction(s)");
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
    
    public final FastList<String> getFactionTitles()
    {
        if (_list_titles == null) _list_titles = new FastList<String>();
        return _list_titles;
    }
}
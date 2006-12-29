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
import java.util.List;
import org.apache.log4j.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


/** 
 * @author evill33t
 * 
 */
public class CoupleManager
{
    protected static Logger _log = Logger.getLogger(CoupleManager.class.getName());

    // =========================================================
    private static CoupleManager _Instance;
    public static final CoupleManager getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing CoupleManager");
            _Instance = new CoupleManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================
    
    // =========================================================
    // Data Field
    private List<Couple> _Couples;

    
    // =========================================================
    // Method - Public
    public void reload()
    {
        this.getCouples().clear();
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

            statement = con.prepareStatement("Select id from couples order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
                getCouples().add(new Couple(rs.getInt("id")));
            }

            statement.close();

            _log.info("Loaded: " + getCouples().size() + " couples(s)");
        }
        catch (Exception e)
        {
            _log.error("Exception: CoupleManager.load(): " + e.getMessage(),e);
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Couple getCouple(int coupleId)
    {
        int index = getCoupleIndex(coupleId);
        if (index >= 0) return getCouples().get(index);
        return null;
    }
    
    public void createCouple(L2PcInstance player1,L2PcInstance player2)
    {
        if(player1!=null && player2!=null)
        {
            if(player1.getPartnerId()==0 && player2.getPartnerId()==0)
            {
                int _player1id = player1.getObjectId();
                int _player2id = player2.getObjectId();
                
                getCouples().add(new Couple(player1,player2));
                player1.setPartnerId(_player2id);
                player2.setPartnerId(_player1id);
            }
        }
    }
    
    public final int getCoupleIndex(int coupleId)
    {
        Couple couple;
        for (int i = 0; i < getCouples().size(); i++)
        {
            couple = getCouples().get(i);
            if (couple != null && couple.getId() == coupleId) return i;
        }
        return -1;
    }

    public final List<Couple> getCouples()
    {
        if (_Couples == null) _Couples = new FastList<Couple>();
        return _Couples;
    }
}
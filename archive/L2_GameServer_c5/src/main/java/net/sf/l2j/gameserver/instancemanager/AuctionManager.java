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

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.Auction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuctionManager
{
    protected static Log _log = LogFactory.getLog(AuctionManager.class.getName());

    // =========================================================
    private static AuctionManager _Instance;
    public static final AuctionManager getInstance()
    {
        if (_Instance == null)
        {
    		_log.info("Initializing AuctionManager");
        	_Instance = new AuctionManager();
        	_Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private FastList<Auction> _Auctions;
    
    // =========================================================
    // Constructor
    public AuctionManager()
    {
    }

    // =========================================================
    // Method - Public
    public void reload()
    {
    	this.getAuctions().clear();
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

            statement = con.prepareStatement("Select id from auction order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
            	getAuctions().add(new Auction(rs.getInt("id")));
            }

            statement.close();

            _log.info("Loaded: " + getAuctions().size() + " auction(s)");
        }
        catch (Exception e)
        {
            _log.error("Exception: AuctionManager.load(): " + e.getMessage(),e);
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Auction getAuction(int auctionId)
    {
        int index = getAuctionIndex(auctionId);
        if (index >= 0) return getAuctions().get(index);
        return null;
    }

    public final int getAuctionIndex(int auctionId)
    {
        Auction auction;
        for (int i = 0; i < getAuctions().size(); i++)
        {
        	auction = getAuctions().get(i);
            if (auction != null && auction.getId() == auctionId) return i;
        }
        return -1;
    }

    public final FastList<Auction> getAuctions()
    {
        if (_Auctions == null) _Auctions = new FastList<Auction>();
        return _Auctions;
    }
}

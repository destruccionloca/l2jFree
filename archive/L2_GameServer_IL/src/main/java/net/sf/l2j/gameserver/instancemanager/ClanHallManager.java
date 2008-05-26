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
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.IZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClanHallManager
{
    protected static Log _log = LogFactory.getLog(ClanHallManager.class.getName());
	
	private static ClanHallManager _instance;
	
	private Map<Integer, ClanHall> _clanHall;
	private Map<Integer, ClanHall> _freeClanHall;
	private boolean _loaded = false;
	public static ClanHallManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanHallManager();
		}
		return _instance;
	}
	public boolean loaded(){
		return _loaded;
	}
	private ClanHallManager()
	{
		_clanHall = new FastMap<Integer, ClanHall>();
		_freeClanHall = new FastMap<Integer, ClanHall>();
		load();
	}
	/** Reload All Clan Hall */
	public final void reload(){
		_clanHall.clear();
		_freeClanHall.clear();
		load();
	}
	/** Load All Clan Hall */
	private final void load(){
       java.sql.Connection con = null;
        try
        {
        	int id;
            PreparedStatement statement;
            ResultSet rs;
            con = L2DatabaseFactory.getInstance().getConnection(con);
            statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
            rs = statement.executeQuery();
            while (rs.next())
            {
            	id = rs.getInt("id");
            	if(rs.getInt("ownerId") == 0)
            		_freeClanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),0,rs.getInt("Grade"),rs.getBoolean("paid")));
            	else
            	{
            		if(ClanTable.getInstance().getClan(rs.getInt("ownerId")) != null)
            		{
            			_clanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),rs.getLong("paidUntil"),rs.getInt("Grade"),rs.getBoolean("paid")));
            			ClanTable.getInstance().getClan(rs.getInt("ownerId")).setHasHideout(id);
            		}
            		else
            		{
            			_freeClanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),rs.getLong("paidUntil"),rs.getInt("Grade"),rs.getBoolean("paid")));
            			_freeClanHall.get(id).free();
            			AuctionManager.getInstance().initNPC(id);
            		}
            	}
            }
            statement.close();
            _log.info("ClanHallManager: loaded "+getClanHalls().size() +" clan halls");
            _log.info("ClanHallManager: loaded "+getFreeClanHalls().size() +" free clan halls");
            _loaded = true;
        }
        catch (Exception e)
        {
        	_log.fatal("Exception: ClanHallManager.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}
	/** Get Map with all FreeClanHalls */
	public final Map<Integer, ClanHall> getFreeClanHalls(){
		return _freeClanHall;
	}
	/** Get Map with all ClanHalls */
	public final Map<Integer, ClanHall> getClanHalls(){
		return _clanHall;
	}
	/** Check is free ClanHall */
	public final boolean isFree(int chId){
		if(_freeClanHall.containsKey(chId))
			return true;
		return false;
	}
	/** Free a ClanHall */
	public final synchronized void setFree(int chId){
		_freeClanHall.put(chId,_clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	/** Set ClanHallOwner */
	public final synchronized void setOwner(int chId, L2Clan clan){
		if(!_clanHall.containsKey(chId)){
			_clanHall.put(chId,_freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}else
			_clanHall.get(chId).free();
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { 
    	return (getClanHall(obj) != null); 
    }
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { 
    	return (getClanHall(x, y) != null); 
    }
    /** Get Clan Hall by Id */
    public final ClanHall getClanHall(int clanHallId)
    {
    	if(_clanHall.containsKey(clanHallId))
    		return _clanHall.get(clanHallId);
    	if(_freeClanHall.containsKey(clanHallId))
    		return _freeClanHall.get(clanHallId);
        return null;
    }
    /** Get Clan Hall by Object */
    public final ClanHall getClanHall(L2Object activeObject) 
    { 
    	return getClanHall(activeObject.getPosition().getX(), activeObject.getPosition().getY()); 
    }
    /** Get Clan Hall by region x,y */
    public final ClanHall getClanHall(int x, int y)
    {
        int index = getClanHallIndex(x, y,_clanHall);
        if (index >= 0) return _clanHall.get(index);
        index = getClanHallIndex(x, y,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
        return null;
    }
    /** Get Clan Hall by region x,y,offset */
    public final ClanHall getClanHall(int x, int y, int offset)
    {
        int index = getClanHallIndex(x, y, offset,_clanHall);
        if (index >= 0) return _clanHall.get(index);
        index = getClanHallIndex(x, y, offset,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
        return null;
    }

    /** Get Clan Hall by name */
    public final ClanHall getClanHall(String name)
    {
        int index = getClanHallIndex(name,_clanHall);
        if (index >= 0) return getClanHalls().get(index);
        index = getClanHallIndex(name,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
        return null;
    }
    /** Get Clan Hall by Owner */
    public final ClanHall getClanHallByOwner(L2Clan clan)
    {
    	for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
    		if (clan.getClanId() == ch.getValue().getOwnerId())
    			return ch.getValue();
        return null;
    }
    /** ClanHallId By Region x,y */
    private final int getClanHallIndex(int x, int y, Map<Integer,ClanHall> clanHall)
    {
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
        {
        	try
        	{
        		if (ch.getValue().checkIfInZone(x, y))
        			return ch.getKey();
        	}
        	catch (NullPointerException e) {}
        }
        return -1;
    }
    
    /** ClanHallId by region x,y,offset */
    private final int getClanHallIndex(int x, int y, int offset, Map<Integer,ClanHall> clanHall)
    {
        int id = -1;
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
        {
            IZone zone = ch.getValue().getZone();
            if (zone != null)
            {
             	if (clanHall != null && zone.getZoneDistance(x, y) < offset)
            	{ 
            		id = ch.getKey();
            		offset = (int)zone.getZoneDistance(x, y);
            	}
            }
        }
        return id;
    }
    /** ClanHallId by name */
    private final int getClanHallIndex(String name, Map<Integer,ClanHall> clanHall)
    {
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
            if (ch.getValue().getName().equalsIgnoreCase(name.trim())) 
            	return ch.getKey();
        return -1;
    }
}
